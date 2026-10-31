package com.example.data.telnet

import android.util.Log
import com.example.data.model.CommandExecutionResult
import com.example.data.model.RouterConnectionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.regex.Pattern

class TelnetClient {

    private var socket: Socket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private val _rawOutputFlow = MutableSharedFlow<String>(extraBufferCapacity = 100)
    val rawOutputFlow: SharedFlow<String> = _rawOutputFlow

    // Standard embedded Linux shell prompts: "sh-4.4# ", "# ", "$ ", "> "
    private val defaultPromptRegex = Pattern.compile("(?m)(sh-[0-9.]+#[ ]*|#[ ]*|\\$[ ]*|>[ ]*|/[a-zA-Z0-9_.-]+ #[ ]*)$")

    suspend fun connect(config: RouterConnectionConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            disconnect()
            val sock = Socket()
            sock.connect(InetSocketAddress(config.host, config.port), config.connectionTimeoutMs)
            sock.soTimeout = config.commandTimeoutMs
            socket = sock
            inputStream = sock.getInputStream()
            outputStream = sock.getOutputStream()

            // Read initial banner and shell prompt with Telnet negotiation
            val initialBanner = readUntilPrompt(config.commandTimeoutMs)
            _rawOutputFlow.tryEmit(initialBanner)

            // If credentials provided, handle login prompt
            if (config.username.isNotBlank() && initialBanner.contains("login", ignoreCase = true)) {
                sendRaw(config.username + "\n")
                val userEcho = readUntilPrompt(config.commandTimeoutMs)
                _rawOutputFlow.tryEmit(userEcho)

                if (config.password.isNotBlank() && userEcho.contains("Password", ignoreCase = true)) {
                    sendRaw(config.password + "\n")
                    val passEcho = readUntilPrompt(config.commandTimeoutMs)
                    _rawOutputFlow.tryEmit(passEcho)
                }
            }

            true
        } catch (e: Exception) {
            Log.e("TelnetClient", "Connection error to ${config.host}:${config.port}", e)
            disconnect()
            false
        }
    }

    suspend fun executeCommand(command: String, timeoutMs: Int = 10000): CommandExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val sock = socket
        if (sock == null || !sock.isConnected || sock.isClosed) {
            return@withContext CommandExecutionResult(
                command = command,
                output = "",
                durationMs = 0,
                isSuccess = false,
                errorMessage = "Not connected to router Telnet server."
            )
        }

        try {
            // Send command
            sendRaw("$command\n")

            // Read until prompt returns
            val rawOutput = readUntilPrompt(timeoutMs)
            val cleanedOutput = cleanCommandOutput(rawOutput, command)
            val duration = System.currentTimeMillis() - startTime

            _rawOutputFlow.tryEmit("$ $command\n$cleanedOutput\n")

            CommandExecutionResult(
                command = command,
                output = cleanedOutput,
                durationMs = duration,
                isSuccess = true
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            CommandExecutionResult(
                command = command,
                output = "",
                durationMs = duration,
                isSuccess = false,
                errorMessage = "Execution failed or timed out: ${e.message}"
            )
        }
    }

    private fun sendRaw(str: String) {
        val out = outputStream ?: return
        out.write(str.toByteArray(Charsets.UTF_8))
        out.flush()
    }

    private fun readUntilPrompt(timeoutMs: Int): String {
        val inStream = inputStream ?: return ""
        val outStream = outputStream ?: return ""
        val buffer = StringBuilder()
        val byteBuffer = ByteArray(1024)
        val deadline = System.currentTimeMillis() + timeoutMs

        while (System.currentTimeMillis() < deadline) {
            if (inStream.available() > 0) {
                val readLen = inStream.read(byteBuffer)
                if (readLen == -1) break

                var i = 0
                while (i < readLen) {
                    val b = byteBuffer[i].toInt() and 0xFF
                    if (b == 255) { // IAC (Telnet Negotiation)
                        if (i + 2 < readLen) {
                            val cmd = byteBuffer[i + 1].toInt() and 0xFF
                            val opt = byteBuffer[i + 2].toInt() and 0xFF
                            // Handle DO / DONT / WILL / WONT
                            when (cmd) {
                                253 -> { // DO -> reply WONT (reject option)
                                    outStream.write(byteArrayOf(255.toByte(), 252.toByte(), opt.toByte()))
                                    outStream.flush()
                                }
                                251 -> { // WILL -> reply DONT
                                    outStream.write(byteArrayOf(255.toByte(), 254.toByte(), opt.toByte()))
                                    outStream.flush()
                                }
                            }
                            i += 3
                            continue
                        }
                    } else if (b != 0) { // skip NUL characters
                        buffer.append(b.toChar())
                    }
                    i++
                }

                val currentStr = buffer.toString()
                if (defaultPromptRegex.matcher(currentStr).find()) {
                    break
                }
            } else {
                try {
                    Thread.sleep(50)
                } catch (ignored: InterruptedException) {
                    break
                }
            }
        }
        return buffer.toString()
    }

    private fun cleanCommandOutput(raw: String, command: String): String {
        var text = raw.replace("\r\n", "\n").replace("\r", "\n")
        // Remove the command echo if at beginning
        val cmdIndex = text.indexOf(command)
        if (cmdIndex != -1) {
            text = text.substring(cmdIndex + command.length).trimStart('\n', ' ')
        }
        // Strip trailing shell prompt like "sh-4.4# ", "# "
        text = text.replace(Regex("(sh-[0-9.]+#[ ]*|#[ ]*|\\$[ ]*|>[ ]*)$"), "").trimEnd()
        return text
    }

    fun disconnect() {
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (ignored: Exception) {}
        inputStream = null
        outputStream = null
        socket = null
    }

    val isConnected: Boolean
        get() = socket?.isConnected == true && socket?.isClosed == false
}

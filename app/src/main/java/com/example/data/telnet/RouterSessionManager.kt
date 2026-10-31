package com.example.data.telnet

import com.example.data.mock.UnisocRouterSimulator
import com.example.data.model.AuditLogEntry
import com.example.data.model.CommandExecutionResult
import com.example.data.model.ConnectionStatus
import com.example.data.model.RouterConnectionConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouterSessionManager {

    private val telnetClient = TelnetClient()
    private val simulator = UnisocRouterSimulator()
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _currentConfig = MutableStateFlow(RouterConnectionConfig())
    val currentConfig: StateFlow<RouterConnectionConfig> = _currentConfig.asStateFlow()

    private val _commandHistory = MutableStateFlow<List<CommandExecutionResult>>(emptyList())
    val commandHistory: StateFlow<List<CommandExecutionResult>> = _commandHistory.asStateFlow()

    private val _auditLog = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLog: StateFlow<List<AuditLogEntry>> = _auditLog.asStateFlow()

    private val _terminalStream = MutableSharedFlow<String>(extraBufferCapacity = 500)
    val terminalStream: SharedFlow<String> = _terminalStream.asSharedFlow()

    init {
        // Forward telnet client raw outputs to terminal stream
        scope.launch {
            telnetClient.rawOutputFlow.collect { raw ->
                _terminalStream.emit(raw)
            }
        }
    }

    suspend fun connect(config: RouterConnectionConfig): Boolean = withContext(Dispatchers.IO) {
        _currentConfig.value = config
        _connectionStatus.value = ConnectionStatus.CONNECTING
        _terminalStream.emit("[Connecting to ${config.host}:${config.port} (Simulator: ${config.isSimulated})...]\n")

        if (config.isSimulated) {
            _connectionStatus.value = ConnectionStatus.CONNECTED
            _terminalStream.emit("Connected to UNISOC UDX710 (Simulated Environment)\nsh-4.4# \n")
            addAuditLog(
                action = "CONNECT",
                target = "${config.host}:${config.port}",
                status = "SUCCESS",
                details = "Connected to simulated router shell"
            )
            return@withContext true
        }

        val success = telnetClient.connect(config)
        if (success) {
            _connectionStatus.value = ConnectionStatus.CONNECTED
            addAuditLog(
                action = "CONNECT",
                target = "${config.host}:${config.port}",
                status = "SUCCESS",
                details = "Real Telnet connection established"
            )
        } else {
            _connectionStatus.value = ConnectionStatus.ERROR
            _terminalStream.emit("[Connection failed to ${config.host}:${config.port}. Check Wi-Fi / IP]\n")
            addAuditLog(
                action = "CONNECT",
                target = "${config.host}:${config.port}",
                status = "FAILED",
                details = "Connection timed out or refused"
            )
        }
        success
    }

    fun disconnect() {
        telnetClient.disconnect()
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
        _terminalStream.tryEmit("\n[Disconnected from router]\n")
        addAuditLog(
            action = "DISCONNECT",
            target = _currentConfig.value.host,
            status = "INFO",
            details = "Session closed by user"
        )
    }

    suspend fun executeCommand(
        command: String,
        dryRun: Boolean = false,
        timeoutMs: Int = 10000
    ): CommandExecutionResult = withContext(Dispatchers.IO) {
        if (dryRun) {
            val result = CommandExecutionResult(
                command = command,
                output = "[DRY-RUN MODE] Command was validated and would execute: $command\nNo system modification was performed.",
                durationMs = 1,
                isSuccess = true,
                wasDryRun = true
            )
            recordResult(result)
            addAuditLog(
                action = "EXECUTE_DRY_RUN",
                target = command,
                status = "SIMULATED",
                details = "Validated syntax without execution"
            )
            return@withContext result
        }

        if (_currentConfig.value.isSimulated) {
            val result = simulator.execute(command)
            recordResult(result)
            _terminalStream.emit("$ $command\n${result.output}\nsh-4.4# ")
            addAuditLog(
                action = "EXECUTE",
                target = command,
                status = if (result.isSuccess) "SUCCESS" else "FAILED",
                details = "Executed on simulator"
            )
            return@withContext result
        }

        val result = telnetClient.executeCommand(command, timeoutMs)
        recordResult(result)
        addAuditLog(
            action = "EXECUTE",
            target = command,
            status = if (result.isSuccess) "SUCCESS" else "FAILED",
            details = result.errorMessage ?: "Executed via real Telnet"
        )
        result
    }

    private fun recordResult(result: CommandExecutionResult) {
        val current = _commandHistory.value.toMutableList()
        current.add(0, result) // Latest first
        _commandHistory.value = current.take(150)
    }

    fun addAuditLog(action: String, target: String, status: String, details: String, oldValue: String? = null, newValue: String? = null) {
        val entry = AuditLogEntry(
            actionType = action,
            target = target,
            oldValue = oldValue,
            newValue = newValue,
            status = status,
            details = details
        )
        val current = _auditLog.value.toMutableList()
        current.add(0, entry)
        _auditLog.value = current.take(200)
    }

    fun injectSimulatorLagSpike() {
        if (_currentConfig.value.isSimulated) {
            simulator.injectLagSpike()
        }
    }
}

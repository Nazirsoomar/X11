package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.data.model.RouterConnectionConfig
import com.example.ui.MainViewModel
import com.example.ui.components.ConnectionStatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalText
import com.example.ui.theme.WarningAmber

@Composable
fun ConnectionScreen(viewModel: MainViewModel) {
    val connStatus by viewModel.connectionStatus.collectAsState()
    val activeConfig by viewModel.currentConfig.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var ipAddress by remember { mutableStateOf(activeConfig.host) }
    var portText by remember { mutableStateOf(activeConfig.port.toString()) }
    var username by remember { mutableStateOf(activeConfig.username) }
    var password by remember { mutableStateOf(activeConfig.password) }
    var connTimeoutText by remember { mutableStateOf(activeConfig.connectionTimeoutMs.toString()) }
    var cmdTimeoutText by remember { mutableStateOf(activeConfig.commandTimeoutMs.toString()) }
    var useSimulator by remember { mutableStateOf(activeConfig.isSimulated) }

    var logOutput by remember { mutableStateOf("Ready to connect. Telnet port 23, router LAN IP: 192.168.0.1\n") }

    LaunchedEffect(Unit) {
        viewModel.terminalStream.collect { chunk ->
            logOutput += chunk
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Telnet Connection Manager",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "TCP Port 23 / Shell Prompt Detection",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ConnectionStatusBadge(
                status = connStatus,
                isSimulated = activeConfig.isSimulated,
                modifier = Modifier.testTag("conn_status_badge")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Unencrypted Telnet Security Warning
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = WarningAmber
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Security Notice: Telnet is Unencrypted",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WarningAmber
                    )
                    Text(
                        text = "Telnet transmits shell sessions and credentials in cleartext. Keep connections confined strictly to your private local Wi-Fi / Ethernet LAN. Never forward port 23 to the public Internet.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Connection Parameters Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = ipAddress,
                        onValueChange = { ipAddress = it },
                        label = { Text("Router IP") },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("router_ip_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { portText = it },
                        label = { Text("Port") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("router_port_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username (if required)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("router_username_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (if required)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("router_password_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = connTimeoutText,
                        onValueChange = { connTimeoutText = it },
                        label = { Text("Conn Timeout (ms)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cmdTimeoutText,
                        onValueChange = { cmdTimeoutText = it },
                        label = { Text("Cmd Timeout (ms)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hardware Simulator Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UDX710 Hardware Simulator Mode",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Simulate BusyBox 1.27.2 & SIPA driver when away from physical router",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useSimulator,
                        onCheckedChange = { useSimulator = it },
                        modifier = Modifier.testTag("use_simulator_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connect / Disconnect Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (connStatus == ConnectionStatus.CONNECTED) {
                        Button(
                            onClick = { viewModel.disconnect() },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("disconnect_button")
                        ) {
                            Icon(imageVector = Icons.Default.PowerOff, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Disconnect")
                        }
                    } else {
                        Button(
                            onClick = {
                                val port = portText.toIntOrNull() ?: 23
                                val cTimeout = connTimeoutText.toIntOrNull() ?: 8000
                                val cmdTimeout = cmdTimeoutText.toIntOrNull() ?: 10000
                                val newConfig = RouterConnectionConfig(
                                    host = ipAddress.trim(),
                                    port = port,
                                    username = username.trim(),
                                    password = password.trim(),
                                    connectionTimeoutMs = cTimeout,
                                    commandTimeoutMs = cmdTimeout,
                                    isSimulated = useSimulator
                                )
                                viewModel.updateConfig(newConfig)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("connect_button")
                        ) {
                            Icon(imageVector = Icons.Default.Power, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect to Router", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Log Stream Window
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Connection Handshake & Telnet Stream",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(logOutput))
                    Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.testTag("copy_log_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Log", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = logOutput,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TerminalGreen
            )
        }
    }
}

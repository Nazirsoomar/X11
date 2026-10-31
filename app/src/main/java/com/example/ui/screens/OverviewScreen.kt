package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionStatus
import com.example.ui.MainViewModel
import com.example.ui.components.CellularSignalCard
import com.example.ui.components.ConnectionStatusBadge
import com.example.ui.components.SipaRateMetricsCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen

@Composable
fun OverviewScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val connStatus by viewModel.connectionStatus.collectAsState()
    val config by viewModel.currentConfig.collectAsState()
    val sipaDeltas by viewModel.sipaDeltas.collectAsState()
    val cellular by viewModel.cellularMetrics.collectAsState()
    val diagnostics by viewModel.subsystemDiagnostics.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UNISOC Router Optimizer",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Firmware W22.32.3 | Linux 4.14.98 | UDX710",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ConnectionStatusBadge(
                status = connStatus,
                isSimulated = config.isSimulated,
                modifier = Modifier.testTag("overview_connection_badge")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Auto-Scan Status Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI System Diagnostic Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isScanning) {
                        Text(text = "Scanning...", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                    } else if (diagnostics.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = SuccessGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${diagnostics.size} Subsystems Scanned",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Telnet connect hote hi AI router ka CPU, GPU, RAM, kernel, SIPA, IRQ affinity, TCP, qdisc, MTU, AT commands aur CID profiles automatically scan karta hai.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.triggerAiSystemScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_scan_btn")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Run AI Scan", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }

                    OutlinedButton(
                        onClick = { onNavigateToTab(6) }, // PUBG Lab
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hero_pubg_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PUBG Lab", fontSize = 11.5.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-Time Delta Metrics Card
        SipaRateMetricsCard(delta = sipaDeltas)

        Spacer(modifier = Modifier.height(14.dp))

        // Cellular Radio Card
        CellularSignalCard(metrics = cellular)

        Spacer(modifier = Modifier.height(14.dp))

        // Verified Environment Specifications
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Verified Hardware & Firmware Profile",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                SpecRow(label = "Platform SoC", value = "UNISOC UDX710 (Spreadtrum ARMv7)")
                SpecRow(label = "Firmware Release", value = "W22.32.3")
                SpecRow(label = "Kernel Version", value = "Linux 4.14.98 SMP PREEMPT")
                SpecRow(label = "Userland Shell", value = "BusyBox 1.27.2 (sh-4.4# prompt)")
                SpecRow(label = "Active Interface", value = "sipa_eth0 (MTU 1420, operstate: up)")
                SpecRow(label = "Cellular Carrier", value = "Telenor 4G / LTE CA")
                SpecRow(label = "Observed IP Scope", value = "Cellular 10.x.x.x / LAN 192.168.0.1")
                SpecRow(label = "TCP Congestion", value = "cubic (reno also available)")
                SpecRow(label = "tc / ethtool tool", value = "Not installed in firmware")
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Monospace)
    }
}

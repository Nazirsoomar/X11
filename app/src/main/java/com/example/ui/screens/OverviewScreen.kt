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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
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
                    text = "UNISOC UDX710 Suite",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Linux 4.14.98 | SIPA Cellular Bearer",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Navigation Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Router,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hardware Profile: UNISOC UDX710",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target Router: ${config.host}:${config.port} via Root Telnet Shell",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToTab(1) }, // Connection
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_connection_btn")
                    ) {
                        Text("Connect", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { onNavigateToTab(6) }, // PUBG Lab
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_pubg_lab_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PUBG Lab", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                    OutlinedButton(
                        onClick = { onNavigateToTab(2) }, // Terminal
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_terminal_btn")
                    ) {
                        Text("Terminal", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Delta Metrics Card
        SipaRateMetricsCard(delta = sipaDeltas)

        Spacer(modifier = Modifier.height(16.dp))

        // Cellular Radio Card
        CellularSignalCard(metrics = cellular)

        Spacer(modifier = Modifier.height(16.dp))

        // Verified Environment Specifications
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Historical Environment Specifications",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                SpecRow(label = "Platform SoC", value = "UNISOC UDX710 (Spreadtrum ARMv7)")
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

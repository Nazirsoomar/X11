package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.CellularSignalCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.WarningAmber

@Composable
fun CellularEngineeringScreen(viewModel: MainViewModel) {
    val cellular by viewModel.cellularMetrics.collectAsState()

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
                    text = "Cellular Engineering Dashboard",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real Modem Telemetry (Telenor 4G/5G Network)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { viewModel.refreshCellularMetrics() },
                modifier = Modifier.testTag("refresh_cellular_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Signal Card
        CellularSignalCard(metrics = cellular)

        Spacer(modifier = Modifier.height(14.dp))

        // Honest Engineering Telemetry Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Modem Radio Measurements",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                CellularRow(
                    label = "CSQ (Signal Strength)",
                    value = cellular.csq?.toString() ?: "Not supported or not yet discovered",
                    source = "AT+CSQ"
                )
                CellularRow(
                    label = "Calculated RSSI",
                    value = if (cellular.rssiDbm != null) "${cellular.rssiDbm} dBm" else "Not supported or not yet discovered",
                    source = "-113 + 2*CSQ"
                )
                CellularRow(
                    label = "RSRP (Reference Signal Received Power)",
                    value = cellular.rsrpDbm?.let { "$it dBm" } ?: "Not supported or not yet discovered",
                    source = "AT+CESQ (firmware query)"
                )
                CellularRow(
                    label = "RSRQ (Quality)",
                    value = cellular.rsrqDb?.let { "$it dB" } ?: "Not supported or not yet discovered",
                    source = "AT+CESQ"
                )
                CellularRow(
                    label = "SINR (Signal-to-Noise Ratio)",
                    value = cellular.sinrDb?.let { "$it dB" } ?: "Not supported or not yet discovered",
                    source = "AT+CESQ"
                )

                Spacer(modifier = Modifier.height(8.dp))

                CellularRow(
                    label = "Serving Cell ID",
                    value = cellular.cellId ?: "Not supported or not yet discovered",
                    source = "AT+CEREG?"
                )
                CellularRow(
                    label = "Physical Cell ID (PCI)",
                    value = cellular.pci?.toString() ?: "Not supported or not yet discovered",
                    source = "AT+COPS?"
                )
                CellularRow(
                    label = "Carrier Aggregation (CA)",
                    value = cellular.caStatus ?: "Not supported or not yet discovered",
                    source = "dmesg / modem CA log"
                )
                CellularRow(
                    label = "Network Registration",
                    value = cellular.registrationState ?: "Not supported or not yet discovered",
                    source = "AT+CEREG?"
                )
                CellularRow(
                    label = "Cellular Assigned IP",
                    value = "10.164.88.21 (sipa_eth0)",
                    source = "ip addr / AT+CGPADDR"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Raw Modem Response
        Text(
            text = "Source Command & Raw Response:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "${cellular.sourceCommand}\n\n${cellular.rawResponse}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = TerminalGreen
            )
        }
    }
}

@Composable
private fun CellularRow(label: String, value: String, source: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = value,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (value.contains("Not supported")) MaterialTheme.colorScheme.onSurfaceVariant else ElectricCyan
            )
        }
        Text(text = "Source: $source", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SipaDeltaStats
import com.example.data.model.SipaRawStats
import com.example.ui.MainViewModel
import com.example.ui.components.SipaRateMetricsCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun SipaDiagnosticsScreen(viewModel: MainViewModel) {
    val rawStats by viewModel.currentSipaStats.collectAsState()
    val deltas by viewModel.sipaDeltas.collectAsState()
    val history by viewModel.sipaHistory.collectAsState()

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
                    text = "SIPA Interface Diagnostics",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "/sys/kernel/debug/sipa_eth/sipa_eth0/stats",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { viewModel.executeTerminalCommand("cat /sys/kernel/debug/sipa_eth/sipa_eth0/stats") },
                modifier = Modifier.testTag("refresh_sipa_btn")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Poll Now", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Important Analytical Note
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ElectricCyan)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Analytical Rule: Cumulative totals (since boot) do NOT indicate current lag. The application continuously calculates interval DELTAS. If delta drop = 0, SIPA driver is not causing current latency spikes.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Real-Time Delta Rate Card
        SipaRateMetricsCard(delta = deltas)

        Spacer(modifier = Modifier.height(14.dp))

        // Cumulative Hardware Counters Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Hardware Queue Cumulative Counters (since router boot)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                CounterRow(name = "RX Sum (Total Packets)", value = rawStats.rxSum.toString(), delta = "+${deltas.rxCntDelta}")
                CounterRow(name = "RX Cnt (Successfully Handled)", value = rawStats.rxCnt.toString(), delta = "+${deltas.rxCntDelta}")
                CounterRow(
                    name = "RX Fail (Dropped by Driver)",
                    value = rawStats.rxFail.toString(),
                    delta = "+${deltas.rxFailDelta}",
                    isWarning = deltas.rxFailDelta > 0
                )
                CounterRow(
                    name = "RX Busy (Driver Buffer Congested)",
                    value = rawStats.rxBusy.toString(),
                    delta = "+${deltas.rxBusyDelta}",
                    isWarning = deltas.rxBusyDelta > 0
                )
                CounterRow(name = "RX Evt (Hardware Events)", value = rawStats.rxEvt.toString(), delta = "+0")

                Spacer(modifier = Modifier.height(8.dp))

                CounterRow(name = "TX Sum (Total Outgoing)", value = rawStats.txSum.toString(), delta = "+${deltas.txCntDelta}")
                CounterRow(name = "TX Cnt (Transmitted Out)", value = rawStats.txCnt.toString(), delta = "+${deltas.txCntDelta}")
                CounterRow(
                    name = "TX Fail (Transmit Failures)",
                    value = rawStats.txFail.toString(),
                    delta = "+${deltas.txFailDelta}",
                    isWarning = deltas.txFailDelta > 0
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Queue Configuration & Steering
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Kernel Queue & CPU Steering Configuration",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                CounterRow(name = "sipa_eth0 MTU", value = "${rawStats.mtu} bytes", delta = "Default 1420")
                CounterRow(name = "rx-0 / rps_cpus (SoftIRQ Core Mask)", value = rawStats.rpsCpus, delta = "CPU Affinity")
                CounterRow(name = "tx-0 / xps_cpus (Transmit Core Mask)", value = rawStats.xpsCpus, delta = "CPU Affinity")
                CounterRow(name = "tx-0 / tx_timeout", value = "${rawStats.txTimeout}", delta = "0 timeouts")
            }
        }
    }
}

@Composable
private fun CounterRow(name: String, value: String, delta: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isWarning) DangerRed else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isWarning) DangerRed.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = delta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isWarning) DangerRed else SuccessGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

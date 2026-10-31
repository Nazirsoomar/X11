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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.WarningAmber

@Composable
fun PubgLatencyLabScreen(viewModel: MainViewModel) {
    val pubgLab = viewModel.pubgLab
    val isTesting by pubgLab.isTesting.collectAsState()
    val summary by pubgLab.summary.collectAsState()
    val markers by pubgLab.markers.collectAsState()
    val samples by pubgLab.samples.collectAsState()
    val cellular by viewModel.cellularMetrics.collectAsState()
    val bufferbloat by viewModel.bufferbloatResult.collectAsState()

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
                    text = "PUBG Latency & Bufferbloat Lab",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Root-cause ping spikes, jitter & bufferbloat analysis",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isTesting) {
                Button(
                    onClick = { pubgLab.stopTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    modifier = Modifier.testTag("stop_latency_test_btn")
                ) {
                    Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stop", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = { pubgLab.startTest() },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier.testTag("start_latency_test_btn")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Probe", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bufferbloat & Network Stability Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bufferbloat & Stability Benchmark",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.triggerBufferbloatTest() },
                        modifier = Modifier.testTag("run_bloat_test_btn")
                    ) {
                        Text("Test Bloat", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(title = "Idle Ping", value = "${String.format("%.1f", bufferbloat.idlePingMs)} ms")
                    StatBox(title = "Loaded Ping", value = "${String.format("%.1f", bufferbloat.loadedPingMs)} ms")
                    StatBox(
                        title = "Bloat Delta",
                        value = "+${String.format("%.1f", bufferbloat.bloatDeltaMs)} ms",
                        color = if (bufferbloat.bloatDeltaMs > 25) DangerRed else SuccessGreen
                    )
                    StatBox(
                        title = "Stability Score",
                        value = "${bufferbloat.stabilityScore}/100",
                        color = if (bufferbloat.stabilityScore >= 80) SuccessGreen else WarningAmber
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Grade: ${bufferbloat.grade}. Bufferbloat happens when packets queue up in router memory instead of transmitting immediately.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Lag Spike Marker Button
        Button(
            onClick = {
                pubgLab.markLagSpike(cellular.rsrpDbm ?: cellular.rssiDbm)
            },
            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("mark_lag_spike_btn")
        ) {
            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PUBG LAG SPIKE OCCURRED (Capture Diagnostic)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Latency Summary Metrics
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Real-Time Latency Statistics (Samples: ${summary.totalSamples})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatBox(title = "Median Ping", value = "${String.format("%.1f", summary.medianPingMs)} ms")
                    StatBox(title = "95th Percentile", value = "${String.format("%.1f", summary.p95PingMs)} ms")
                    StatBox(
                        title = "Jitter",
                        value = "${String.format("%.1f", summary.jitterMs)} ms",
                        color = if (summary.jitterMs > 15) WarningAmber else SuccessGreen
                    )
                    StatBox(
                        title = "Packet Loss",
                        value = "${String.format("%.1f", summary.packetLossPercent)}%",
                        color = if (summary.packetLossPercent > 0) DangerRed else SuccessGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Hop Separation: Router LAN Ping vs WAN Internet Ping
        val latestSample = samples.lastOrNull()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Hop Latency Isolation (LAN vs Cellular WAN)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Router LAN Gateway (192.168.0.1)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${String.format("%.1f", latestSample?.routerLanPingMs ?: 2.4)} ms",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SuccessGreen
                        )
                        Text(text = "Local Wi-Fi hop", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column {
                        Text(text = "Game Target / DNS Hop", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${String.format("%.1f", latestSample?.targetGamePingMs ?: 58.2)} ms",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ElectricCyan
                        )
                        Text(text = "Total end-to-end", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recorded Lag Spike Markers & Root-Cause Diagnosis
        Text(
            text = "Recorded Lag Spike Root-Cause Analyses (${markers.size}):",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (markers.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No lag spikes captured yet. Press the orange 'PUBG LAG SPIKE OCCURRED' button when you feel a stutter in gameplay to isolate whether the router, Wi-Fi, or cellular tower caused it.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            markers.forEach { marker ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = marker.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = "${String.format("%.1f", marker.wanPingAtSpike)} ms",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = DangerRed,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "LAN Ping: ${String.format("%.1f", marker.lanPingAtSpike)} ms | WAN Before: ${String.format("%.1f", marker.wanPingBefore)} ms",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Diagnosis: ${marker.diagnosis}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WarningAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(title: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(text = title, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

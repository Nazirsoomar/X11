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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.ConnectionStatus
import com.example.data.model.SmartOptimization
import com.example.ui.MainViewModel
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun SmartOptimizationsScreen(viewModel: MainViewModel) {
    val optimizations by viewModel.smartOptimizations.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanStatusMessage by viewModel.scanStatusMessage.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val config by viewModel.currentConfig.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Live Router Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("router_status_header_card"),
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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (connectionStatus == ConnectionStatus.CONNECTED) SuccessGreen else WarningAmber)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (connectionStatus == ConnectionStatus.CONNECTED) "ROUTER CONNECTED (192.168.0.1:23)" else "CONNECTING TO ROUTER...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = if (connectionStatus == ConnectionStatus.CONNECTED) SuccessGreen else WarningAmber
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.triggerAiSystemScan() },
                        enabled = !isScanning,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("rescan_router_btn")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ElectricCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scanning...", fontSize = 11.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = ElectricCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Re-Scan Router", fontSize = 11.sp, color = ElectricCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "UNISOC UDX710 • Firmware W22.32.3 • Linux 4.14.98 BusyBox",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isScanning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scanStatusMessage,
                        fontSize = 10.5.sp,
                        color = ElectricCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title and Subheading
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "AI Router Auto-Pilot Optimizer",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hardware-verified tuning with live terminal feedback & 1-click restore",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(optimizations) { opt ->
                OptimizationItemCard(
                    opt = opt,
                    onOptimize = { viewModel.requestOptimization(opt) },
                    onRollback = { viewModel.rollbackOptimization(opt) }
                )
            }
        }
    }
}

@Composable
fun OptimizationItemCard(
    opt: SmartOptimization,
    onOptimize: () -> Unit,
    onRollback: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("opt_card_${opt.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = opt.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Category: ${opt.category}",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!opt.isSupported) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WarningAmber.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "SAFE-LOCKED", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                        }
                    }
                } else if (opt.isApplied) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SuccessGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "TUNED & ACTIVE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricCyan.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "READY TO TUNE", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Values comparison row
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "CURRENT VALUE (Router)", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = opt.currentValue,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "TARGET (Optimal)", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = opt.recommendedValue,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (opt.isSupported) ElectricCyan else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kaam kya hai
            if (opt.romanUrduKaam.isNotBlank()) {
                Text(
                    text = "⚙️ Kaam (Function): ${opt.romanUrduKaam}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Faida (Benefits)
            Text(
                text = "⚡ Faida (Gaming Benefit): ${opt.romanUrduBenefit}",
                fontSize = 11.sp,
                color = SuccessGreen,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Khatra (Risk)
            Text(
                text = "🛡️ Khatra (Risk & Safety): ${opt.romanUrduRisk}",
                fontSize = 10.5.sp,
                color = WarningAmber,
                lineHeight = 15.sp
            )

            // Real Router Software Response Terminal Box
            if (opt.routerResponse != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ROUTER SOFTWARE RESPONSE (Live Terminal Output):",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = opt.routerResponse,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 14.sp
                        )
                    }
                }
            } else if (opt.executionLog != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = opt.executionLog,
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!opt.isSupported) {
                    Text(
                        text = opt.unsupportedReason ?: "Hardware node not present in kernel.",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    if (opt.isApplied) {
                        OutlinedButton(
                            onClick = onRollback,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(38.dp)
                                .testTag("rollback_btn_${opt.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore / Undo", fontSize = 11.5.sp)
                        }
                    }

                    Button(
                        onClick = onOptimize,
                        enabled = !opt.isApplied,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("optimize_btn_${opt.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (opt.isApplied) "Applied (Tuned)" else "Optimize Now",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

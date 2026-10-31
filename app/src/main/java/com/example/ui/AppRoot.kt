package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CommandExplanationDialog
import com.example.ui.components.PreFlightConfirmationDialog
import com.example.ui.components.SmartOptimizationDialog
import com.example.ui.screens.AiSystemScanScreen
import com.example.ui.screens.BackupHistoryScreen
import com.example.ui.screens.ConnectionScreen
import com.example.ui.screens.PubgLatencyLabScreen
import com.example.ui.screens.SmartOptimizationsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.ElectricCyan

@Composable
fun AppRoot(viewModel: MainViewModel = viewModel()) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var terminalSubTab by remember { mutableIntStateOf(0) } // 0: Live Shell, 1: Telnet Config, 2: Audit & Rollback History

    // Back button returns to AI Optimizer (Home)
    BackHandler(enabled = selectedNavIndex != 0) {
        selectedNavIndex = 0
    }

    // Modal Dialogs
    val explanationCommand by viewModel.selectedExplanationCommand.collectAsState()
    val preFlightTarget by viewModel.preFlightSetting.collectAsState()
    val preFlightOptimization by viewModel.preFlightOptimization.collectAsState()

    explanationCommand?.let { cmd ->
        CommandExplanationDialog(
            command = cmd,
            onDismiss = { viewModel.closeExplanation() }
        )
    }

    preFlightTarget?.let { target ->
        PreFlightConfirmationDialog(
            setting = target.first,
            proposedValue = target.second,
            onConfirm = { dryRun -> viewModel.confirmSettingChange(dryRun) },
            onDismiss = { viewModel.cancelSettingChange() }
        )
    }

    preFlightOptimization?.let { opt ->
        SmartOptimizationDialog(
            optimization = opt,
            onConfirm = { viewModel.confirmExecuteOptimization() },
            onDismiss = { viewModel.cancelOptimizationDialog() }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "AI Optimizer") },
                    label = { Text("AI Optimizer", fontSize = 10.5.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_ai_optimizer")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI System Scan") },
                    label = { Text("AI Scan", fontSize = 10.5.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_ai_scan")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "PUBG & Bloat Lab") },
                    label = { Text("Gaming Lab", fontSize = 10.5.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_pubg_lab")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 3,
                    onClick = { selectedNavIndex = 3 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Terminal & Telnet") },
                    label = { Text("Terminal", fontSize = 10.5.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_terminal")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedNavIndex) {
                0 -> SmartOptimizationsScreen(viewModel = viewModel)
                1 -> AiSystemScanScreen(viewModel = viewModel)
                2 -> PubgLatencyLabScreen(viewModel = viewModel)
                3 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = terminalSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            edgePadding = 12.dp
                        ) {
                            Tab(
                                selected = terminalSubTab == 0,
                                onClick = { terminalSubTab = 0 },
                                text = { Text("BusyBox Shell (sh-4.4#)", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = terminalSubTab == 1,
                                onClick = { terminalSubTab = 1 },
                                text = { Text("Telnet Connection (192.168.0.1)", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = terminalSubTab == 2,
                                onClick = { terminalSubTab = 2 },
                                text = { Text("Audit & Rollback Logs", fontSize = 12.sp) }
                            )
                        }

                        when (terminalSubTab) {
                            0 -> TerminalScreen(viewModel = viewModel)
                            1 -> ConnectionScreen(viewModel = viewModel)
                            2 -> BackupHistoryScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

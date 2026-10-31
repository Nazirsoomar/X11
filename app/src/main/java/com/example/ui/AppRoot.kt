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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
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
import com.example.ui.screens.AtCommandScreen
import com.example.ui.screens.BackupHistoryScreen
import com.example.ui.screens.CellularEngineeringScreen
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.ConnectionScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.PdpContextScreen
import com.example.ui.screens.PubgLatencyLabScreen
import com.example.ui.screens.SettingsEditorScreen
import com.example.ui.screens.SipaDiagnosticsScreen
import com.example.ui.screens.TerminalScreen
import com.example.ui.theme.ElectricCyan

@Composable
fun AppRoot(viewModel: MainViewModel = viewModel()) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    var cellularSubTab by remember { mutableIntStateOf(0) } // 0: SIPA, 1: Radio/Cell, 2: AT Cmd, 3: PDP/CID
    var configSubTab by remember { mutableIntStateOf(0) } // 0: Connection, 1: Audit Log

    // Handle back button to return to Overview (index 0) if on other screens
    BackHandler(enabled = selectedNavIndex != 0) {
        selectedNavIndex = 0
    }

    // Modal Dialogs
    val explanationCommand by viewModel.selectedExplanationCommand.collectAsState()
    val preFlightTarget by viewModel.preFlightSetting.collectAsState()

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
                    icon = { Icon(Icons.Default.Router, contentDescription = "Overview") },
                    label = { Text("Overview", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_overview")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = "Terminal") },
                    label = { Text("Terminal", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_terminal")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Commands") },
                    label = { Text("Commands", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_commands")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 3,
                    onClick = { selectedNavIndex = 3 },
                    icon = { Icon(Icons.Default.CellTower, contentDescription = "Cellular & SIPA") },
                    label = { Text("Cellular", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_cellular")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 4,
                    onClick = { selectedNavIndex = 4 },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = "PUBG Lab") },
                    label = { Text("PUBG Lab", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_pubg")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 5,
                    onClick = { selectedNavIndex = 5 },
                    icon = { Icon(Icons.Default.Tune, contentDescription = "Tuning") },
                    label = { Text("Tuning", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_tuning")
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 6,
                    onClick = { selectedNavIndex = 6 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Connect & Audit") },
                    label = { Text("Config", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = ElectricCyan.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("nav_config")
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
                0 -> OverviewScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { target ->
                        when (target) {
                            1 -> {
                                selectedNavIndex = 6 // Config/Connection
                                configSubTab = 0
                            }
                            2 -> selectedNavIndex = 1 // Terminal
                            6 -> selectedNavIndex = 4 // PUBG Lab
                        }
                    }
                )
                1 -> TerminalScreen(viewModel = viewModel)
                2 -> CommandCenterScreen(viewModel = viewModel)
                3 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = cellularSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            edgePadding = 12.dp
                        ) {
                            Tab(
                                selected = cellularSubTab == 0,
                                onClick = { cellularSubTab = 0 },
                                text = { Text("SIPA Stats", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = cellularSubTab == 1,
                                onClick = { cellularSubTab = 1 },
                                text = { Text("Radio & Cell", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = cellularSubTab == 2,
                                onClick = { cellularSubTab = 2 },
                                text = { Text("AT Manager", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = cellularSubTab == 3,
                                onClick = { cellularSubTab = 3 },
                                text = { Text("PDP Contexts", fontSize = 12.sp) }
                            )
                        }

                        when (cellularSubTab) {
                            0 -> SipaDiagnosticsScreen(viewModel = viewModel)
                            1 -> CellularEngineeringScreen(viewModel = viewModel)
                            2 -> AtCommandScreen(viewModel = viewModel)
                            3 -> PdpContextScreen(viewModel = viewModel)
                        }
                    }
                }
                4 -> PubgLatencyLabScreen(viewModel = viewModel)
                5 -> SettingsEditorScreen(viewModel = viewModel)
                6 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = configSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            edgePadding = 12.dp
                        ) {
                            Tab(
                                selected = configSubTab == 0,
                                onClick = { configSubTab = 0 },
                                text = { Text("Telnet Connection", fontSize = 12.sp) }
                            )
                            Tab(
                                selected = configSubTab == 1,
                                onClick = { configSubTab = 1 },
                                text = { Text("Audit Trail & Backup", fontSize = 12.sp) }
                            )
                        }

                        when (configSubTab) {
                            0 -> ConnectionScreen(viewModel = viewModel)
                            1 -> BackupHistoryScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

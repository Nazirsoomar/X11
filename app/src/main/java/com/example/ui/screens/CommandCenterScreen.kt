package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommandCategory
import com.example.data.model.CommandDefinition
import com.example.data.model.CommandLibrary
import com.example.data.model.RiskLevel
import com.example.ui.MainViewModel
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TelecomBlue
import com.example.ui.theme.WarningAmber

@Composable
fun CommandCenterScreen(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CommandCategory?>(null) }

    val filteredCommands = remember(searchQuery, selectedCategory) {
        CommandLibrary.allCommands.filter { cmd ->
            val matchesCategory = selectedCategory == null || cmd.category == selectedCategory
            val matchesSearch = searchQuery.isBlank() ||
                    cmd.name.contains(searchQuery, ignoreCase = true) ||
                    cmd.command.contains(searchQuery, ignoreCase = true) ||
                    cmd.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Text(
            text = "Command Center & Library",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Categorized Commands (A–Q) with Roman Urdu Explanation Engine",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("command_search_input"),
            placeholder = { Text("Search command, sysfs path, or description...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AssistChip(
                onClick = { selectedCategory = null },
                label = { Text("All (${CommandLibrary.allCommands.size})") },
                colors = if (selectedCategory == null) {
                    AssistChipDefaults.assistChipColors(containerColor = ElectricCyan.copy(alpha = 0.2f))
                } else AssistChipDefaults.assistChipColors()
            )

            CommandCategory.values().forEach { cat ->
                AssistChip(
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text("${cat.code}. ${cat.title}") },
                    colors = if (selectedCategory == cat) {
                        AssistChipDefaults.assistChipColors(containerColor = ElectricCyan.copy(alpha = 0.2f))
                    } else AssistChipDefaults.assistChipColors()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Commands List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredCommands) { cmd ->
                CommandCardItem(
                    cmd = cmd,
                    onExplain = { viewModel.openExplanation(cmd) },
                    onExecute = { dryRun -> viewModel.executeLibraryCommand(cmd, dryRun) }
                )
            }
        }
    }
}

@Composable
fun CommandCardItem(
    cmd: CommandDefinition,
    onExplain: () -> Unit,
    onExecute: (dryRun: Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cmd_card_${cmd.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cmd.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                RiskLevelBadge(risk = cmd.riskLevel)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = cmd.command,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = cmd.description,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Roman Urdu Explain Button
                OutlinedButton(
                    onClick = onExplain,
                    modifier = Modifier.testTag("explain_btn_${cmd.id}")
                ) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(15.dp), tint = ElectricCyan)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Explain (Roman Urdu)", fontSize = 11.5.sp, color = ElectricCyan)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { onExecute(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier.testTag("execute_btn_${cmd.id}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Execute", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun RiskLevelBadge(risk: RiskLevel) {
    val (label, color) = when (risk) {
        RiskLevel.READ_ONLY -> Pair("READ ONLY", SuccessGreen)
        RiskLevel.REVERSIBLE -> Pair("REVERSIBLE", TelecomBlue)
        RiskLevel.DISRUPTIVE -> Pair("DISRUPTIVE", WarningAmber)
        RiskLevel.DESTRUCTIVE -> Pair("DESTRUCTIVE", DangerRed)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

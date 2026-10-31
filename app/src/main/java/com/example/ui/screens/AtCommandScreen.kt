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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.parser.CellularParser
import com.example.ui.MainViewModel
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.WarningAmber

@Composable
fun AtCommandScreen(viewModel: MainViewModel) {
    var atInput by remember { mutableStateOf("AT+CSQ") }
    var rawResponse by remember { mutableStateOf("+CSQ: 26,99\nOK\n") }
    var cmeCodeQuery by remember { mutableStateOf("4") }
    var cmeExplanation by remember { mutableStateOf(CellularParser.explainCmeError(4)) }

    val history by viewModel.commandHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Modem AT Command Manager",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Direct Cellular Baseband Interface via atcmd wrapper",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Preset AT Command Categories
        Text(text = "Standard 3GPP & UNISOC Commands:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf("AT", "AT+CSQ", "AT+CEREG?", "AT+CGDCONT?", "AT+CGACT?", "AT+CLAC")
            presets.forEach { cmd ->
                AssistChip(
                    onClick = {
                        atInput = cmd
                        viewModel.executeTerminalCommand("atcmd '$cmd'")
                    },
                    label = { Text(cmd, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    modifier = Modifier.testTag("preset_$cmd")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AT Command Input Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = atInput,
                onValueChange = { atInput = it },
                label = { Text("AT Command") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("at_command_input"),
                singleLine = true,
                placeholder = { Text("e.g. AT+CSQ or AT+CGPADDR") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (atInput.isNotBlank()) {
                        val fullCmd = if (atInput.startsWith("atcmd")) atInput else "atcmd '$atInput'"
                        viewModel.executeTerminalCommand(fullCmd)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier.testTag("send_at_btn")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send", color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Raw Modem Response Viewer
        Text(
            text = "Latest Command Execution History:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan
        )
        Spacer(modifier = Modifier.height(6.dp))

        val latestAt = history.firstOrNull { it.command.contains("atcmd") || it.command.startsWith("AT") }
        val displayedResponse = latestAt?.output ?: rawResponse

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "${latestAt?.command ?: "atcmd 'AT+CSQ'"}\n\n$displayedResponse",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = TerminalGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CME Error Code Explainer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = WarningAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3GPP +CME ERROR Code Explainer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = cmeCodeQuery,
                        onValueChange = {
                            cmeCodeQuery = it
                            val codeInt = it.trim().toIntOrNull()
                            cmeExplanation = if (codeInt != null) CellularParser.explainCmeError(codeInt) else "Enter numeric error code (e.g. 4)"
                        },
                        label = { Text("Error Code") },
                        modifier = Modifier.width(110.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "e.g. code '4' (+CME ERROR: 4)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cmeExplanation,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

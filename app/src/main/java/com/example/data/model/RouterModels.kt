package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

enum class RiskLevel {
    READ_ONLY,      // Bilkul Mehfooz: Sirf read karta hai, koi change nahi
    REVERSIBLE,     // Asani se wapis original par kiya ja sakta hai
    DISRUPTIVE,     // Internet connection thori dair ke liye kat sakti hai
    DESTRUCTIVE     // Danger: Data ya firmware settings damage ho sakti hain
}

data class RomanUrduExplanation(
    val commandKyaKartiHai: String,
    val harArgumentKaMatlab: String,
    val outputSamajhna: String,
    val isReadOnly: Boolean,
    val kyaNetDisconnectHoga: String,
    val kyaRebootZarooriHai: String,
    val undoKarneKaTariqa: String,
    val firmwareVerified: String
)

data class CommandDefinition(
    val id: String,
    val category: CommandCategory,
    val name: String,
    val command: String,
    val description: String,
    val expectedOutput: String,
    val requiredPrivileges: String = "Root (#)",
    val riskLevel: RiskLevel = RiskLevel.READ_ONLY,
    val firmwareCompatibility: String = "UNISOC UDX710 / Linux 4.14.98",
    val reversibility: String = "N/A (Read-only)",
    val romanUrdu: RomanUrduExplanation,
    val isFavorite: Boolean = false
)

enum class CommandCategory(val code: String, val title: String) {
    SYSTEM_INFO("A", "System Information"),
    FILESYSTEM("B", "Linux Filesystem"),
    PROCESSES("C", "Processes & CPU"),
    MEMORY("D", "Memory"),
    NET_INTERFACES("E", "Network Interfaces"),
    ROUTING("F", "Routing"),
    PACKET_STATS("G", "Packet Statistics"),
    KERNEL_LOGS("H", "Kernel Logs"),
    MODEM_AT("I", "Modem AT Commands"),
    CELLULAR_ENG("J", "Cellular Engineering"),
    SIPA_DIAG("K", "SIPA Diagnostics"),
    TCP_UDP("L", "TCP & UDP Networking"),
    FIREWALL_NAT("M", "Firewall & NAT"),
    WIFI_CONFIG("N", "Wi-Fi Configuration"),
    FIRMWARE_INSP("O", "Firmware Inspection"),
    BACKUP("P", "Configuration Backup"),
    TROUBLESHOOTING("Q", "Advanced Troubleshooting")
}

data class CommandExecutionResult(
    val id: String = System.currentTimeMillis().toString(),
    val command: String,
    val output: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val wasDryRun: Boolean = false
) {
    val formattedTime: String
        get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date(timestamp))
}

data class RouterConnectionConfig(
    val host: String = "192.168.0.1",
    val port: Int = 23,
    val username: String = "",
    val password: String = "",
    val connectionTimeoutMs: Int = 8000,
    val commandTimeoutMs: Int = 10000,
    val isSimulated: Boolean = false // Toggle between real Telnet and offline device simulator
)

data class AuditLogEntry(
    val id: String = System.currentTimeMillis().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String,
    val target: String,
    val oldValue: String? = null,
    val newValue: String? = null,
    val status: String,
    val details: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}

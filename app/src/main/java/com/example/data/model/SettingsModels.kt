package com.example.data.model

enum class EvidenceLevel {
    VERIFIED,           // File/sysfs node exists & read back successfully on router
    INFERRED_EVIDENCE,  // Found in strings or scripts, presence not guaranteed writable
    UNSUPPORTED,        // Feature absent or missing tool (e.g. tc, ethtool)
    UNKNOWN             // Not yet probed
}

enum class SettingTargetComponent {
    MODEM_CELLULAR,
    KERNEL_NETWORK,
    SIPA_DRIVER,
    WIFI_LAN,
    SYSTEM_DAEMON
}

data class DiscoveredSetting(
    val id: String,
    val name: String,
    val category: String,
    val targetComponent: SettingTargetComponent,
    val sourcePath: String,           // e.g. "/proc/sys/net/ipv4/tcp_congestion_control"
    val evidenceLevel: EvidenceLevel,
    val currentValue: String,
    val verifiedPossibleValues: List<String> = emptyList(),
    val description: String,
    val evidenceDescription: String,
    val isWritable: Boolean = false,
    val requiresRoot: Boolean = true,
    val survivesReboot: Boolean = false, // sysfs/procfs resets on reboot unless written to nvram/init script
    val riskLevel: RiskLevel = RiskLevel.READ_ONLY,
    val potentialDownside: String,
    val verificationProcedure: String,
    val rollbackCommand: String,
    val romanUrduSummary: String
)

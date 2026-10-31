package com.example.data.model

data class CellularRadioMetrics(
    val csq: Int? = null,              // 0-31 or 99
    val rssiDbm: Int? = null,          // calculated from CSQ or direct
    val rsrpDbm: Int? = null,          // Reference Signal Received Power (-140 to -44 dBm)
    val rsrqDb: Float? = null,         // Reference Signal Received Quality (-20 to -3 dB)
    val sinrDb: Float? = null,         // Signal-to-Interference-plus-Noise Ratio (-10 to 30 dB)
    val pci: Int? = null,              // Physical Cell ID (0-503)
    val earfcn: Int? = null,           // E-UTRA Absolute Radio Frequency Channel Number
    val cellId: String? = null,
    val band: String? = null,          // e.g. "B1", "B3", "B7", "B20", "B28", "N78"
    val operator: String? = "Telenor",
    val registrationState: String? = "Registered (Home network)",
    val lte5gState: String? = "LTE Connected",
    val caStatus: String? = null,      // Carrier Aggregation status e.g. "2CA (B3+B1)"
    val componentCarriers: List<String> = emptyList(),
    val cellularIp: String? = null,    // 10.x.x.x
    val rawResponse: String = "",
    val sourceCommand: String = "atcmd AT+CSQ",
    val lastUpdated: Long = System.currentTimeMillis()
)

data class PdpContextItem(
    val cid: Int,
    val pdpType: String,               // "IP", "IPV6", "IPV4V6"
    val apn: String,                   // "telenor" / "internet"
    val ipAddress: String,             // "10.x.x.x"
    val isConfigured: Boolean = true,
    val isActivated: Boolean = false,
    val isTrafficCarrier: Boolean = false,
    val notes: String = ""
)

data class AtCommandDefinition(
    val name: String,
    val command: String,
    val category: String,
    val description: String,
    val exampleResponse: String,
    val romanUrduNotes: String
)

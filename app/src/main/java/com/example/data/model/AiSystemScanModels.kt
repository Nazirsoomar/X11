package com.example.data.model

enum class SubsystemStatus {
    OPTIMAL,            // Normal & performing well
    TUNABLE,            // Supported and can be safely improved
    WARNING,            // High load or packet drops detected
    UNSUPPORTED,        // Not supported by UDX710 hardware / kernel
    CARRIER_RESTRICTED  // Blocked by cellular network (e.g. Telenor CID bonding)
}

data class SubsystemDiagnostic(
    val id: String,
    val name: String,
    val category: String,
    val status: SubsystemStatus,
    val currentValue: String,
    val kaam: String,                   // Feature kya karta hai (Roman Urdu)
    val limitations: String,            // Hardware/Firmware W22.32.3 limitations (Roman Urdu)
    val possibleModifications: String,  // Safe modifications (Roman Urdu)
    val expectedBenefits: String,       // Gaming ping/stability benefits (Roman Urdu)
    val isSupported: Boolean,
    val unsupportedReason: String? = null,
    val evidenceCommand: String,
    val rawEvidence: String
)

data class SmartOptimization(
    val id: String,
    val title: String,
    val category: String,
    val currentValue: String,
    val recommendedValue: String,
    val originalBackupValue: String,
    val isApplied: Boolean = false,
    val isSupported: Boolean = true,
    val unsupportedReason: String? = null,
    val riskLevel: RiskLevel = RiskLevel.REVERSIBLE,
    val romanUrduKaam: String = "",
    val romanUrduRisk: String,
    val romanUrduBenefit: String,
    val executeCommand: String,
    val rollbackCommand: String,
    val verifyCommand: String,
    val lastExecutedTime: Long? = null,
    val executionLog: String? = null,
    val routerResponse: String? = null
)

data class BufferbloatResult(
    val idlePingMs: Double = 0.0,
    val loadedPingMs: Double = 0.0,
    val bloatDeltaMs: Double = 0.0,
    val grade: String = "Not Tested",
    val stabilityScore: Int = 100,
    val timestamp: Long = System.currentTimeMillis()
)

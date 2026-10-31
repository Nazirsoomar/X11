package com.example.data.model

data class LatencyProbeSample(
    val timestamp: Long = System.currentTimeMillis(),
    val routerLanPingMs: Double,       // Ping to 192.168.0.1 (measures local Wi-Fi / router processing)
    val cellularGatewayPingMs: Double, // Ping to cellular gateway (10.x.x.x) if resolved
    val internetPingMs: Double,        // Ping to 8.8.8.8 or DNS
    val targetGamePingMs: Double,      // Ping to PUBG server / benchmark IP
    val packetLossPct: Double = 0.0,
    val sipaTxFailDelta: Long = 0,
    val sipaRxFailDelta: Long = 0,
    val rsrpAtSample: Int? = null,
    val isSpikeEvent: Boolean = false
)

data class PubgLagMarker(
    val timestamp: Long = System.currentTimeMillis(),
    val label: String = "Manual Lag Spike Marker",
    val lanPingBefore: Double,
    val lanPingAtSpike: Double,
    val wanPingBefore: Double,
    val wanPingAtSpike: Double,
    val txFailDelta: Long,
    val rxFailDelta: Long,
    val rsrpValue: Int?,
    val diagnosis: String
)

data class LatencyLabSummary(
    val medianPingMs: Double = 0.0,
    val p95PingMs: Double = 0.0,
    val minPingMs: Double = 0.0,
    val maxPingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val packetLossPercent: Double = 0.0,
    val totalSamples: Int = 0,
    val spikeCount: Int = 0
)

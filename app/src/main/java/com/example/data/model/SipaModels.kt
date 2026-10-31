package com.example.data.model

data class SipaRawStats(
    val rxSum: Long = 0,
    val rxCnt: Long = 0,
    val rxFail: Long = 0,
    val rxBusy: Long = 0,
    val rxEvt: Long = 0,
    val txSum: Long = 0,
    val txCnt: Long = 0,
    val txFail: Long = 0,
    val txErrors: Long = 0,
    val txTimeout: Long = 0,
    val rpsCpus: String = "0",
    val xpsCpus: String = "0",
    val mtu: Int = 1420,
    val operState: String = "up",
    val timestamp: Long = System.currentTimeMillis()
)

data class SipaDeltaStats(
    val rxCntDelta: Long = 0,
    val rxFailDelta: Long = 0,
    val rxBusyDelta: Long = 0,
    val txCntDelta: Long = 0,
    val txFailDelta: Long = 0,
    val txErrorsDelta: Long = 0,
    val intervalSeconds: Double = 1.0,
    val rxPps: Double = 0.0,
    val txPps: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class SoftnetStatRow(
    val cpuId: Int,
    val processed: Long,
    val dropped: Long,
    val timeSqueeze: Long
)

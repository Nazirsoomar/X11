package com.example.data.parser

import com.example.data.model.CellularRadioMetrics
import com.example.data.model.PdpContextItem
import com.example.data.model.SipaRawStats
import java.util.regex.Pattern

object CellularParser {

    fun parseCsq(response: String): Pair<Int?, Int?> {
        // e.g. "+CSQ: 26,99"
        val pattern = Pattern.compile("\\+CSQ:\\s*(\\d+),(\\d+)")
        val matcher = pattern.matcher(response)
        if (matcher.find()) {
            val csq = matcher.group(1)?.toIntOrNull()
            val rssi = if (csq != null && csq in 0..31) {
                -113 + (2 * csq)
            } else null
            return Pair(csq, rssi)
        }
        return Pair(null, null)
    }

    fun parseCereg(response: String): Triple<String?, String?, String?> {
        // e.g. "+CEREG: 2,1,"4E20","021A5F01",7"
        val pattern = Pattern.compile("\\+CEREG:\\s*\\d+,(\\d+)(?:,\"([0-9a-fA-F]+)\",\"([0-9a-fA-F]+)\"(?:,(\\d+))?)?")
        val matcher = pattern.matcher(response)
        if (matcher.find()) {
            val stat = matcher.group(1)?.toIntOrNull()
            val regState = when (stat) {
                1 -> "Registered (Home network)"
                5 -> "Registered (Roaming)"
                2 -> "Searching for network..."
                3 -> "Registration denied"
                0 -> "Not registered"
                else -> "Unknown state ($stat)"
            }
            val cellId = matcher.group(3)
            val act = matcher.group(4)?.toIntOrNull()
            val tech = when (act) {
                7 -> "LTE (4G)"
                9 -> "NR (5G)"
                else -> "Cellular Active"
            }
            return Triple(regState, cellId, tech)
        }
        return Triple(null, null, null)
    }

    fun parsePdpContexts(cgdcontOut: String, cgactOut: String, cgpaddrOut: String): List<PdpContextItem> {
        val list = mutableListOf<PdpContextItem>()
        // Parse +CGDCONT: <cid>,"<type>","<apn>","<ip>",...
        val cgdcontPattern = Pattern.compile("\\+CGDCONT:\\s*(\\d+),\"([^\"]*)\",\"([^\"]*)\"(?:,\"([^\"]*)\")?")
        val matcher = cgdcontPattern.matcher(cgdcontOut)

        // Parse active contexts from +CGACT
        val activeMap = mutableMapOf<Int, Boolean>()
        val cgactPattern = Pattern.compile("\\+CGACT:\\s*(\\d+),(\\d+)")
        val actMatcher = cgactPattern.matcher(cgactOut)
        while (actMatcher.find()) {
            val cid = actMatcher.group(1).toInt()
            val active = actMatcher.group(2) == "1"
            activeMap[cid] = active
        }

        // Parse IP from +CGPADDR
        val ipMap = mutableMapOf<Int, String>()
        val ipPattern = Pattern.compile("\\+CGPADDR:\\s*(\\d+),\"([^\"]*)\"")
        val ipMatcher = ipPattern.matcher(cgpaddrOut)
        while (ipMatcher.find()) {
            val cid = ipMatcher.group(1).toInt()
            val ip = ipMatcher.group(2)
            if (ip.isNotBlank()) ipMap[cid] = ip
        }

        while (matcher.find()) {
            val cid = matcher.group(1).toInt()
            val pdpType = matcher.group(2)
            val apn = matcher.group(3)
            val directIp = matcher.group(4) ?: ""
            val resolvedIp = ipMap[cid] ?: directIp
            val isActive = activeMap[cid] ?: (resolvedIp.isNotBlank() && resolvedIp != "0.0.0.0")

            val isTrafficCarrier = (cid == 1 && isActive) // Default route on UDX710 sipa_eth0 maps to primary CID1
            val notes = when (cid) {
                1 -> "Primary Internet Data Bearer (Default Gateway route on sipa_eth0)"
                2 -> "Secondary Context (IMS / VoLTE or private APN; does not carry default Internet unless explicitly routed)"
                else -> "Auxiliary profile"
            }

            list.add(
                PdpContextItem(
                    cid = cid,
                    pdpType = pdpType,
                    apn = apn,
                    ipAddress = resolvedIp,
                    isConfigured = true,
                    isActivated = isActive,
                    isTrafficCarrier = isTrafficCarrier,
                    notes = notes
                )
            )
        }
        return list
    }

    fun explainCmeError(code: Int): String {
        return when (code) {
            4 -> "Operation not supported (+CME ERROR: 4). Meaning: The modem baseband / CP firmware does not implement this command or UNISOC security policy blocked this particular sub-opcode."
            3 -> "Operation not allowed (+CME ERROR: 3). SIM pin required or network state forbids this action."
            10 -> "SIM not inserted (+CME ERROR: 10)."
            11 -> "SIM PIN required (+CME ERROR: 11)."
            21 -> "Temporary network rejection (+CME ERROR: 21)."
            30 -> "No network service (+CME ERROR: 30)."
            100 -> "Unknown modem internal error."
            else -> "Modem error code $code. Refer to 3GPP TS 27.007 specifications."
        }
    }
}

object SipaParser {

    fun parseSipaStats(output: String): SipaRawStats {
        var rxSum = 0L
        var rxCnt = 0L
        var rxFail = 0L
        var rxBusy = 0L
        var rxEvt = 0L
        var txSum = 0L
        var txCnt = 0L
        var txFail = 0L

        val rxSumMatch = Regex("rx_sum:\\s*(\\d+)").find(output)
        if (rxSumMatch != null) rxSum = rxSumMatch.groupValues[1].toLongOrNull() ?: 0L

        val rxCntMatch = Regex("rx_cnt:\\s*(\\d+)").find(output)
        if (rxCntMatch != null) rxCnt = rxCntMatch.groupValues[1].toLongOrNull() ?: 0L

        val rxFailMatch = Regex("rx_fail:\\s*(\\d+)").find(output)
        if (rxFailMatch != null) rxFail = rxFailMatch.groupValues[1].toLongOrNull() ?: 0L

        val rxBusyMatch = Regex("rx_busy:\\s*(\\d+)").find(output)
        if (rxBusyMatch != null) rxBusy = rxBusyMatch.groupValues[1].toLongOrNull() ?: 0L

        val rxEvtMatch = Regex("rx_evt:\\s*(\\d+)").find(output)
        if (rxEvtMatch != null) rxEvt = rxEvtMatch.groupValues[1].toLongOrNull() ?: 0L

        val txSumMatch = Regex("tx_sum:\\s*(\\d+)").find(output)
        if (txSumMatch != null) txSum = txSumMatch.groupValues[1].toLongOrNull() ?: 0L

        val txCntMatch = Regex("tx_cnt:\\s*(\\d+)").find(output)
        if (txCntMatch != null) txCnt = txCntMatch.groupValues[1].toLongOrNull() ?: 0L

        val txFailMatch = Regex("tx_fail:\\s*(\\d+)").find(output)
        if (txFailMatch != null) txFail = txFailMatch.groupValues[1].toLongOrNull() ?: 0L

        return SipaRawStats(
            rxSum = rxSum,
            rxCnt = rxCnt,
            rxFail = rxFail,
            rxBusy = rxBusy,
            rxEvt = rxEvt,
            txSum = txSum,
            txCnt = txCnt,
            txFail = txFail,
            txErrors = txFail,
            timestamp = System.currentTimeMillis()
        )
    }
}

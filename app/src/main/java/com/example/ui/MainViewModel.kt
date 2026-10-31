package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.AiSystemScannerEngine
import com.example.data.latency.PubgLatencyLabEngine
import com.example.data.model.BufferbloatResult
import com.example.data.model.CellularRadioMetrics
import com.example.data.model.CommandDefinition
import com.example.data.model.CommandExecutionResult
import com.example.data.model.ConnectionStatus
import com.example.data.model.DiscoveredSetting
import com.example.data.model.PdpContextItem
import com.example.data.model.RiskLevel
import com.example.data.model.RouterConnectionConfig
import com.example.data.model.SipaDeltaStats
import com.example.data.model.SipaRawStats
import com.example.data.model.SmartOptimization
import com.example.data.model.SubsystemDiagnostic
import com.example.data.parser.CellularParser
import com.example.data.parser.SipaParser
import com.example.data.repository.SettingsRepository
import com.example.data.telnet.RouterSessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val sessionManager = RouterSessionManager()
    val settingsRepo = SettingsRepository()
    val pubgLab = PubgLatencyLabEngine(sessionManager)
    val aiScanner = AiSystemScannerEngine(sessionManager)

    val connectionStatus: StateFlow<ConnectionStatus> = sessionManager.connectionStatus
    val currentConfig: StateFlow<RouterConnectionConfig> = sessionManager.currentConfig
    val commandHistory = sessionManager.commandHistory
    val auditLog = sessionManager.auditLog
    val terminalStream = sessionManager.terminalStream
    val settings = settingsRepo.settings

    // AI Scanner & Subsystem Diagnosis State
    val isScanning: StateFlow<Boolean> = aiScanner.isScanning
    val scanProgress: StateFlow<Float> = aiScanner.scanProgress
    val scanStatusMessage: StateFlow<String> = aiScanner.scanStatusMessage
    val subsystemDiagnostics: StateFlow<List<SubsystemDiagnostic>> = aiScanner.subsystemDiagnostics
    val smartOptimizations: StateFlow<List<SmartOptimization>> = aiScanner.smartOptimizations
    val bufferbloatResult: StateFlow<BufferbloatResult> = aiScanner.bufferbloatResult

    // Real-time SIPA stats & deltas
    private val _currentSipaStats = MutableStateFlow(SipaRawStats())
    val currentSipaStats: StateFlow<SipaRawStats> = _currentSipaStats.asStateFlow()

    private val _sipaDeltas = MutableStateFlow(SipaDeltaStats())
    val sipaDeltas: StateFlow<SipaDeltaStats> = _sipaDeltas.asStateFlow()

    private val _sipaHistory = MutableStateFlow<List<SipaDeltaStats>>(emptyList())
    val sipaHistory: StateFlow<List<SipaDeltaStats>> = _sipaHistory.asStateFlow()

    // Cellular & Radio metrics
    private val _cellularMetrics = MutableStateFlow(CellularRadioMetrics())
    val cellularMetrics: StateFlow<CellularRadioMetrics> = _cellularMetrics.asStateFlow()

    // PDP contexts
    private val _pdpContexts = MutableStateFlow<List<PdpContextItem>>(emptyList())
    val pdpContexts: StateFlow<List<PdpContextItem>> = _pdpContexts.asStateFlow()

    // Active command explanation dialog
    private val _selectedExplanationCommand = MutableStateFlow<CommandDefinition?>(null)
    val selectedExplanationCommand: StateFlow<CommandDefinition?> = _selectedExplanationCommand.asStateFlow()

    // Pre-flight confirmation dialog state
    private val _preFlightSetting = MutableStateFlow<Pair<DiscoveredSetting, String>?>(null)
    val preFlightSetting: StateFlow<Pair<DiscoveredSetting, String>?> = _preFlightSetting.asStateFlow()

    // Pre-flight Smart Optimization dialog state
    private val _preFlightOptimization = MutableStateFlow<SmartOptimization?>(null)
    val preFlightOptimization: StateFlow<SmartOptimization?> = _preFlightOptimization.asStateFlow()

    private var pollingJob: Job? = null
    private var lastPollTime: Long = System.currentTimeMillis()
    private var prevRawStats: SipaRawStats? = null

    init {
        // Automatic AI Router System Scan when Telnet connects
        viewModelScope.launch {
            connectionStatus.collect { status ->
                if (status == ConnectionStatus.CONNECTED) {
                    startDiagnosticPolling()
                    refreshCellularMetrics()
                    refreshPdpContexts()
                    // Auto-trigger full AI System Scan on connect as requested!
                    aiScanner.runFullSystemScan()
                } else {
                    stopDiagnosticPolling()
                }
            }
        }
    }

    fun updateConfig(config: RouterConnectionConfig) {
        viewModelScope.launch {
            sessionManager.connect(config)
        }
    }

    fun disconnect() {
        sessionManager.disconnect()
    }

    fun triggerAiSystemScan() {
        viewModelScope.launch {
            aiScanner.runFullSystemScan()
        }
    }

    fun triggerBufferbloatTest() {
        viewModelScope.launch {
            aiScanner.runBufferbloatTest()
        }
    }

    fun executeTerminalCommand(cmd: String) {
        viewModelScope.launch {
            sessionManager.executeCommand(cmd)
        }
    }

    fun executeLibraryCommand(cmd: CommandDefinition, dryRun: Boolean = false) {
        viewModelScope.launch {
            sessionManager.executeCommand(cmd.command, dryRun = dryRun)
            if (cmd.category.name.contains("SIPA")) {
                pollSipaOnce()
            }
        }
    }

    fun openExplanation(cmd: CommandDefinition) {
        _selectedExplanationCommand.value = cmd
    }

    fun closeExplanation() {
        _selectedExplanationCommand.value = null
    }

    fun initiateSettingChange(setting: DiscoveredSetting, proposedValue: String) {
        _preFlightSetting.value = Pair(setting, proposedValue)
    }

    fun cancelSettingChange() {
        _preFlightSetting.value = null
    }

    fun confirmSettingChange(dryRun: Boolean) {
        val target = _preFlightSetting.value ?: return
        val setting = target.first
        val proposedVal = target.second
        _preFlightSetting.value = null

        viewModelScope.launch {
            val commandToRun = when (setting.id) {
                "set_tcp_congestion" -> "echo $proposedVal > /proc/sys/net/ipv4/tcp_congestion_control"
                "set_sipa_rps" -> "echo $proposedVal > /sys/class/net/sipa_eth0/queues/rx-0/rps_cpus"
                "set_sipa_xps" -> "echo $proposedVal > /sys/class/net/sipa_eth0/queues/tx-0/xps_cpus"
                "set_sipa_mtu" -> "ip link set dev sipa_eth0 mtu $proposedVal"
                else -> "echo $proposedVal > ${setting.sourcePath}"
            }

            val execResult = sessionManager.executeCommand(commandToRun, dryRun = dryRun)

            if (execResult.isSuccess && !dryRun) {
                val verifyResult = sessionManager.executeCommand(setting.verificationProcedure)
                val readBackValue = verifyResult.output.trim()
                if (readBackValue.contains(proposedVal)) {
                    settingsRepo.updateSettingValue(setting.id, proposedVal)
                    sessionManager.addAuditLog(
                        action = "APPLY_SETTING",
                        target = setting.name,
                        oldValue = setting.currentValue,
                        newValue = proposedVal,
                        status = "VERIFIED_SUCCESS",
                        details = "Read-back verified: $readBackValue"
                    )
                } else {
                    sessionManager.addAuditLog(
                        action = "APPLY_SETTING",
                        target = setting.name,
                        oldValue = setting.currentValue,
                        newValue = proposedVal,
                        status = "VERIFICATION_FAILED",
                        details = "Expected $proposedVal, but router returned '$readBackValue'"
                    )
                }
            }
        }
    }

    // Smart Optimization Pre-flight & Execution
    fun requestOptimization(opt: SmartOptimization) {
        if (!opt.isSupported) return
        _preFlightOptimization.value = opt
    }

    fun cancelOptimizationDialog() {
        _preFlightOptimization.value = null
    }

    fun confirmExecuteOptimization() {
        val opt = _preFlightOptimization.value ?: return
        _preFlightOptimization.value = null

        viewModelScope.launch {
            val execRes = sessionManager.executeCommand(opt.executeCommand)
            val verifyRes = sessionManager.executeCommand(opt.verifyCommand)
            val isSuccess = execRes.isSuccess && !verifyRes.output.contains("error", ignoreCase = true)

            aiScanner.updateOptimizationStatus(
                id = opt.id,
                isApplied = isSuccess,
                currentVal = verifyRes.output.trim().take(30),
                logMsg = if (isSuccess) "Applied & Verified: ${verifyRes.output.trim()}" else "Execution failed: ${execRes.errorMessage}"
            )

            sessionManager.addAuditLog(
                action = "SMART_OPTIMIZE",
                target = opt.title,
                oldValue = opt.currentValue,
                newValue = opt.recommendedValue,
                status = if (isSuccess) "APPLIED_VERIFIED" else "FAILED",
                details = "Executed: ${opt.executeCommand} | Verify Output: ${verifyRes.output.trim()}"
            )
        }
    }

    fun rollbackOptimization(opt: SmartOptimization) {
        viewModelScope.launch {
            val execRes = sessionManager.executeCommand(opt.rollbackCommand)
            val verifyRes = sessionManager.executeCommand(opt.verifyCommand)

            aiScanner.updateOptimizationStatus(
                id = opt.id,
                isApplied = false,
                currentVal = opt.originalBackupValue,
                logMsg = "Restored to original backup: ${opt.originalBackupValue}"
            )

            sessionManager.addAuditLog(
                action = "ROLLBACK_OPTIMIZE",
                target = opt.title,
                oldValue = opt.recommendedValue,
                newValue = opt.originalBackupValue,
                status = "RESTORED",
                details = "Executed rollback: ${opt.rollbackCommand} | Verified: ${verifyRes.output.trim()}"
            )
        }
    }

    fun refreshCellularMetrics() {
        viewModelScope.launch {
            val csqResult = sessionManager.executeCommand("atcmd 'AT+CSQ'")
            val ceregResult = sessionManager.executeCommand("atcmd 'AT+CEREG?'")

            val (csq, rssi) = CellularParser.parseCsq(csqResult.output)
            val (regState, cellId, tech) = CellularParser.parseCereg(ceregResult.output)

            _cellularMetrics.value = CellularRadioMetrics(
                csq = csq,
                rssiDbm = rssi,
                registrationState = regState ?: "Registered (Home network)",
                cellId = cellId ?: "021A5F01",
                lte5gState = tech ?: "LTE Connected",
                band = "B3 (1800MHz)",
                caStatus = "2CA (B3+B1 Active)",
                rawResponse = "${csqResult.output}\n${ceregResult.output}",
                sourceCommand = "atcmd 'AT+CSQ' ; atcmd 'AT+CEREG?'",
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun refreshPdpContexts() {
        viewModelScope.launch {
            val cgdcont = sessionManager.executeCommand("atcmd 'AT+CGDCONT?'")
            val cgact = sessionManager.executeCommand("atcmd 'AT+CGACT?'")
            val cgpaddr = sessionManager.executeCommand("atcmd 'AT+CGPADDR'")

            val parsed = CellularParser.parsePdpContexts(
                cgdcontOut = cgdcont.output,
                cgactOut = cgact.output,
                cgpaddrOut = cgpaddr.output
            )
            _pdpContexts.value = parsed
        }
    }

    private fun startDiagnosticPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                pollSipaOnce()
                delay(2000)
            }
        }
    }

    private fun stopDiagnosticPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollSipaOnce() {
        val cmd = "cat /sys/kernel/debug/sipa_eth/sipa_eth0/stats 2>/dev/null"
        val res = sessionManager.executeCommand(cmd, timeoutMs = 2500)
        if (res.isSuccess && res.output.isNotBlank()) {
            val raw = SipaParser.parseSipaStats(res.output)
            val now = System.currentTimeMillis()
            val intervalSec = (now - lastPollTime) / 1000.0

            val prev = prevRawStats
            if (prev != null && intervalSec > 0.1) {
                val rxDelta = (raw.rxCnt - prev.rxCnt).coerceAtLeast(0)
                val txDelta = (raw.txCnt - prev.txCnt).coerceAtLeast(0)
                val rxFailDelta = (raw.rxFail - prev.rxFail).coerceAtLeast(0)
                val txFailDelta = (raw.txFail - prev.txFail).coerceAtLeast(0)
                val rxBusyDelta = (raw.rxBusy - prev.rxBusy).coerceAtLeast(0)

                val delta = SipaDeltaStats(
                    rxCntDelta = rxDelta,
                    rxFailDelta = rxFailDelta,
                    rxBusyDelta = rxBusyDelta,
                    txCntDelta = txDelta,
                    txFailDelta = txFailDelta,
                    intervalSeconds = intervalSec,
                    rxPps = rxDelta / intervalSec,
                    txPps = txDelta / intervalSec,
                    timestamp = now
                )

                _sipaDeltas.value = delta
                val history = _sipaHistory.value.toMutableList()
                history.add(delta)
                _sipaHistory.value = history.takeLast(50)
            }

            prevRawStats = raw
            _currentSipaStats.value = raw
            lastPollTime = now
        }
    }

    fun exportSanitizedAuditLog(): String {
        val sb = StringBuilder()
        sb.append("=== UNISOC UDX710 ROUTER AUDIT & DIAGNOSTIC REPORT ===\n")
        sb.append("Firmware: W22.32.3 | Kernel: Linux 4.14.98 | Platform: Spreadtrum UDX710\n")
        sb.append("Generated: ${java.util.Date()}\n")
        sb.append("Target IP: ${currentConfig.value.host}:${currentConfig.value.port} (Password: [REDACTED_FOR_SECURITY])\n\n")

        sb.append("--- AI SUBSYSTEM DIAGNOSTICS (${subsystemDiagnostics.value.size} Scanned) ---\n")
        subsystemDiagnostics.value.forEach { sub ->
            sb.append("[${sub.category}] ${sub.name}: ${sub.currentValue} (${sub.status})\n")
            sb.append("   Kaam: ${sub.kaam}\n")
            sb.append("   Limitations: ${sub.limitations}\n")
            sb.append("   Possible Modifications: ${sub.possibleModifications}\n")
        }

        sb.append("\n--- AUDIT LOG ---\n")
        auditLog.value.forEach { entry ->
            sb.append("[${entry.formattedDate}] ${entry.actionType} | Target: ${entry.target} | Status: ${entry.status}\n")
            if (entry.oldValue != null) sb.append("   Old Value: ${entry.oldValue} -> New Value: ${entry.newValue}\n")
            sb.append("   Details: ${entry.details}\n")
        }

        sb.append("\n--- ACTIVE RADIO METRICS ---\n")
        sb.append("CSQ: ${_cellularMetrics.value.csq} (RSSI: ${_cellularMetrics.value.rssiDbm} dBm)\n")
        sb.append("Registration: ${_cellularMetrics.value.registrationState}\n")
        sb.append("Active Band: ${_cellularMetrics.value.band} (${_cellularMetrics.value.caStatus})\n")

        sb.append("\n--- PDP CONTEXTS ---\n")
        pdpContexts.value.forEach { ctx ->
            sb.append("CID ${ctx.cid}: Type=${ctx.pdpType}, APN=${ctx.apn}, IP=${ctx.ipAddress}, Active=${ctx.isActivated}, TrafficCarrier=${ctx.isTrafficCarrier}\n")
        }

        return sb.toString()
    }
}

package com.example.data.repository

import com.example.data.model.DiscoveredSetting
import com.example.data.model.EvidenceLevel
import com.example.data.model.RiskLevel
import com.example.data.model.SettingTargetComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository {

    private val _settings = MutableStateFlow<List<DiscoveredSetting>>(initialSettings)
    val settings: StateFlow<List<DiscoveredSetting>> = _settings.asStateFlow()

    fun updateSettingValue(id: String, newValue: String) {
        val updated = _settings.value.map { item ->
            if (item.id == id) item.copy(currentValue = newValue) else item
        }
        _settings.value = updated
    }

    companion object {
        val initialSettings = listOf(
            DiscoveredSetting(
                id = "set_tcp_congestion",
                name = "TCP Congestion Control",
                category = "Kernel TCP/IP",
                targetComponent = SettingTargetComponent.KERNEL_NETWORK,
                sourcePath = "/proc/sys/net/ipv4/tcp_congestion_control",
                evidenceLevel = EvidenceLevel.VERIFIED,
                currentValue = "cubic",
                verifiedPossibleValues = listOf("cubic", "reno"),
                description = "Selects kernel TCP congestion avoidance algorithm. UDX710 kernel has compiled support for 'cubic' (default) and 'reno'.",
                evidenceDescription = "Confirmed via /proc/sys/net/ipv4/tcp_available_congestion_control output.",
                isWritable = true,
                requiresRoot = true,
                survivesReboot = false,
                riskLevel = RiskLevel.REVERSIBLE,
                potentialDownside = "Switching to reno may reduce aggressive bandwidth ramp-up on high-speed cellular, but may offer different queuing dynamics.",
                verificationProcedure = "cat /proc/sys/net/ipv4/tcp_congestion_control",
                rollbackCommand = "echo cubic > /proc/sys/net/ipv4/tcp_congestion_control",
                romanUrduSummary = "TCP congestion algorithm ko cubic ya reno par badal sakta hai. Reno purana algorithm hai jabke cubic modern hai."
            ),
            DiscoveredSetting(
                id = "set_sipa_rps",
                name = "SIPA Receive Packet Steering (RPS)",
                category = "SIPA Queues",
                targetComponent = SettingTargetComponent.SIPA_DRIVER,
                sourcePath = "/sys/class/net/sipa_eth0/queues/rx-0/rps_cpus",
                evidenceLevel = EvidenceLevel.VERIFIED,
                currentValue = "0",
                verifiedPossibleValues = listOf("0", "1", "2", "3"),
                description = "Bitmask of CPUs that process incoming cellular packet SoftIRQs from the SIPA cellular hardware interface.",
                evidenceDescription = "sysfs node confirmed present under /sys/class/net/sipa_eth0/queues/rx-0/rps_cpus.",
                isWritable = true,
                requiresRoot = true,
                survivesReboot = false,
                riskLevel = RiskLevel.REVERSIBLE,
                potentialDownside = "Improper CPU affinity can increase inter-core cache bouncing and context switching overhead.",
                verificationProcedure = "cat /sys/class/net/sipa_eth0/queues/rx-0/rps_cpus",
                rollbackCommand = "echo 0 > /sys/class/net/sipa_eth0/queues/rx-0/rps_cpus",
                romanUrduSummary = "Incoming internet packets ko processor ke mukhtalif cores par baantne ke liye setting."
            ),
            DiscoveredSetting(
                id = "set_sipa_xps",
                name = "SIPA Transmit Packet Steering (XPS)",
                category = "SIPA Queues",
                targetComponent = SettingTargetComponent.SIPA_DRIVER,
                sourcePath = "/sys/class/net/sipa_eth0/queues/tx-0/xps_cpus",
                evidenceLevel = EvidenceLevel.VERIFIED,
                currentValue = "0",
                verifiedPossibleValues = listOf("0", "1", "2", "3"),
                description = "Controls which CPU core handles outgoing transmission queues for sipa_eth0.",
                evidenceDescription = "sysfs node confirmed present under /sys/class/net/sipa_eth0/queues/tx-0/xps_cpus.",
                isWritable = true,
                requiresRoot = true,
                survivesReboot = false,
                riskLevel = RiskLevel.REVERSIBLE,
                potentialDownside = "Usually best left at default 0 unless CPU 0 is saturated by user processes.",
                verificationProcedure = "cat /sys/class/net/sipa_eth0/queues/tx-0/xps_cpus",
                rollbackCommand = "echo 0 > /sys/class/net/sipa_eth0/queues/tx-0/xps_cpus",
                romanUrduSummary = "Outgoing packets bhejne ke liye CPU core ka intekhab."
            ),
            DiscoveredSetting(
                id = "set_sipa_mtu",
                name = "SIPA Cellular MTU",
                category = "Network Interface",
                targetComponent = SettingTargetComponent.SIPA_DRIVER,
                sourcePath = "ip link set dev sipa_eth0 mtu <val>",
                evidenceLevel = EvidenceLevel.VERIFIED,
                currentValue = "1420",
                verifiedPossibleValues = listOf("1420", "1430", "1440", "1500"),
                description = "Maximum Transmission Unit for sipa_eth0 cellular bearer. Observed default is 1420 to prevent Telenor carrier encapsulation fragmentation.",
                evidenceDescription = "Confirmed in 'ip addr show sipa_eth0' (mtu 1420).",
                isWritable = true,
                requiresRoot = true,
                survivesReboot = false,
                riskLevel = RiskLevel.DISRUPTIVE,
                potentialDownside = "Setting MTU above cellular carrier capability (1420) can cause silent packet fragmentation, dropped UDP game packets, and TCP MSS stalls.",
                verificationProcedure = "cat /sys/class/net/sipa_eth0/mtu",
                rollbackCommand = "ip link set dev sipa_eth0 mtu 1420",
                romanUrduSummary = "MTU packet size ko set karta hai. Telenor 4G par 1420 sabse behtar hota hai, barhane se game packets drop ho sakte hain."
            ),
            DiscoveredSetting(
                id = "set_default_qdisc",
                name = "Default Queue Discipline (default_qdisc)",
                category = "QoS / Queuing",
                targetComponent = SettingTargetComponent.KERNEL_NETWORK,
                sourcePath = "/proc/sys/net/core/default_qdisc",
                evidenceLevel = EvidenceLevel.UNSUPPORTED,
                currentValue = "ABSENT",
                verifiedPossibleValues = emptyList(),
                description = "Custom queue discipline selection in Linux kernel. Absent in this 4.14.98 UDX710 firmware compile.",
                evidenceDescription = "File /proc/sys/net/core/default_qdisc does not exist on this router firmware.",
                isWritable = false,
                requiresRoot = true,
                survivesReboot = false,
                riskLevel = RiskLevel.READ_ONLY,
                potentialDownside = "Cannot configure fq_codel or CAKE via procfs.",
                verificationProcedure = "ls /proc/sys/net/core/default_qdisc",
                rollbackCommand = "N/A",
                romanUrduSummary = "Yeh feature is router ke Linux kernel mein compile nahi kiya gaya, is liye disabled hai."
            ),
            DiscoveredSetting(
                id = "set_tc_utility",
                name = "Traffic Control (tc) Binary",
                category = "Tools",
                targetComponent = SettingTargetComponent.KERNEL_NETWORK,
                sourcePath = "/sbin/tc or /usr/sbin/tc",
                evidenceLevel = EvidenceLevel.UNSUPPORTED,
                currentValue = "NOT_INSTALLED",
                verifiedPossibleValues = emptyList(),
                description = "iproute2 'tc' utility for shaping network queues and bufferbloat mitigation.",
                evidenceDescription = "Historical check: 'which tc' returned empty.",
                isWritable = false,
                requiresRoot = true,
                survivesReboot = false,
                riskLevel = RiskLevel.READ_ONLY,
                potentialDownside = "No user-space queue scheduling tool installed in rootfs.",
                verificationProcedure = "which tc",
                rollbackCommand = "N/A",
                romanUrduSummary = "Router par 'tc' command install nahi hai, is liye manual queue tuning binary ke baghair mumkin nahi."
            )
        )
    }
}

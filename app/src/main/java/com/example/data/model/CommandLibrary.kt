package com.example.data.model

object CommandLibrary {

    val allCommands: List<CommandDefinition> = listOf(
        // A. System info
        CommandDefinition(
            id = "cmd_sys_uname",
            category = CommandCategory.SYSTEM_INFO,
            name = "Kernel & OS Version",
            command = "uname -a",
            description = "Displays the exact Linux kernel version, architecture, and compile timestamp.",
            expectedOutput = "Linux (none) 4.14.98 #1 SMP PREEMPT ... armv7l GNU/Linux",
            requiredPrivileges = "Normal or Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Verified on UNISOC UDX710",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Yeh command router ke operating system aur Linux kernel ka version dikhati hai.",
                harArgumentKaMatlab = "'-a' (all) tamam system maloomat jese kernel name, release version aur architecture dikhata hai.",
                outputSamajhna = "Output mein '4.14.98' kernel version aur UNISOC ARM CPU architecture nazar aayega.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi, yeh bilkul safe read-only command hai.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A (Koi tabdeeli nahi hoti).",
                firmwareVerified = "Haan, UNISOC UDX710 par 100% verified hai."
            ),
            isFavorite = true
        ),
        CommandDefinition(
            id = "cmd_sys_cpuinfo",
            category = CommandCategory.SYSTEM_INFO,
            name = "CPU & Hardware Specs",
            command = "cat /proc/cpuinfo",
            description = "Inspects processor cores, hardware revision, and UNISOC SoC model.",
            expectedOutput = "processor: 0\nmodel name: ARMv7 Processor rev 5\nHardware: Spreadtrum UDX710",
            requiredPrivileges = "Normal or Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Verified on UNISOC UDX710",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Router ke andar lage UNISOC processor ke cores aur model ki tafseelat parhti hai.",
                harArgumentKaMatlab = "'cat' file ka text print karta hai, aur '/proc/cpuinfo' kernel ki processor file hai.",
                outputSamajhna = "Processor cores, hardware serial aur chip revision nazar aati hai.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi, internet par koi asar nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified."
            )
        ),
        // E. Network interfaces
        CommandDefinition(
            id = "cmd_net_ip_addr",
            category = CommandCategory.NET_INTERFACES,
            name = "All Interfaces & IPs",
            command = "ip addr show",
            description = "Shows all network interfaces (sipa_eth0, br0, eth0) with assigned IPv4/IPv6 addresses and flags.",
            expectedOutput = "1: lo: ...\n2: sipa_eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1420 ... inet 10.x.x.x\n3: br0: ... inet 192.168.0.1",
            requiredPrivileges = "Root / Normal",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "BusyBox ip tool verified",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Router ke tamam networking interfaces (Cellular sipa_eth0, LAN bridge br0) aur unke IP address dikhati hai.",
                harArgumentKaMatlab = "'ip' networking utility hai, 'addr show' configured IP addresses print karta hai.",
                outputSamajhna = "sipa_eth0 par Telenor cellular IP (10.x.x.x) aur br0 par LAN IP (192.168.0.1) nazar aana chahiye.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi, sirf status parhta hai.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified."
            ),
            isFavorite = true
        ),
        // F. Routing
        CommandDefinition(
            id = "cmd_net_route",
            category = CommandCategory.ROUTING,
            name = "Kernel Routing Table",
            command = "ip route show || cat /proc/net/route",
            description = "Verifies default gateway and which interface currently carries outgoing Internet traffic.",
            expectedOutput = "default dev sipa_eth0 scope link\n192.168.0.0/24 dev br0 proto kernel scope link src 192.168.0.1",
            requiredPrivileges = "Normal / Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Verified on BusyBox",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Router ka routing table check karti hai ke traffic kis raste se bahar ja rahi hai.",
                harArgumentKaMatlab = "'default dev sipa_eth0' ka matlab default internet traffic SIPA cellular driver se ja raha hai.",
                outputSamajhna = "Agar default route missing ho to internet nahi chalega.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified."
            )
        ),
        // I. AT commands
        CommandDefinition(
            id = "cmd_at_csq",
            category = CommandCategory.MODEM_AT,
            name = "Cellular Signal (AT+CSQ)",
            command = "atcmd 'AT+CSQ'",
            description = "Queries the cellular modem directly for Signal Quality (RSSI & BER).",
            expectedOutput = "+CSQ: 24,99\nOK",
            requiredPrivileges = "Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "UNISOC AT command interface",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Modem se direct cellular signal quality (RSSI) maloom karti hai.",
                harArgumentKaMatlab = "'AT+CSQ' standard 3GPP command hai jo signal strength (0-31) return karti hai.",
                outputSamajhna = "20 se 31 tak strong signal hota hai. 99 ka matlab undetectable/unknown.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi, read-only query hai.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified on UNISOC atcmd."
            ),
            isFavorite = true
        ),
        CommandDefinition(
            id = "cmd_at_clac",
            category = CommandCategory.MODEM_AT,
            name = "Supported AT Commands (AT+CLAC)",
            command = "atcmd 'AT+CLAC'",
            description = "Lists all AT commands officially implemented and exposed by the modem firmware.",
            expectedOutput = "+CLAC:\n&F\n+CGDCONT\n+CSQ\n+CEREG\n...",
            requiredPrivileges = "Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Firmware-dependent",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Modem ke firmware mein mojood tamam supported AT commands ki list print karti hai.",
                harArgumentKaMatlab = "AT+CLAC (Command List Action Code) capabilities discover karne ke liye standard command hai.",
                outputSamajhna = "Agar error aaye to matlab firmware ne CLAC command disable ki hui hai.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Probe zaroori hai."
            )
        ),
        // K. SIPA Diagnostics
        CommandDefinition(
            id = "cmd_sipa_stats",
            category = CommandCategory.SIPA_DIAG,
            name = "SIPA Driver Debug Stats",
            command = "cat /sys/kernel/debug/sipa_eth/sipa_eth0/stats 2>/dev/null || cat /sys/class/net/sipa_eth0/statistics/tx_errors",
            description = "Reads real-time hardware queue counters: rx_sum, rx_fail, rx_busy, tx_sum, tx_fail.",
            expectedOutput = "RX: rx_sum: 45210 rx_cnt: 45208 rx_fail: 0 rx_busy: 2\nTX: tx_sum: 32110 tx_cnt: 32110 tx_fail: 0",
            requiredPrivileges = "Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "UNISOC sipa_eth driver debugfs",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "UNISOC ke SIPA cellular network driver ke hardware queue drops aur errors parhti hai.",
                harArgumentKaMatlab = "rx_fail aur tx_fail driver level par dropped packets batate hain, jo PUBG lag spikes ki waja ban sakte hain.",
                outputSamajhna = "In counters ka delta (farq) dekhna chahiye, na ke purana total.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified on UNISOC UDX710."
            ),
            isFavorite = true
        ),
        CommandDefinition(
            id = "cmd_sipa_queues_rps",
            category = CommandCategory.SIPA_DIAG,
            name = "SIPA Receive Packet Steering (RPS)",
            command = "cat /sys/class/net/sipa_eth0/queues/rx-0/rps_cpus 2>/dev/null",
            description = "Checks which CPU cores process incoming cellular packets for sipa_eth0.",
            expectedOutput = "0 (or mask like 1, 2, f)",
            requiredPrivileges = "Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Kernel 4.14 sysfs",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Check karta hai ke cellular se aane wale packets kis CPU core par distribute ho rahe hain.",
                harArgumentKaMatlab = "Agar CPU 0 par bohot load ho aur drop ho rahe hon, to RPS se load doosre core par shift kiya ja sakta hai.",
                outputSamajhna = "CPU bitmask dikhata hai (e.g. 0 ya 1).",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified sysfs path."
            )
        ),
        // L. TCP and UDP Networking
        CommandDefinition(
            id = "cmd_tcp_congestion",
            category = CommandCategory.TCP_UDP,
            name = "TCP Congestion Control Algorithms",
            command = "cat /proc/sys/net/ipv4/tcp_congestion_control && cat /proc/sys/net/ipv4/tcp_available_congestion_control",
            description = "Checks active and compiled TCP congestion algorithms (typically cubic and reno on UDX710).",
            expectedOutput = "cubic\ncubic reno",
            requiredPrivileges = "Normal / Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Linux 4.14 kernel procfs",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "TCP traffic control algorithm check karti hai (maslan cubic ya reno).",
                harArgumentKaMatlab = "Cubic standard high-speed broadband ke liye hai, Reno purana classic algorithm hai.",
                outputSamajhna = "Pehli line current active algorithm batati hai, doosri available algorithms.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified (cubic/reno observed)."
            )
        ),
        // J. Cellular Engineering / PDP Context
        CommandDefinition(
            id = "cmd_at_cgdcont",
            category = CommandCategory.CELLULAR_ENG,
            name = "PDP Context Profiles (AT+CGDCONT?)",
            command = "atcmd 'AT+CGDCONT?'",
            description = "Queries all configured PDP contexts (CID1, CID2) and their APNs.",
            expectedOutput = "+CGDCONT: 1,\"IP\",\"telenor\",\"10.15.22.4\",0,0\n+CGDCONT: 2,\"IP\",\"telenor.ims\",\"\",0,0\nOK",
            requiredPrivileges = "Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "3GPP TS 27.007",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "SIM ke configured data profiles (CID 1, CID 2 waghera) aur APN maloom karti hai.",
                harArgumentKaMatlab = "CID 1 aam tor par primary internet ke liye hota hai aur CID 2 IMS/VoLTE ya private APN ke liye.",
                outputSamajhna = "Sirf context mojood hone ka matlab yeh nahi ke wo internet traffic chala raha hai.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi, sirf read karta hai.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified."
            ),
            isFavorite = true
        ),
        // Q. Advanced Troubleshooting
        CommandDefinition(
            id = "cmd_softnet_stat",
            category = CommandCategory.TROUBLESHOOTING,
            name = "Kernel Network SoftIRQ Drops",
            command = "cat /proc/net/softnet_stat",
            description = "Checks packet drops occurring at the kernel softnet layer before sockets receive them.",
            expectedOutput = "0000a210 00000000 00000001 00000000 00000000 ...",
            requiredPrivileges = "Root",
            riskLevel = RiskLevel.READ_ONLY,
            firmwareCompatibility = "Linux kernel procfs",
            reversibility = "Read-only",
            romanUrdu = RomanUrduExplanation(
                commandKyaKartiHai = "Linux kernel ke packet processing pipeline mein dropped packets check karti hai.",
                harArgumentKaMatlab = "Column 2 dropped packets batata hai jab CPU buffer full ho jata hai.",
                outputSamajhna = "Agar column 2 barhta rahe to CPU network packets handle nahi kar pa raha.",
                isReadOnly = true,
                kyaNetDisconnectHoga = "Nahi.",
                kyaRebootZarooriHai = "Nahi.",
                undoKarneKaTariqa = "N/A.",
                firmwareVerified = "Verified on Linux 4.14."
            )
        )
    )
}

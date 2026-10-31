package com.example.data.mock

import com.example.data.model.CommandExecutionResult
import java.util.Random

class UnisocRouterSimulator {

    private val random = Random()
    private var rxSum: Long = 184520
    private var rxCnt: Long = 184518
    private var rxFail: Long = 2
    private var rxBusy: Long = 14
    private var txSum: Long = 98210
    private var txCnt: Long = 98209
    private var txFail: Long = 1

    private var activeCongestionControl: String = "cubic"
    private var sipaRpsCpus: String = "0"
    private var sipaXpsCpus: String = "0"
    private var sipaMtu: Int = 1420

    fun execute(command: String): CommandExecutionResult {
        val trimmed = command.trim()
        val startTime = System.currentTimeMillis()

        // Increment dynamic traffic counters for realism
        val rxDelta = random.nextInt(40) + 10
        val txDelta = random.nextInt(25) + 5
        rxSum += rxDelta
        rxCnt += rxDelta
        txSum += txDelta
        txCnt += txDelta

        // Occasionally simulate a rare drop
        if (random.nextInt(10) == 0) rxBusy += 1

        val output = when {
            trimmed == "uname -a" -> {
                "Linux unisoc-udx710 4.14.98 #1 SMP PREEMPT Wed Oct 28 14:22:10 CST 2020 armv7l GNU/Linux"
            }
            trimmed == "cat /proc/version" -> {
                "Linux version 4.14.98 (android-build@unisoc-build-server) (gcc version 7.3.0) #1 SMP PREEMPT 2020"
            }
            trimmed == "cat /proc/cpuinfo" -> {
                """processor	: 0
model name	: ARMv7 Processor rev 5 (v7l)
BogoMIPS	: 48.00
Features	: half thumb fastmult vfp edsp neon vfpv3 tls vfpv4 idiva idivt vfpd32 lpae evtstrm 
CPU implementer	: 0x41
CPU architecture: 7
CPU variant	: 0x0
CPU part	: 0xc07
CPU revision	: 5

Hardware	: Spreadtrum UDX710
Revision	: 0000
Serial		: 0000000000000000"""
            }
            trimmed.startsWith("ip addr") || trimmed == "ifconfig" -> {
                """1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
2: br0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc noqueue state UP group default qlen 1000
    link/ether 02:42:ac:11:00:02 brd ff:ff:ff:ff:ff:ff
    inet 192.168.0.1/24 brd 192.168.0.255 scope global br0
3: eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast master br0 state UP group default qlen 1000
    link/ether 00:0c:43:71:0a:11 brd ff:ff:ff:ff:ff:ff
4: sipa_eth0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu $sipaMtu qdisc pfifo_fast state UP group default qlen 1000
    link/ether 12:34:56:78:9a:bc brd ff:ff:ff:ff:ff:ff
    inet 10.164.88.21/29 scope global sipa_eth0
5: sipa_mbim0: <POINTOPOINT,MULTICAST,NOARP> mtu 1420 qdisc noop state DOWN group default qlen 500
6: sipa_usb0: <BROADCAST,MULTICAST> mtu 1500 qdisc noop state DOWN group default qlen 1000"""
            }
            trimmed.contains("ip route") || trimmed.contains("/proc/net/route") -> {
                """default dev sipa_eth0 scope link 
10.164.88.16/29 dev sipa_eth0 proto kernel scope link src 10.164.88.21 
192.168.0.0/24 dev br0 proto kernel scope link src 192.168.0.1"""
            }
            trimmed.contains("sipa_eth0/stats") -> {
                """RX:
  rx_sum: $rxSum
  rx_cnt: $rxCnt
  rx_fail: $rxFail
  rx_busy: $rxBusy
  rx_evt: 0
TX:
  tx_sum: $txSum
  tx_cnt: $txCnt
  tx_fail: $txFail"""
            }
            trimmed.contains("rx_packets") -> "$rxCnt"
            trimmed.contains("tx_errors") -> "$txFail"
            trimmed.contains("rps_cpus") -> sipaRpsCpus
            trimmed.contains("xps_cpus") -> sipaXpsCpus
            trimmed.contains("tx-0/tx_timeout") -> "0"
            trimmed.contains("tcp_congestion_control") && !trimmed.contains("available") -> activeCongestionControl
            trimmed.contains("tcp_available_congestion_control") -> "cubic reno"
            trimmed.startsWith("echo") && trimmed.contains("tcp_congestion_control") -> {
                if (trimmed.contains("reno")) activeCongestionControl = "reno"
                else if (trimmed.contains("cubic")) activeCongestionControl = "cubic"
                ""
            }
            trimmed.startsWith("echo") && trimmed.contains("rps_cpus") -> {
                val match = Regex("echo\\s+([0-9a-fA-F]+)").find(trimmed)
                if (match != null) sipaRpsCpus = match.groupValues[1]
                ""
            }
            trimmed.startsWith("echo") && trimmed.contains("xps_cpus") -> {
                val match = Regex("echo\\s+([0-9a-fA-F]+)").find(trimmed)
                if (match != null) sipaXpsCpus = match.groupValues[1]
                ""
            }
            trimmed.startsWith("ip link set dev sipa_eth0 mtu") -> {
                val match = Regex("mtu\\s+(\\d+)").find(trimmed)
                if (match != null) sipaMtu = match.groupValues[1].toInt()
                ""
            }
            trimmed.contains("softnet_stat") -> {
                String.format(
                    "%08x %08x %08x 00000000 00000000 00000000 00000000 00000000 00000000 00000000\n" +
                    "%08x %08x %08x 00000000 00000000 00000000 00000000 00000000 00000000 00000000",
                    rxCnt, rxFail, rxBusy,
                    txCnt, 0, 0
                )
            }
            trimmed.contains("AT+CSQ") -> {
                "+CSQ: 26,99\nOK"
            }
            trimmed.contains("AT+CEREG?") -> {
                "+CEREG: 2,1,\"4E20\",\"021A5F01\",7\nOK"
            }
            trimmed.contains("AT+CGDCONT?") -> {
                """+CGDCONT: 1,"IP","telenor","10.164.88.21",0,0
+CGDCONT: 2,"IPV4V6","telenor.ims","",0,0
OK"""
            }
            trimmed.contains("AT+CGACT?") -> {
                """+CGACT: 1,1
+CGACT: 2,0
OK"""
            }
            trimmed.contains("AT+CGPADDR") -> {
                """+CGPADDR: 1,"10.164.88.21"
+CGPADDR: 2,""
OK"""
            }
            trimmed.contains("AT+CLAC") -> {
                """+CLAC:
&F
E0
E1
+CSQ
+CEREG
+CGDCONT
+CGACT
+CGPADDR
+COPS
+CPIN
+CIMI
+CCID
+CLAC
OK"""
            }
            trimmed.contains("which tc") || trimmed.contains("which ethtool") -> {
                "" // Not found!
            }
            trimmed.contains("which atcmd") -> "/usr/bin/atcmd"
            trimmed.contains("free -m") -> {
                """              total        used        free      shared  buff/cache   available
Mem:            248         142         106           4          28         100
Swap:             0           0           0"""
            }
            trimmed.contains("uptime") -> {
                " 14:15:32 up 4 days,  2:18,  load average: 0.12, 0.08, 0.05"
            }
            trimmed.contains("dmesg") -> {
                """[   12.450120] sipa_eth0: UNISOC SIPA network interface registered
[   12.451000] sipa_eth0: Link is Up - 1000Mbps/Full - flow control rx/tx
[   15.820110] IPv6: ADDRCONF(NETDEV_CHANGE): sipa_eth0: link becomes ready
[   15.912300] bridge: br0: port 1(eth0) entered forwarding state
[   84.102390] modem: LTE carrier aggregation active: Primary Band 3 (1800MHz), Secondary Band 1 (2100MHz)"""
            }
            else -> {
                "Command executed: $trimmed (simulated output)"
            }
        }

        return CommandExecutionResult(
            command = command,
            output = output,
            durationMs = System.currentTimeMillis() - startTime,
            isSuccess = true
        )
    }

    fun injectLagSpike() {
        // Increases fail counters to simulate what happens during an actual PUBG spike
        txFail += 12
        rxFail += 8
        rxBusy += 30
    }
}

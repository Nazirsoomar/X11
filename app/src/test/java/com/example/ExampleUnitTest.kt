package com.example

import com.example.data.engine.AiSystemScannerEngine
import com.example.data.mock.UnisocRouterSimulator
import com.example.data.model.RouterConnectionConfig
import com.example.data.parser.CellularParser
import com.example.data.parser.SipaParser
import com.example.data.telnet.RouterSessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCellularParserCsq() {
        val raw = "+CSQ: 26,99\nOK"
        val (csq, rssi) = CellularParser.parseCsq(raw)
        assertEquals(26, csq)
        assertEquals(-61, rssi)
    }

    @Test
    fun testCellularParserCereg() {
        val raw = "+CEREG: 2,1,\"4E20\",\"021A5F01\",7\nOK"
        val (state, cellId, tech) = CellularParser.parseCereg(raw)
        assertEquals("Registered (Home network)", state)
        assertEquals("021A5F01", cellId)
        assertEquals("LTE (4G)", tech)
    }

    @Test
    fun testPdpContextParser() {
        val cgdcont = """+CGDCONT: 1,"IP","telenor","10.164.88.21",0,0
+CGDCONT: 2,"IPV4V6","telenor.ims","",0,0
OK"""
        val cgact = "+CGACT: 1,1\n+CGACT: 2,0\nOK"
        val cgpaddr = "+CGPADDR: 1,\"10.164.88.21\"\nOK"

        val parsed = CellularParser.parsePdpContexts(cgdcont, cgact, cgpaddr)
        assertEquals(2, parsed.size)

        val cid1 = parsed.first { it.cid == 1 }
        assertEquals("telenor", cid1.apn)
        assertEquals("10.164.88.21", cid1.ipAddress)
        assertTrue(cid1.isActivated)
        assertTrue(cid1.isTrafficCarrier)

        val cid2 = parsed.first { it.cid == 2 }
        assertEquals("telenor.ims", cid2.apn)
        assertTrue(!cid2.isActivated)
        assertTrue(!cid2.isTrafficCarrier)
    }

    @Test
    fun testSipaStatsParser() {
        val raw = """RX:
  rx_sum: 50000
  rx_cnt: 49990
  rx_fail: 5
  rx_busy: 5
  rx_evt: 0
TX:
  tx_sum: 30000
  tx_cnt: 29998
  tx_fail: 2"""

        val stats = SipaParser.parseSipaStats(raw)
        assertEquals(50000L, stats.rxSum)
        assertEquals(49990L, stats.rxCnt)
        assertEquals(5L, stats.rxFail)
        assertEquals(5L, stats.rxBusy)
        assertEquals(30000L, stats.txSum)
        assertEquals(29998L, stats.txCnt)
        assertEquals(2L, stats.txFail)
    }

    @Test
    fun testCmeErrorExplanation() {
        val explanation = CellularParser.explainCmeError(4)
        assertTrue(explanation.contains("Operation not supported"))
    }

    @Test
    fun testUnisocRouterSimulator() {
        val sim = UnisocRouterSimulator()
        val unameRes = sim.execute("uname -a")
        assertTrue(unameRes.output.contains("4.14.98"))
        assertTrue(unameRes.output.contains("W22.32.3"))
        assertTrue(unameRes.output.contains("unisoc-udx710"))

        val atRes = sim.execute("atcmd 'AT+CSQ'")
        assertTrue(atRes.output.contains("+CSQ:"))
    }

    @Test
    fun testAiSystemScanner() = runBlocking {
        val sessionManager = RouterSessionManager()
        sessionManager.connect(RouterConnectionConfig(isSimulated = true))

        val scanner = AiSystemScannerEngine(sessionManager)
        val diagnostics = scanner.runFullSystemScan()

        assertTrue("Expected at least 10 subsystems scanned", diagnostics.size >= 10)
        val cpuDiag = diagnostics.firstOrNull { it.id == "diag_cpu" }
        assertNotNull(cpuDiag)
        assertTrue(cpuDiag!!.limitations.contains("PMIC") || cpuDiag.limitations.contains("locked"))

        val gpuDiag = diagnostics.firstOrNull { it.id == "diag_gpu" }
        assertNotNull(gpuDiag)
        assertTrue(!gpuDiag!!.isSupported)

        val pdpDiag = diagnostics.firstOrNull { it.id == "diag_pdp_bonding" }
        assertNotNull(pdpDiag)
        assertTrue(pdpDiag!!.kaam.contains("data session"))
    }
}

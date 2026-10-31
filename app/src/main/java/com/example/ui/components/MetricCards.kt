package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CellularRadioMetrics
import com.example.data.model.ConnectionStatus
import com.example.data.model.SipaDeltaStats
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun ConnectionStatusBadge(
    status: ConnectionStatus,
    isSimulated: Boolean,
    modifier: Modifier = Modifier
) {
    val (bgColor, dotColor, text) = when (status) {
        ConnectionStatus.CONNECTED -> {
            if (isSimulated) Triple(Color(0xFF0F2B3D), ElectricCyan, "Simulated UDX710")
            else Triple(Color(0xFF063B2C), SuccessGreen, "Connected (192.168.0.1)")
        }
        ConnectionStatus.CONNECTING -> Triple(Color(0xFF3B2A06), WarningAmber, "Connecting...")
        ConnectionStatus.ERROR -> Triple(Color(0xFF3B0F0F), DangerRed, "Connection Error")
        ConnectionStatus.DISCONNECTED -> Triple(Color(0xFF1E293B), Color(0xFF94A3B8), "Disconnected")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, dotColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SipaRateMetricsCard(
    delta: SipaDeltaStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SIPA Interface Dynamic Delta Rates",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(title = "RX Rate", value = "${String.format("%.0f", delta.rxPps)} pps", sub = "+${delta.rxCntDelta} pkts")
                MetricColumn(title = "TX Rate", value = "${String.format("%.0f", delta.txPps)} pps", sub = "+${delta.txCntDelta} pkts")
                MetricColumn(
                    title = "TX Drops",
                    value = "${delta.txFailDelta}",
                    sub = if (delta.txFailDelta > 0) "FAIL SPIKE" else "0 drops",
                    color = if (delta.txFailDelta > 0) DangerRed else SuccessGreen
                )
                MetricColumn(
                    title = "RX Busy",
                    value = "${delta.rxBusyDelta}",
                    sub = "driver busy",
                    color = if (delta.rxBusyDelta > 0) WarningAmber else SuccessGreen
                )
            }
        }
    }
}

@Composable
fun CellularSignalCard(
    metrics: CellularRadioMetrics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cellular Radio & Signal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
                Text(
                    text = metrics.operator ?: "Telenor",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn(
                    title = "CSQ (0-31)",
                    value = metrics.csq?.toString() ?: "N/A",
                    sub = if (metrics.rssiDbm != null) "${metrics.rssiDbm} dBm" else "Probing..."
                )
                MetricColumn(
                    title = "Network Tech",
                    value = metrics.lte5gState ?: "LTE",
                    sub = metrics.band ?: "B3"
                )
                MetricColumn(
                    title = "Carrier Aggregation",
                    value = if (metrics.caStatus != null) "ACTIVE" else "Single Carrier",
                    sub = metrics.caStatus ?: "None",
                    color = if (metrics.caStatus != null) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricColumn(
    title: String,
    value: String,
    sub: String,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
        Text(text = sub, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

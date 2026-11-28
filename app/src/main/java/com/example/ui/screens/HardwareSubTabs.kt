package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.Hardware7DayTrendsCard
import com.example.ui.components.PermissionManagerPanel
import com.example.util.HardwareReportExporter
import com.example.viewmodel.OptimiserViewModel
import java.util.Locale

/**
 * Dedicated "7-Day Trends" sub-tab inside HardwareScreen showing full 7-day hardware
 * performance charts, daily bottleneck breakdown table, and CSV/PDF export actions.
 */
@Composable
fun HardwareTrendsTabView(
    viewModel: OptimiserViewModel,
    isHighContrast: Boolean
) {
    val context = LocalContext.current
    val trends by viewModel.hardware7DayTrends.collectAsState()
    val isExportingCsv by viewModel.isExportingCsv.collectAsState()
    val isExportingPdf by viewModel.isExportingPdf.collectAsState()
    val lastReportExport by viewModel.lastReportExport.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh7DayHardwareTrends()
    }

    val cardBg = if (isHighContrast) Color.Black else Color(0xFF0E1118)
    val cardBorder = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.08f)
    val textPrimary = Color.White
    val textMuted = if (isHighContrast) Color.Yellow else Color(0xFF9AA7B8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("hardware_7day_trends_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Hardware7DayTrendsCard(
            trends = trends,
            isHighContrast = isHighContrast,
            isExportingCsv = isExportingCsv,
            isExportingPdf = isExportingPdf,
            onExportCsv = {
                viewModel.exportHardwareReportCsv(context) { res ->
                    if (res != null) {
                        Toast.makeText(context, "CSV report saved: ${res.publicDisplayPath}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "CSV export failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onExportPdf = {
                viewModel.exportHardwareReportPdf(context) { res ->
                    if (res != null) {
                        Toast.makeText(context, "PDF report saved: ${res.publicDisplayPath}", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "PDF export failed.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        lastReportExport?.let { report ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2417)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "✓ ${report.format} Report Saved (${(report.byteSize / 1024).coerceAtLeast(1)} KB)",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = report.publicDisplayPath,
                            color = Color.White,
                            fontSize = 9.5.sp
                        )
                    }
                    TextButton(
                        onClick = {
                            val mime = if (report.format == "PDF") "application/pdf" else "text/csv"
                            HardwareReportExporter.shareExportedFile(context, report.file, mime)
                        }
                    ) {
                        Text("SHARE", color = Color(0xFF40C4FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 7-Day Daily Bottleneck Log Table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, cardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DAILY RESOURCE BOTTLENECK LEDGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                trends.reversed().forEach { pt ->
                    val isSevere = pt.bottleneckScore >= 68
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .background(Color(0xFF151924), RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isSevere) Color(0xFFFF5252).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = "${pt.dayLabel} (${pt.dateIso.takeLast(5)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = pt.primaryBottleneck,
                                fontSize = 9.5.sp,
                                color = if (isSevere) Color(0xFFFF8A80) else Color(0xFF00E676)
                            )
                        }
                        Text(
                            text = "CPU ${pt.peakCpuPct.toInt()}% • RAM ${pt.avgRamPct.toInt()}% • ${String.format(Locale.US, "%.1f°C", pt.peakTempC)}",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = textMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dedicated "Permissions" sub-tab inside HardwareScreen displaying the full High-Risk
 * Permission Manager for all audited installed apps.
 */
@Composable
fun PermissionsTabView(
    viewModel: OptimiserViewModel,
    isHighContrast: Boolean
) {
    val highRiskApps by viewModel.highRiskAppPermissions.collectAsState()
    val isScanning by viewModel.isScanningPermissions.collectAsState()

    val activeCardBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val activeBorderColor = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.08f)
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("hardware_permissions_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PermissionManagerPanel(
            apps = highRiskApps,
            isScanning = isScanning,
            isHighContrast = isHighContrast,
            activeCardBgColor = activeCardBgColor,
            activeBorderColor = activeBorderColor,
            activeTextColor = activeTextColor,
            activeSubTextColor = activeSubTextColor,
            onScanPermissions = { viewModel.scanHighRiskPermissions() },
            onRevokePermission = { app -> viewModel.revokeHighRiskPermission(app) }
        )
    }
}

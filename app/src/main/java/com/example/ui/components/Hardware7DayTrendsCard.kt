package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DailyHardwareTrendPoint
import java.util.Locale

/**
 * Interactive 7-Day Hardware Performance Trends data visualization built natively in Jetpack Compose.
 * Supports Multi-Series Trend Lines (CPU, RAM, Thermal) and Bottleneck Severity Bars, tap-to-inspect
 * daily drill-down tooltips, persistent bottleneck diagnosis, and one-tap CSV/PDF report export.
 */
@Composable
fun Hardware7DayTrendsCard(
    trends: List<DailyHardwareTrendPoint>,
    isHighContrast: Boolean,
    isExportingCsv: Boolean = false,
    isExportingPdf: Boolean = false,
    onExportCsv: (() -> Unit)? = null,
    onExportPdf: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    var chartMode by remember { mutableStateOf("LINES") } // "LINES" or "BARS"
    var showCpuSeries by remember { mutableStateOf(true) }
    var showRamSeries by remember { mutableStateOf(true) }
    var showTempSeries by remember { mutableStateOf(true) }
    var selectedDayIndex by remember(trends.size) {
        mutableIntStateOf((trends.size - 1).coerceAtLeast(0))
    }

    val cardBg = if (isHighContrast) Color.Black else Color(0xFF0E1118)
    val cardBorder = if (isHighContrast) Color.White else Color(0xFF2B3548)
    val textPrimary = Color.White
    val textMuted = if (isHighContrast) Color.Yellow else Color(0xFF9AA7B8)

    val cpuColor = if (isHighContrast) Color.Yellow else Color(0xFFB388FF)
    val ramColor = if (isHighContrast) Color.Cyan else Color(0xFF00E676)
    val tempColor = if (isHighContrast) Color.White else Color(0xFFFF7043)

    // Persistent Bottleneck Analysis across the 7-day dataset
    val avgCpu7d = if (trends.isNotEmpty()) trends.map { it.avgCpuPct }.average().toFloat() else 0f
    val avgRam7d = if (trends.isNotEmpty()) trends.map { it.avgRamPct }.average().toFloat() else 0f
    val maxTemp7d = trends.maxOfOrNull { it.peakTempC } ?: 0f
    val peakDay = trends.maxByOrNull { it.bottleneckScore }
    val highRamDays = trends.count { it.avgRamPct >= 75f }
    val highCpuDays = trends.count { it.peakCpuPct >= 75f }
    val hotDays = trends.count { it.peakTempC >= 38.5f }

    val persistentDiagnosis = when {
        hotDays >= 3 -> "Persistent Thermal Bottleneck: Peak temperature exceeded 38.5°C on $hotDays of 7 days. Consider Power Saving Mode & App Hibernation."
        highRamDays >= 4 -> "Persistent Memory Bottleneck: RAM usage stayed above 75% on $highRamDays of 7 days. Clear RAM or enable Auto-Clean > 80%."
        highCpuDays >= 4 -> "Persistent CPU Saturation: Peak CPU load exceeded 75% on $highCpuDays of 7 days. Audit background tasks on ${peakDay?.dayLabel ?: "peak days"}."
        else -> "Healthy 7-Day Trend: Hardware loads remained mostly balanced with isolated spikes on ${peakDay?.dayLabel ?: "recent days"}."
    }

    val selectedPoint = trends.getOrNull(selectedDayIndex.coerceIn(0, (trends.size - 1).coerceAtLeast(0)))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .testTag("hardware_7day_trends_card"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header & Chart Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "7-DAY PERFORMANCE TRENDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = textPrimary
                    )
                    Text(
                        text = "Persistent CPU, RAM & Thermal Bottleneck Analytics",
                        fontSize = 10.sp,
                        color = textMuted
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A2030))
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ChartModeChip(
                        icon = Icons.Default.ShowChart,
                        label = "Lines",
                        selected = chartMode == "LINES",
                        isHighContrast = isHighContrast,
                        onClick = { chartMode = "LINES" },
                        testTag = "trends_mode_lines"
                    )
                    ChartModeChip(
                        icon = Icons.Default.BarChart,
                        label = "Bars",
                        selected = chartMode == "BARS",
                        isHighContrast = isHighContrast,
                        onClick = { chartMode = "BARS" },
                        testTag = "trends_mode_bars"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Series Legend & Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeriesToggleChip(
                    label = "Peak CPU %",
                    color = cpuColor,
                    enabled = showCpuSeries,
                    onClick = { showCpuSeries = !showCpuSeries },
                    testTag = "series_toggle_cpu"
                )
                SeriesToggleChip(
                    label = "Avg RAM %",
                    color = ramColor,
                    enabled = showRamSeries,
                    onClick = { showRamSeries = !showRamSeries },
                    testTag = "series_toggle_ram"
                )
                SeriesToggleChip(
                    label = "Thermal",
                    color = tempColor,
                    enabled = showTempSeries,
                    onClick = { showTempSeries = !showTempSeries },
                    testTag = "series_toggle_temp"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Canvas Visualization
            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Collecting 7-day hardware telemetry...", fontSize = 11.sp, color = textMuted)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF080A10))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(162.dp)
                            .pointerInput(trends) {
                                detectTapGestures { offset ->
                                    if (trends.isNotEmpty() && size.width > 0) {
                                        val idx = ((offset.x / size.width) * trends.size)
                                            .toInt()
                                            .coerceIn(0, trends.size - 1)
                                        selectedDayIndex = idx
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            }
                            .testTag("hardware_7day_chart_canvas")
                    ) {
                        val w = size.width
                        val h = size.height
                        val padLeft = 6f
                        val padRight = 6f
                        val padTop = 10f
                        val padBottom = 10f
                        val plotW = (w - padLeft - padRight).coerceAtLeast(1f)
                        val plotH = (h - padTop - padBottom).coerceAtLeast(1f)

                        // Horizontal reference grid lines (25%, 50%, 75% threshold, 100%)
                        listOf(0.25f, 0.50f, 0.75f, 1.0f).forEach { frac ->
                            val y = padTop + plotH * (1f - frac)
                            val isThreshold = frac == 0.75f
                            drawLine(
                                color = if (isThreshold) Color(0xFFFF5252).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.08f),
                                start = Offset(padLeft, y),
                                end = Offset(w - padRight, y),
                                strokeWidth = if (isThreshold) 1.8f else 1f,
                                pathEffect = if (isThreshold) PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) else null
                            )
                        }

                        if (chartMode == "LINES") {
                            val stepX = if (trends.size > 1) plotW / (trends.size - 1) else 0f

                            // Selected day vertical highlight line
                            val selX = padLeft + selectedDayIndex.coerceIn(0, trends.size - 1) * stepX
                            drawLine(
                                color = Color.White.copy(alpha = 0.25f),
                                start = Offset(selX, padTop),
                                end = Offset(selX, padTop + plotH),
                                strokeWidth = 2f
                            )

                            fun drawMetricLine(
                                values: List<Float>,
                                color: Color,
                                fillAlpha: Float
                            ) {
                                val linePath = Path()
                                val areaPath = Path()
                                val coords = values.mapIndexed { idx, pct ->
                                    val x = padLeft + idx * stepX
                                    val y = padTop + plotH * (1f - (pct.coerceIn(0f, 100f) / 100f))
                                    Offset(x, y)
                                }

                                coords.forEachIndexed { idx, pt ->
                                    if (idx == 0) {
                                        linePath.moveTo(pt.x, pt.y)
                                        areaPath.moveTo(pt.x, padTop + plotH)
                                        areaPath.lineTo(pt.x, pt.y)
                                    } else {
                                        val prev = coords[idx - 1]
                                        val midX = (prev.x + pt.x) / 2f
                                        linePath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                                        areaPath.cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                                    }
                                }
                                if (coords.isNotEmpty()) {
                                    areaPath.lineTo(coords.last().x, padTop + plotH)
                                    areaPath.close()
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(color.copy(alpha = fillAlpha), Color.Transparent),
                                            startY = padTop,
                                            endY = padTop + plotH
                                        )
                                    )
                                }
                                drawPath(
                                    path = linePath,
                                    color = color,
                                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                coords.forEachIndexed { idx, pt ->
                                    val isSel = idx == selectedDayIndex
                                    drawCircle(
                                        color = if (isSel) Color.White else color,
                                        radius = if (isSel) 5.5.dp.toPx() else 3.2.dp.toPx(),
                                        center = pt
                                    )
                                    if (isSel) {
                                        drawCircle(
                                            color = color,
                                            radius = 3.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }

                            if (showCpuSeries) {
                                drawMetricLine(trends.map { it.peakCpuPct }, cpuColor, 0.22f)
                            }
                            if (showRamSeries) {
                                drawMetricLine(trends.map { it.avgRamPct }, ramColor, 0.18f)
                            }
                            if (showTempSeries) {
                                // Map 25°C..45°C to 0..100% scale for visual correlation
                                val normTemp = trends.map { ((it.peakTempC - 25f) / 20f * 100f).coerceIn(5f, 100f) }
                                drawMetricLine(normTemp, tempColor, 0.14f)
                            }
                        } else {
                            // BOTTLENECK SEVERITY GROUPED BARS
                            val groupWidth = plotW / trends.size
                            val barWidth = (groupWidth * 0.24f).coerceAtLeast(6f)
                            trends.forEachIndexed { idx, pt ->
                                val groupCenter = padLeft + groupWidth * idx + groupWidth / 2f
                                if (idx == selectedDayIndex) {
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.08f),
                                        topLeft = Offset(padLeft + groupWidth * idx + 2f, padTop),
                                        size = Size(groupWidth - 4f, plotH),
                                        cornerRadius = CornerRadius(8f, 8f)
                                    )
                                }
                                val bars = buildList {
                                    if (showCpuSeries) add(pt.peakCpuPct to cpuColor)
                                    if (showRamSeries) add(pt.avgRamPct to ramColor)
                                    if (showTempSeries) add((((pt.peakTempC - 25f) / 20f * 100f).coerceIn(8f, 100f)) to tempColor)
                                }
                                val totalBarsW = bars.size * barWidth + (bars.size - 1).coerceAtLeast(0) * 3f
                                var startX = groupCenter - totalBarsW / 2f
                                bars.forEach { (value, color) ->
                                    val barH = (value.coerceIn(4f, 100f) / 100f) * plotH
                                    drawRoundRect(
                                        color = color,
                                        topLeft = Offset(startX, padTop + plotH - barH),
                                        size = Size(barWidth, barH),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                    startX += barWidth + 3f
                                }
                            }
                        }
                    }

                    // Subtle Threshold Annotation
                    Text(
                        text = "75% Bottleneck Threshold",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFFF8A80),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Interactive Day Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trends.forEachIndexed { idx, pt ->
                        val isSel = idx == selectedDayIndex
                        val isBottleneckDay = pt.bottleneckScore >= 68
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSel) Color.White.copy(alpha = 0.14f) else Color.Transparent
                                )
                                .clickable {
                                    selectedDayIndex = idx
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(vertical = 6.dp)
                                .testTag("trend_day_selector_$idx"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = pt.dayLabel,
                                fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSel) Color.White else textMuted
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isBottleneckDay) Color(0xFFFF5252) else Color(0xFF00E676)
                                    )
                            )
                        }
                    }
                }

                // Selected Day Drill-Down Tooltip Card
                AnimatedVisibility(visible = selectedPoint != null) {
                    selectedPoint?.let { pt ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF151A26), RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (pt.bottleneckScore >= 68) Color(0xFFFF5252).copy(alpha = 0.55f)
                                    else Color(0xFF00E676).copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                                .testTag("trend_day_tooltip_card")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${pt.dayLabel} (${pt.dateIso})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (pt.bottleneckScore >= 68) Color(0xFFFF5252).copy(alpha = 0.2f)
                                            else Color(0xFF00E676).copy(alpha = 0.18f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = pt.primaryBottleneck.uppercase(Locale.ROOT),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (pt.bottleneckScore >= 68) Color(0xFFFF8A80) else Color(0xFF00E676)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DayStatBadge("PEAK CPU", "${pt.peakCpuPct.toInt()}%", cpuColor)
                                DayStatBadge("AVG RAM", "${pt.avgRamPct.toInt()}%", ramColor)
                                DayStatBadge("PEAK TEMP", String.format(Locale.US, "%.1f°C", pt.peakTempC), tempColor)
                                DayStatBadge("SCORE", "${pt.bottleneckScore}/100", Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            // 7-Day Persistent Bottleneck Diagnosis Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isHighContrast) Color.Black else Color(0xFF1D1418),
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        1.dp,
                        if (isHighContrast) Color.White else Color(0xFFFFAB40).copy(alpha = 0.4f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Bottleneck Diagnosis",
                    tint = if (isHighContrast) Color.Yellow else Color(0xFFFFAB40),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "7-DAY BOTTLENECK INSIGHT • AVG CPU ${avgCpu7d.toInt()}% • AVG RAM ${avgRam7d.toInt()}% • MAX ${String.format(Locale.US, "%.1f°C", maxTemp7d)}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isHighContrast) Color.Yellow else Color(0xFFFFAB40)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = persistentDiagnosis,
                        fontSize = 10.5.sp,
                        color = Color(0xFFE5E9F0)
                    )
                }
            }

            // Optional Direct CSV / PDF Export Actions
            if (onExportCsv != null && onExportPdf != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
                            onExportCsv()
                        },
                        enabled = !isExportingCsv && !isExportingPdf,
                        border = BorderStroke(1.dp, if (isHighContrast) Color.White else Color(0xFF00E676)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("trends_export_csv_button")
                    ) {
                        if (isExportingCsv) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF00E676))
                        } else {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = "Export CSV",
                                tint = if (isHighContrast) Color.White else Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXPORT CSV",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isHighContrast) Color.White else Color(0xFF00E676)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
                            onExportPdf()
                        },
                        enabled = !isExportingCsv && !isExportingPdf,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else Color(0xFFE53935),
                            contentColor = if (isHighContrast) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("trends_export_pdf_button")
                    ) {
                        if (isExportingPdf) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "Export PDF Report",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "EXPORT PDF",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartModeChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    isHighContrast: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(
                when {
                    selected && isHighContrast -> Color.White
                    selected -> Color(0xFF3949AB)
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected && isHighContrast) Color.Black else Color.White,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected && isHighContrast) Color.Black else Color.White
        )
    }
}

@Composable
private fun SeriesToggleChip(
    label: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) color.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (enabled) color.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (enabled) color else Color.Gray)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.Gray
        )
    }
}

@Composable
private fun DayStatBadge(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 8.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF9AA7B8))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = valueColor)
    }
}

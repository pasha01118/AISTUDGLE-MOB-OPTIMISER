package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.widget.LiveCpuUsageWidgetProvider

/**
 * Interactive Compose preview and Home Screen pin card for the secondary
 * Real-Time CPU Usage Home Screen widget.
 */
@Composable
fun LiveCpuWidgetCard(
    cpuUsagePercent: Float,
    cpuAvgMhz: Int,
    cpuHistory: List<Float>,
    coreCount: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(8),
    isHighContrast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isHighLoad = cpuUsagePercent >= 75f

    val animatedProgress by animateFloatAsState(
        targetValue = (cpuUsagePercent / 100f).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cpu_widget_bar"
    )

    val accentColor by animateColorAsState(
        targetValue = when {
            isHighContrast -> Color.White
            cpuUsagePercent >= 75f -> Color(0xFFFF5252)
            cpuUsagePercent >= 50f -> Color(0xFFFFAB40)
            else -> Color(0xFFB388FF)
        },
        animationSpec = tween(300),
        label = "cpu_widget_accent"
    )

    val cardBrush = if (isHighContrast) {
        Brush.linearGradient(listOf(Color.Black, Color.Black))
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF0D0A1A), Color(0xFF141028), Color(0xFF0B1C2D))
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_realtime_cpu_card"),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                listOf(accentColor.copy(alpha = 0.8f), Color(0xFF00E5FF).copy(alpha = 0.5f))
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "● LIVE CPU // TELEMETRY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.1.sp,
                    color = accentColor
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when {
                            cpuUsagePercent >= 75f -> "HIGH LOAD"
                            cpuUsagePercent >= 50f -> "MODERATE"
                            else -> "OPTIMAL"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Lottie-backed CPU threshold indicator
                HardwareThresholdLottieIcon(
                    metricType = HardwareMetricType.CPU,
                    isThresholdExceeded = isHighLoad,
                    isHighContrast = isHighContrast,
                    size = 56.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${cpuUsagePercent.toInt()}%",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier.testTag("widget_cpu_percent_readout")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$coreCount CORES • $cpuAvgMhz MHz",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFB8D4E8),
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live CPU Sparkline Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .padding(horizontal = 4.dp, vertical = 3.dp)
                    ) {
                        val pts = if (cpuHistory.size >= 2) cpuHistory else listOf(30f, cpuUsagePercent)
                        val stepX = size.width / (pts.size - 1).coerceAtLeast(1)
                        val path = Path()
                        pts.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = size.height - ((value.coerceIn(0f, 100f) / 100f) * size.height)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF), accentColor)
                            ),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated horizontal CPU load bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF), accentColor)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    LiveCpuUsageWidgetProvider.updateAllWidgets(context, cpuUsagePercent.toInt(), cpuAvgMhz)
                    val pinned = LiveCpuUsageWidgetProvider.requestPinWidget(context)
                    if (pinned) {
                        Toast.makeText(
                            context,
                            "Confirm system prompt to pin Real-Time CPU Widget!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Long-press Home Screen → Widgets → Mobile Optimiser → Real-Time CPU Monitor",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("widget_cpu_pin_to_home_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.65f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddToHomeScreen,
                    contentDescription = "Pin Real-Time CPU Widget to Home Screen",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PIN CPU MONITOR WIDGET TO HOME SCREEN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

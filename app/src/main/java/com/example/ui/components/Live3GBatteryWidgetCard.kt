package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.widget.Live3GBatteryWidgetProvider
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

/**
 * Futuristic 3G Live Animated Home Screen Widget component displaying real-time battery percentage,
 * animated cyber orbital rings, live 3G waveform telemetry, and a rapid-interaction 'Enable Saver' toggle.
 */
@Composable
fun Live3GBatteryWidgetCard(
    batteryPercentage: Int,
    temperatureCelsius: Float,
    isCharging: Boolean,
    isPowerSavingMode: Boolean,
    isHighContrast: Boolean = false,
    onToggleSaver: () -> Unit,
    showPinAction: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val infiniteTransition = rememberInfiniteTransition(label = "live_3g_cyber_transition")

    // Rotating orbital cyber telemetry ring
    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_angle"
    )

    // Counter-rotating inner micro-arc
    val counterAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_angle"
    )

    // Live 3G waveform phase shift
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Pulsing glow beacon & radar sweep
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val scanSweepFraction by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_sweep"
    )

    // Smooth spring-driven battery gauge progress
    val animatedBatterySweep by animateFloatAsState(
        targetValue = (batteryPercentage.coerceIn(0, 100) / 100f) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "battery_sweep"
    )

    // Adaptive futuristic color palette reacting smoothly to Saver state
    val primaryNeon by animateColorAsState(
        targetValue = when {
            isHighContrast -> Color.White
            isPowerSavingMode -> Color(0xFF00FF88)
            else -> Color(0xFF00E5FF)
        },
        animationSpec = tween(350),
        label = "primary_neon"
    )

    val secondaryNeon by animateColorAsState(
        targetValue = when {
            isHighContrast -> Color.Yellow
            isPowerSavingMode -> Color(0xFF00E5FF)
            else -> Color(0xFF7C4DFF)
        },
        animationSpec = tween(350),
        label = "secondary_neon"
    )

    val cardBrush = if (isHighContrast) {
        Brush.linearGradient(listOf(Color.Black, Color.Black))
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF060A14),
                Color(0xFF0B1426),
                Color(0xFF081B29)
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_3g_live_futuristic_card"),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    primaryNeon.copy(alpha = 0.85f),
                    secondaryNeon.copy(alpha = 0.45f),
                    primaryNeon.copy(alpha = 0.75f)
                )
            )
        ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBrush)
        ) {
            // Animated Futuristic Grid + Holographic Scanline Beam
            Canvas(modifier = Modifier.matchParentSize()) {
                val beamX = size.width * scanSweepFraction
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            primaryNeon.copy(alpha = 0.08f),
                            primaryNeon.copy(alpha = 0.16f),
                            primaryNeon.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startX = beamX - size.width * 0.28f,
                        endX = beamX + size.width * 0.28f
                    )
                )

                // Subtle futuristic corner telemetry accents
                val cornerLen = 14.dp.toPx()
                val strokeW = 1.5.dp.toPx()
                val cornerColor = primaryNeon.copy(alpha = 0.45f)
                drawLine(cornerColor, Offset(12f, 12f), Offset(12f + cornerLen, 12f), strokeW)
                drawLine(cornerColor, Offset(12f, 12f), Offset(12f, 12f + cornerLen), strokeW)
                drawLine(
                    cornerColor,
                    Offset(size.width - 12f, size.height - 12f),
                    Offset(size.width - 12f - cornerLen, size.height - 12f),
                    strokeW
                )
                drawLine(
                    cornerColor,
                    Offset(size.width - 12f, size.height - 12f),
                    Offset(size.width - 12f, size.height - 12f - cornerLen),
                    strokeW
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Row: 3G LIVE Beacon + Animated Signal Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(primaryNeon.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3G LIVE // NEXUS POWER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.2.sp,
                            color = primaryNeon
                        )
                    }

                    // Animated 3G Live Signal Strength Bars
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(primaryNeon.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, primaryNeon.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val barHeights = listOf(6.dp, 9.dp, 12.dp, 15.dp)
                        barHeights.forEachIndexed { idx, h ->
                            val barGlow = ((sin(wavePhase + idx * 0.9f) + 1f) / 2f).coerceIn(0.35f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(h)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(primaryNeon.copy(alpha = barGlow))
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "3G",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Body: Orbital Neon Battery Gauge + Live Telemetry & Quick 'Enable Saver' Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Futuristic Animated Orbital Battery Core Gauge
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .testTag("widget_3g_battery_gauge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val outerRadius = size.minDimension / 2f - 4.dp.toPx()
                            val innerRadius = outerRadius - 10.dp.toPx()

                            // Outer rotating cyber dashed orbital ring
                            rotate(degrees = orbitalAngle, pivot = center) {
                                drawCircle(
                                    color = primaryNeon.copy(alpha = 0.35f),
                                    radius = outerRadius,
                                    center = center,
                                    style = Stroke(
                                        width = 1.6.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                                    )
                                )
                            }

                            // Counter-rotating inner micro-segments
                            rotate(degrees = counterAngle, pivot = center) {
                                drawArc(
                                    color = secondaryNeon.copy(alpha = 0.6f),
                                    startAngle = 0f,
                                    sweepAngle = 75f,
                                    useCenter = false,
                                    topLeft = Offset(center.x - (innerRadius - 8.dp.toPx()), center.y - (innerRadius - 8.dp.toPx())),
                                    size = Size((innerRadius - 8.dp.toPx()) * 2f, (innerRadius - 8.dp.toPx()) * 2f),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = secondaryNeon.copy(alpha = 0.45f),
                                    startAngle = 180f,
                                    sweepAngle = 60f,
                                    useCenter = false,
                                    topLeft = Offset(center.x - (innerRadius - 8.dp.toPx()), center.y - (innerRadius - 8.dp.toPx())),
                                    size = Size((innerRadius - 8.dp.toPx()) * 2f, (innerRadius - 8.dp.toPx()) * 2f),
                                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // Track ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.08f),
                                radius = innerRadius,
                                center = center,
                                style = Stroke(width = 8.dp.toPx())
                            )

                            // Spring-animated battery charge neon arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(primaryNeon, secondaryNeon, primaryNeon)
                                ),
                                startAngle = -90f,
                                sweepAngle = animatedBatterySweep,
                                useCenter = false,
                                topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                                size = Size(innerRadius * 2f, innerRadius * 2f),
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else Icons.Default.Bolt,
                                contentDescription = "Battery State",
                                tint = primaryNeon,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$batteryPercentage%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                modifier = Modifier.testTag("widget_3g_battery_percent_text")
                            )
                            Text(
                                text = if (isCharging) "CHARGING" else "CELL LIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = primaryNeon.copy(alpha = 0.85f)
                            )
                        }
                    }

                    // Right Telemetry Column + Rapid-Interaction 'Enable Saver' Toggle
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val estHours = if (isPowerSavingMode) {
                            (batteryPercentage * 0.24f)
                        } else {
                            (batteryPercentage * 0.16f)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isPowerSavingMode) "ECO SHIELD ACTIVE" else "PEAK PERFORMANCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = primaryNeon
                                )
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "Est. %.1fh • %.1f°C",
                                        estHours,
                                        temperatureCelsius
                                    ),
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8D4E8)
                                )
                            }
                        }

                        // Live 3G Telemetry Energy Waveform Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                        ) {
                            val midY = size.height / 2f
                            val amp = if (isPowerSavingMode) size.height * 0.22f else size.height * 0.36f
                            val freq = if (isPowerSavingMode) 2.2f else 3.8f
                            val path = Path()
                            val steps = 48
                            for (i in 0..steps) {
                                val x = (i / steps.toFloat()) * size.width
                                val y = midY + sin((i / steps.toFloat()) * freq * 2f * PI.toFloat() + wavePhase) * amp
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(secondaryNeon, primaryNeon, secondaryNeon)
                                ),
                                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Rapid-Interaction 'Enable Saver' Glowing Pill Toggle
                        val toggleBg by animateColorAsState(
                            targetValue = if (isPowerSavingMode) primaryNeon else Color(0xFF10253D),
                            animationSpec = tween(250),
                            label = "toggle_bg"
                        )
                        val toggleContentColor by animateColorAsState(
                            targetValue = if (isPowerSavingMode) Color(0xFF041016) else primaryNeon,
                            animationSpec = tween(250),
                            label = "toggle_fg"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(toggleBg)
                                .border(
                                    width = 1.2.dp,
                                    color = if (isPowerSavingMode) Color.White.copy(alpha = 0.85f) else primaryNeon.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    } else {
                                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    }
                                    onToggleSaver()
                                }
                                .padding(horizontal = 12.dp)
                                .testTag("widget_3g_enable_saver_toggle"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatterySaver,
                                contentDescription = if (isPowerSavingMode) "Battery Saver Active" else "Enable Battery Saver",
                                tint = toggleContentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPowerSavingMode) "SAVER ACTIVE" else "ENABLE SAVER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp,
                                color = toggleContentColor
                            )
                        }
                    }
                }

                if (showPinAction) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val pinned = Live3GBatteryWidgetProvider.requestPinWidget(context)
                            if (pinned) {
                                Toast.makeText(
                                    context,
                                    "Confirm system prompt to pin 3G Live Widget to Home Screen!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Long-press Home Screen → Widgets → Mobile Optimiser → 3G Live Battery Saver",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("widget_3g_pin_to_home_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, primaryNeon.copy(alpha = 0.65f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryNeon),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddToHomeScreen,
                            contentDescription = "Pin 3G Live Widget to Home Screen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PIN 3G LIVE WIDGET TO HOME SCREEN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }
    }
}

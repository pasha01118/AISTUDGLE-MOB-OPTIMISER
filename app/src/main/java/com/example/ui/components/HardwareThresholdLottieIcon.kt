package com.example.ui.components

import androidx.annotation.RawRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import java.util.Locale

enum class HardwareMetricType(
    val label: String,
    @RawRes val lottieRawRes: Int,
    val icon: ImageVector,
    val alertColor: Color,
    val normalColor: Color,
    val alertBadgeText: String
) {
    CPU(
        label = "CPU",
        lottieRawRes = R.raw.lottie_cpu_alert,
        icon = Icons.Default.Speed,
        alertColor = Color(0xFFFF5252),
        normalColor = Color(0xFF00E676),
        alertBadgeText = "HIGH LOAD"
    ),
    RAM(
        label = "RAM",
        lottieRawRes = R.raw.lottie_ram_alert,
        icon = Icons.Default.Memory,
        alertColor = Color(0xFFE040FB),
        normalColor = Color(0xFF00E5FF),
        alertBadgeText = "PRESSURE"
    ),
    TEMP(
        label = "TEMP",
        lottieRawRes = R.raw.lottie_temp_alert,
        icon = Icons.Default.DeviceThermostat,
        alertColor = Color(0xFFFF6D00),
        normalColor = Color(0xFF4CAF50),
        alertBadgeText = "THERMAL"
    )
}

/**
 * Lottie-powered animated icon indicator for CPU, RAM, or Temp monitoring.
 * Plays its dedicated Lottie threshold animation continuously whenever usage exceeds the safe threshold.
 */
@Composable
fun HardwareThresholdLottieIcon(
    metricType: HardwareMetricType,
    isThresholdExceeded: Boolean,
    isHighContrast: Boolean = false,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(metricType.lottieRawRes)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isThresholdExceeded,
        iterations = LottieConstants.IterateForever,
        speed = if (isThresholdExceeded) 1.35f else 0.5f,
        restartOnPlay = true
    )

    val activeColor by animateColorAsState(
        targetValue = when {
            isHighContrast && isThresholdExceeded -> Color.Yellow
            isHighContrast -> Color.White
            isThresholdExceeded -> metricType.alertColor
            else -> metricType.normalColor
        },
        animationSpec = tween(300),
        label = "lottie_metric_color_${metricType.name}"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("lottie_hardware_icon_${metricType.name.lowercase(Locale.ROOT)}"),
        contentAlignment = Alignment.Center
    ) {
        if (isThresholdExceeded) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size * 0.78f)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = 0.12f))
                    .border(1.2.dp, activeColor.copy(alpha = 0.45f), CircleShape)
            )
        }

        Icon(
            imageVector = metricType.icon,
            contentDescription = "${metricType.label} status: ${if (isThresholdExceeded) "Threshold Exceeded" else "Normal"}",
            tint = activeColor,
            modifier = Modifier.size(size * 0.48f)
        )
    }
}

/**
 * Compact 3-tile Lottie hardware threshold telemetry strip for CPU, RAM, and Temp.
 */
@Composable
fun HardwareLottieThresholdStrip(
    cpuPercent: Float,
    ramPercent: Float,
    temperatureCelsius: Float,
    isHighContrast: Boolean,
    modifier: Modifier = Modifier
) {
    val cpuExceeded = cpuPercent >= 75f
    val ramExceeded = ramPercent >= 75f
    val tempExceeded = temperatureCelsius >= 38.0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hardware_lottie_threshold_strip"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThresholdMetricTile(
            metricType = HardwareMetricType.CPU,
            valueText = "${cpuPercent.toInt()}%",
            thresholdText = "≥75%",
            isExceeded = cpuExceeded,
            isHighContrast = isHighContrast,
            modifier = Modifier.weight(1f)
        )
        ThresholdMetricTile(
            metricType = HardwareMetricType.RAM,
            valueText = "${ramPercent.toInt()}%",
            thresholdText = "≥75%",
            isExceeded = ramExceeded,
            isHighContrast = isHighContrast,
            modifier = Modifier.weight(1f)
        )
        ThresholdMetricTile(
            metricType = HardwareMetricType.TEMP,
            valueText = String.format(Locale.getDefault(), "%.1f°C", temperatureCelsius),
            thresholdText = "≥38°C",
            isExceeded = tempExceeded,
            isHighContrast = isHighContrast,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThresholdMetricTile(
    metricType: HardwareMetricType,
    valueText: String,
    thresholdText: String,
    isExceeded: Boolean,
    isHighContrast: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = when {
        isHighContrast && isExceeded -> Color.Yellow
        isHighContrast -> Color.White
        isExceeded -> metricType.alertColor
        else -> metricType.normalColor
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHighContrast) Color.Black
                else if (isExceeded) accent.copy(alpha = 0.14f) else Color(0xFF14151E)
            )
            .border(
                width = if (isExceeded) 1.5.dp else 1.dp,
                color = if (isHighContrast) Color.White else accent.copy(alpha = if (isExceeded) 0.75f else 0.25f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HardwareThresholdLottieIcon(
            metricType = metricType,
            isThresholdExceeded = isExceeded,
            isHighContrast = isHighContrast,
            size = 46.dp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = metricType.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color.LightGray
        )
        Text(
            text = valueText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = if (isExceeded) metricType.alertBadgeText else "OK ($thresholdText)",
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = accent
        )
    }
}

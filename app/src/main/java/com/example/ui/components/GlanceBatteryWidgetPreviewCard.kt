package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.widget.GlanceBatteryWidgetHelper
import java.util.Locale

/**
 * Interactive preview and Home Screen pin card for the Jetpack Compose Glance
 * Battery Saver widget (GlanceBatterySaverWidget).
 */
@Composable
fun GlanceBatteryWidgetPreviewCard(
    batteryPercentage: Int,
    temperatureCelsius: Float,
    isCharging: Boolean,
    isPowerSavingMode: Boolean,
    isHighContrast: Boolean,
    onToggleSaver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val accentColor = when {
        isHighContrast -> Color.White
        batteryPercentage <= 20 -> Color(0xFFFF5252)
        isPowerSavingMode -> Color(0xFF00E676)
        else -> Color(0xFF00E5FF)
    }
    val filledSegments = (batteryPercentage / 10).coerceIn(1, 10)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("widget_glance_battery_preview_card"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, if (isHighContrast) Color.White else accentColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else Color(0xFF0B131E)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Compose Glance Battery Widget",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JETPACK GLANCE • POWER MONITOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isHighContrast) Color.Yellow else Color(0xFF90CAF9)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isHighContrast) Color.Black else accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isPowerSavingMode) "SAVER ON" else "PERFORMANCE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$batteryPercentage%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%s • %.1f°C",
                            if (isCharging) "Charging" else "Live Cell",
                            temperatureCelsius
                        ),
                        fontSize = 11.sp,
                        color = if (isHighContrast) Color.Yellow else Color(0xFFB0BEC5)
                    )
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        onToggleSaver()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else if (isPowerSavingMode) Color(0xFF00E676) else Color(0xFF1E3A5F),
                        contentColor = if (isHighContrast || isPowerSavingMode) Color(0xFF061610) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("widget_glance_toggle_saver_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = "Toggle Battery Saver",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPowerSavingMode) "SAVER: ON" else "ENABLE SAVER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 10-Segment Battery Gauge mirroring the Glance AppWidget layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF132030))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (seg in 1..10) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (seg <= filledSegments) accentColor else Color(0xFF233245))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val pinned = GlanceBatteryWidgetHelper.requestPinWidget(context)
                    if (pinned) {
                        Toast.makeText(context, "Confirm Glance Battery Widget on your home screen!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Long-press your home screen -> Widgets -> Glance Battery Saver", Toast.LENGTH_LONG).show()
                    }
                },
                border = BorderStroke(1.dp, if (isHighContrast) Color.White else accentColor.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("widget_glance_pin_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddToHomeScreen,
                    contentDescription = "Pin Compose Glance Widget",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PIN COMPOSE GLANCE WIDGET TO HOME SCREEN",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
            }
        }
    }
}

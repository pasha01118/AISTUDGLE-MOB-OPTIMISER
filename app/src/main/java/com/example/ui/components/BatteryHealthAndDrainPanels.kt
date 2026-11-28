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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.repository.AppDrainCandidate
import com.example.data.repository.BatteryInfo
import com.example.viewmodel.DynamicPowerSuggestion
import java.util.Locale

/**
 * Detailed Battery Health Status panel integrated inside the Battery Management Dashboard.
 * Displays cycle count, capacity retention, estimated remaining lifespan, voltage & chemistry.
 */
@Composable
fun DetailedBatteryHealthPanel(
    battery: BatteryInfo,
    isHighContrast: Boolean,
    activeTextColor: Color,
    activeSubTextColor: Color,
    modifier: Modifier = Modifier
) {
    val cycleRatio = (battery.cycleCount.toFloat() / battery.maxRatedCycles.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)
    val years = battery.estimatedLifespanMonths / 12
    val months = battery.estimatedLifespanMonths % 12
    val lifespanText = buildString {
        if (years > 0) append("${years}y ")
        append("${months}m est.")
    }

    val healthAccent = when {
        isHighContrast -> Color.White
        battery.healthCapacityPercent >= 88 -> Color(0xFF00E676)
        battery.healthCapacityPercent >= 80 -> Color(0xFFFFAB40)
        else -> Color(0xFFFF5252)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isHighContrast) Color.White else healthAccent.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
            .testTag("detailed_battery_health_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = "Battery Health Diagnostics",
                    tint = healthAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "DETAILED BATTERY HEALTH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = activeTextColor
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(healthAccent.copy(alpha = 0.16f))
                    .border(1.dp, healthAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "${battery.healthCapacityPercent}% • ${battery.health.uppercase(Locale.ROOT)}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = healthAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Metric Summary Tiles: Cycle Count, Est. Lifespan, Voltage/Chemistry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HealthStatTile(
                title = "CYCLE COUNT",
                value = "${battery.cycleCount}",
                subtitle = "of ${battery.maxRatedCycles} rated",
                accentColor = healthAccent,
                isHighContrast = isHighContrast,
                textColor = activeTextColor,
                subTextColor = activeSubTextColor,
                modifier = Modifier
                    .weight(1f)
                    .testTag("battery_cycle_count_tile")
            )
            HealthStatTile(
                title = "EST. LIFESPAN",
                value = lifespanText,
                subtitle = "to 80% wear",
                accentColor = if (isHighContrast) Color.Yellow else Color(0xFF00E5FF),
                isHighContrast = isHighContrast,
                textColor = activeTextColor,
                subTextColor = activeSubTextColor,
                modifier = Modifier
                    .weight(1f)
                    .testTag("battery_lifespan_tile")
            )
            HealthStatTile(
                title = "CELL SPEC",
                value = String.format(Locale.getDefault(), "%.2fV", battery.voltageMv / 1000f),
                subtitle = battery.technology,
                accentColor = if (isHighContrast) Color.White else Color(0xFFB388FF),
                isHighContrast = isHighContrast,
                textColor = activeTextColor,
                subTextColor = activeSubTextColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cycle Wear Progression Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Charge Cycle Wear Progression",
                fontSize = 10.sp,
                color = activeSubTextColor
            )
            Text(
                text = "${(cycleRatio * 100).toInt()}% of rated lifecycle",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = healthAccent
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { cycleRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = healthAccent,
            trackColor = if (isHighContrast) Color.DarkGray else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun HealthStatTile(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    isHighContrast: Boolean,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, if (isHighContrast) Color.White else accentColor.copy(alpha = 0.22f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = subTextColor
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = accentColor
        )
        Text(
            text = subtitle,
            fontSize = 8.sp,
            color = textColor.copy(alpha = 0.75f)
        )
    }
}

/**
 * Dynamic Power Profile automatic recommendation banner triggered when Battery < 20%
 * or Hardware Monitor detects high temperature (>= 38.5°C).
 */
@Composable
fun DynamicPowerProfileSuggestionBanner(
    suggestion: DynamicPowerSuggestion,
    isHighContrast: Boolean,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val isThermal = suggestion.triggerType == "HIGH_TEMP"
    val accent = when {
        isHighContrast -> Color.Yellow
        isThermal -> Color(0xFFFF6D00)
        else -> Color(0xFFFF5252)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_power_suggestion_card"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, if (isHighContrast) Color.White else accent.copy(alpha = 0.75f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else Color(0xFF1F1110)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isThermal) Icons.Default.DeviceThermostat else Icons.Default.BatteryAlert,
                    contentDescription = "Dynamic Power Profile Alert",
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DYNAMIC POWER PROFILE SUGGESTION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = accent
                    )
                    Text(
                        text = suggestion.reasonTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = suggestion.reasonDetail,
                fontSize = 11.sp,
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("dismiss_dynamic_power_profile_button")
                ) {
                    Text("DISMISS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                        onAccept()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else accent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("accept_dynamic_power_profile_button"),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SWITCH TO POWER SAVING", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

/**
 * Scans installed apps for high battery consumption patterns and suggests App Hibernation
 * for draining background processes.
 */
@Composable
fun AppBatteryDrainHibernationCard(
    candidates: List<AppDrainCandidate>,
    isScanning: Boolean,
    isHighContrast: Boolean,
    activeCardBgColor: Color,
    activeBorderColor: Color,
    activeTextColor: Color,
    activeSubTextColor: Color,
    onScanApps: () -> Unit,
    onHibernateApp: (String) -> Unit,
    onHibernateAll: () -> Unit,
    isBgScannerEnabled: Boolean = true,
    lastBgScanSummary: String = "Background scanner ready",
    onToggleBgScanner: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val activeDrainers = candidates.count { !it.isHibernated }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
            .testTag("app_battery_drain_hibernation_card"),
        colors = CardDefaults.cardColors(containerColor = activeCardBgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = "App Hibernation Scanner",
                        tint = if (isHighContrast) Color.White else Color(0xFFFF7043),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BACKGROUND DRAIN SCANNER & HIBERNATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                            color = activeTextColor
                        )
                        Text(
                            text = if (candidates.isEmpty()) {
                                "Background worker scans for high-battery-consuming apps"
                            } else {
                                "$activeDrainers draining processes ready for hibernation"
                            },
                            fontSize = 10.sp,
                            color = activeSubTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Automated Background Scanner Worker Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
                    .border(
                        1.dp,
                        if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBgScannerEnabled) "AUTO BACKGROUND SCANNER: ACTIVE (30m)" else "AUTO BACKGROUND SCANNER: PAUSED",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isBgScannerEnabled && !isHighContrast) Color(0xFF00E676) else activeTextColor
                    )
                    Text(
                        text = lastBgScanSummary,
                        fontSize = 9.5.sp,
                        color = activeSubTextColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isBgScannerEnabled,
                    onCheckedChange = { enabled ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleBgScanner(enabled)
                    },
                    modifier = Modifier.testTag("bg_drain_scanner_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onScanApps()
                    },
                    enabled = !isScanning,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("scan_battery_drain_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SCANNING...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SCAN DRAINING APPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (activeDrainers > 0) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            }
                            onHibernateAll()
                            Toast.makeText(context, "Hibernated $activeDrainers draining background apps!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("hibernate_all_draining_apps_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HIBERNATE ALL ($activeDrainers)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (candidates.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    candidates.forEach { app ->
                        val riskColor = when {
                            isHighContrast -> Color.Yellow
                            app.isHibernated -> Color(0xFF4CAF50)
                            app.riskLevel == "CRITICAL" -> Color(0xFFFF5252)
                            else -> Color(0xFFFFAB40)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isHighContrast) Color.Black
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                                )
                                .border(
                                    1.dp,
                                    if (isHighContrast) Color.White else riskColor.copy(alpha = 0.35f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = app.appName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeTextColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(riskColor.copy(alpha = 0.18f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (app.isHibernated) "HIBERNATED" else "${app.riskLevel} DRAIN",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = riskColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(
                                        Locale.getDefault(),
                                        "%.1f%%/hr • %d wakeups/hr • %d MB RAM",
                                        app.drainPercentPerHour,
                                        app.wakeupsPerHour,
                                        app.memoryMB
                                    ),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = activeSubTextColor
                                )
                                Text(
                                    text = app.drainReason,
                                    fontSize = 9.sp,
                                    color = activeSubTextColor.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (app.isHibernated) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Hibernated",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ASLEEP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onHibernateApp(app.packageName)
                                        Toast.makeText(context, "Hibernated ${app.appName} to stop battery drain", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isHighContrast) Color.White else riskColor,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("hibernate_app_${app.packageName}"),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("HIBERNATE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BackgroundApp
import com.example.viewmodel.OptimiserViewModel
import android.app.TimePickerDialog
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import com.example.ui.components.AppBatteryDrainHibernationCard
import com.example.ui.components.DetailedBatteryHealthPanel
import com.example.ui.components.DynamicPowerProfileSuggestionBanner
import com.example.ui.components.Live3GBatteryWidgetCard
import com.example.ui.components.PermissionManagerPanel
import com.example.worker.JunkCleanupPrefs
import com.example.worker.JunkCleanupScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: OptimiserViewModel,
    onNavigateToLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val storage by viewModel.storageInfo.collectAsState()
    val battery by viewModel.batteryInfo.collectAsState()
    val backgroundApps by viewModel.backgroundApps.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isCleaning by viewModel.isCleaning.collectAsState()
    val statusMsg by viewModel.optimizerStatusMessage.collectAsState()
    val thermalAlert by viewModel.thermalAlert.collectAsState()
    val batteryAlert by viewModel.batteryAlert.collectAsState()
    val showJunkNotificationPrompt by viewModel.showJunkNotificationPrompt.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isPowerSavingMode by viewModel.isPowerSavingMode.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()
    val isAuditing by viewModel.isAuditing.collectAsState()
    val logs by viewModel.activityLogs.collectAsState()

    // Persisted safe manual junk checkboxes & daily WorkManager schedule
    val tickSysCache by viewModel.tickSysCache.collectAsState()
    val tickObsoleteLogs by viewModel.tickObsoleteLogs.collectAsState()
    val tickApkRemnants by viewModel.tickApkRemnants.collectAsState()
    val tickTempThumbnails by viewModel.tickTempThumbnails.collectAsState()
    val isScheduledJunkCleanEnabled by viewModel.isScheduledJunkCleanEnabled.collectAsState()
    val scheduledCleanHour by viewModel.scheduledCleanHour.collectAsState()
    val scheduledCleanMinute by viewModel.scheduledCleanMinute.collectAsState()
    val lastScheduledCleanSummary by viewModel.lastScheduledCleanSummary.collectAsState()

    // Dynamic Power Profiles & App Battery Drain Hibernation states
    val isDynamicProfilesEnabled by viewModel.isDynamicProfilesEnabled.collectAsState()
    val dynamicPowerSuggestion by viewModel.dynamicPowerSuggestion.collectAsState()
    val drainScanResults by viewModel.drainScanResults.collectAsState()
    val isScanningAppDrain by viewModel.isScanningAppDrain.collectAsState()
    val isBgDrainScannerEnabled by viewModel.isBgDrainScannerEnabled.collectAsState()
    val lastBgDrainScanSummary by viewModel.lastBgDrainScanSummary.collectAsState()
    val highRiskAppPermissions by viewModel.highRiskAppPermissions.collectAsState()
    val isScanningPermissions by viewModel.isScanningPermissions.collectAsState()

    // Color schema adaptive to High Contrast accessibility modifier
    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeCardBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Core Title Tag with theme toggle
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MOBILE OPTIMISER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        ),
                        color = activeTextColor
                    )
                    Text(
                        text = "Real-time AI Assistive Diagnostics",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeSubTextColor
                    )
                }
                
                IconButton(
                    onClick = { viewModel.toggleDarkTheme() },
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark/Light Mode",
                        tint = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Warning alerts list
        item {
            AnimatedVisibility(
                visible = thermalAlert != null ||
                    batteryAlert != null ||
                    showJunkNotificationPrompt ||
                    dynamicPowerSuggestion != null
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dynamicPowerSuggestion?.let { suggestion ->
                        DynamicPowerProfileSuggestionBanner(
                            suggestion = suggestion,
                            isHighContrast = isHighContrast,
                            onAccept = { viewModel.acceptDynamicPowerSuggestion() },
                            onDismiss = { viewModel.dismissDynamicPowerSuggestion() }
                        )
                    }
                    thermalAlert?.let {
                        AlertCard(
                            message = it,
                            icon = Icons.Default.Warning,
                            color = Color.Red,
                            isHighContrast = isHighContrast
                        )
                    }
                    batteryAlert?.let {
                        AlertCard(
                            message = it,
                            icon = Icons.Default.BatteryAlert,
                            color = Color(0xFFE65100), // Dark orange
                            isHighContrast = isHighContrast
                        )
                    }
                    if (showJunkNotificationPrompt) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isHighContrast) Color.White else Color(0xFF00E676).copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .testTag("junk_alert_prompt_card"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighContrast) Color.Black else Color(0xFF0D1B13)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Junk Storage warning icon",
                                        tint = if (isHighContrast) Color.White else Color(0xFF00E676),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "HIGH JUNK STORAGE TRASH ALERT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isHighContrast) Color.White else Color(0xFF00E676),
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "System junk has exceeded 200 MB (current: ${storage.junkMB} MB)! We highly recommend running clean up vectors to prevent CPU stress.",
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextButton(onClick = { viewModel.dismissJunkPrompt() }) {
                                        Text("DISMISS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.cleanJunkFiles() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isHighContrast) Color.White else Color(0xFF00E676),
                                            contentColor = if (isHighContrast) Color.Black else Color.Black
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp).testTag("junk_alert_clean_now_button"),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("CLEAN NOW", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // QUICK-ACCESS OPERATIONS BAR (with System Haptic Feedback)
        item {
            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current
            val view = LocalView.current

            fun performActionHaptic(isConfirm: Boolean = false) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isConfirm && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                } else {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("dashboard_quick_actions_card"),
                colors = CardDefaults.cardColors(
                    containerColor = activeCardBgColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUICK-ACCESS ACTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeTextColor,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "HAPTIC ENABLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeSubTextColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Clear RAM Button
                        Button(
                            onClick = {
                                performActionHaptic(isConfirm = false)
                                viewModel.clearRam {
                                    performActionHaptic(isConfirm = true)
                                    Toast.makeText(context, "RAM quick-cleared successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("dashboard_quick_clear_ram"),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = "Clear RAM",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear RAM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Battery Saver Button
                        Button(
                            onClick = {
                                performActionHaptic(isConfirm = true)
                                viewModel.togglePowerSavingMode()
                                Toast.makeText(context, "Battery Saver mode toggled!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHighContrast) Color.White else (if (isPowerSavingMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                                contentColor = if (isHighContrast) Color.Black else (if (isPowerSavingMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("dashboard_quick_battery_saver"),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatterySaver,
                                contentDescription = "Battery Saver",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Battery Saver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Deep Clean Button
                        Button(
                            onClick = {
                                performActionHaptic(isConfirm = false)
                                viewModel.cleanJunkFiles {
                                    performActionHaptic(isConfirm = true)
                                    Toast.makeText(context, "System junk cache deep-cleaned successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("dashboard_quick_deep_clean"),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Deep Clean",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deep Clean", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // FUTURISTIC 3G LIVE BATTERY & QUICK SAVER WIDGET
        item {
            Live3GBatteryWidgetCard(
                batteryPercentage = battery.percentage,
                temperatureCelsius = battery.temperatureCelsius,
                isCharging = battery.isCharging,
                isPowerSavingMode = isPowerSavingMode,
                isHighContrast = isHighContrast,
                onToggleSaver = { viewModel.togglePowerSavingMode() },
                showPinAction = true
            )
        }

        // REAL-TIME SYSTEM ACTIVITY LOG VIEWER WIDGET
        item {
            var selectedFilterType by remember { mutableStateOf("ALL") } // ALL, USER, AI
            val timeFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
            val aiActivityTypes = remember {
                setOf(
                    "AUDIT",
                    "JAMUN_REPAIR",
                    "SENTINEL_FIX",
                    "SENTINEL_ALERT",
                    "AGENT_LEARNING",
                    "AGENT_HEALING",
                    "SKILL_ADDED",
                    "SKILL_UPGRADED",
                    "MEMORY_REGISTERED",
                    "MEMORY_PURGED",
                    "MONITOR",
                    "THERMAL_CALIB"
                )
            }
            fun isAiEntry(type: String, description: String): Boolean {
                return type.uppercase(Locale.ROOT) in aiActivityTypes ||
                    description.contains("AI", ignoreCase = true) ||
                    description.contains("Jamun", ignoreCase = true) ||
                    description.contains("Sentinel", ignoreCase = true) ||
                    description.contains("Agent", ignoreCase = true)
            }

            val filteredLogs = remember(logs, selectedFilterType) {
                when (selectedFilterType) {
                    "USER" -> logs.filter { !isAiEntry(it.activityType, it.description) }
                    "AI" -> logs.filter { isAiEntry(it.activityType, it.description) }
                    else -> logs
                }.take(5)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .testTag("dashboard_activity_logs_card"),
                colors = CardDefaults.cardColors(
                    containerColor = activeCardBgColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                                imageVector = Icons.Default.History,
                                contentDescription = "Real-time activity logs",
                                tint = activeTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVITY LOGS (${logs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeTextColor,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.8.sp
                            )
                        }

                        // Filter selectors row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("ALL", "USER", "AI").forEach { type ->
                                val isSelected = selectedFilterType == type
                                val tagBg = if (isSelected) {
                                    if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                }
                                val tagText = if (isSelected) {
                                    if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                } else {
                                    activeTextColor
                                }

                                Box(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(tagBg, RoundedCornerShape(6.dp))
                                        .border(
                                            1.dp,
                                            if (isHighContrast || isSelected) {
                                                if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            },
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable { selectedFilterType = type }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("log_filter_$type"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = type,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tagText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredLogs.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No logs",
                                tint = activeSubTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "No recorded logs match '$selectedFilterType' yet. Run a scan, cleanup, or AI audit to populate live activity.",
                                fontSize = 11.sp,
                                color = activeSubTextColor
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            filteredLogs.forEach { log ->
                                val isAiLog = isAiEntry(log.activityType, log.description)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isHighContrast) Color.White else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isAiLog) Icons.Default.AutoAwesome else Icons.Default.Done,
                                        contentDescription = if (isAiLog) "AI Action" else "User Action",
                                        tint = if (isAiLog) Color(0xFF81D4FA) else Color(0xFFC5E1A5),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.description,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = activeTextColor
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Category: ${log.activityType}",
                                                fontSize = 8.sp,
                                                color = activeSubTextColor
                                            )
                                            Text(
                                                text = timeFormatter.format(Date(log.timestamp)),
                                                fontSize = 8.sp,
                                                color = activeSubTextColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onNavigateToLogs,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("dashboard_view_all_logs_button")
                        ) {
                            Text(
                                text = "VIEW FULL HISTORY & BACKUPS →",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // AI Sentinel Shield Status Dashboard Widget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp, 
                        if (isHighContrast) Color.White else Color(0xFFE53935).copy(alpha = 0.3f), 
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isHighContrast) Color.Black else Color(0xFF1C1315)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1.0f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SENTINEL IMMUNE DAEMON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Actively shield Binder memory & inspect for broken dispatcher input channel leaks.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { viewModel.setSentinelOpen(true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("dashboard_sentinel_btn")
                    ) {
                        Text("DIAGNOSE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // System Gauges Card (Storage, Memory, Temperature)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = activeCardBgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SYSTEM METRIC MONITOR",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = activeTextColor,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Storage progress
                        val usedPct = if (storage.totalMB > 0) (storage.usedMB.toFloat() / storage.totalMB.toFloat()) else 0.5f
                        val animatedUsedPct by animateFloatAsState(targetValue = usedPct, label = "storage_anim")
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                                CircularProgressIndicator(
                                    progress = { animatedUsedPct },
                                    modifier = Modifier.size(70.dp),
                                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    trackColor = if (isHighContrast) Color.Gray else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                    strokeWidth = 6.dp
                                )
                                Text(
                                    text = "${(usedPct * 100).toInt()}%",
                                    fontWeight = FontWeight.Bold,
                                    color = activeTextColor,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Storage Used", style = MaterialTheme.typography.bodySmall, color = activeTextColor)
                            Text(
                                "${String.format(Locale.getDefault(), "%.1f", storage.usedMB / 1024f)} / ${String.format(Locale.getDefault(), "%.1f", storage.totalMB / 1024f)} GB",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeSubTextColor
                            )
                        }

                        // Temperature Gauge
                        val temp = battery.temperatureCelsius
                        val tempColor by animateColorAsState(
                            targetValue = when {
                                isHighContrast -> Color.White
                                temp > 40f -> Color.Red
                                temp > 36f -> Color(0xFFFBC02D) // Gold Amber
                                else -> Color(0xFF4CAF50) // Emerald Green
                            }, label = "temp_color"
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(tempColor.copy(alpha = 0.15f), CircleShape)
                                    .border(2.dp, tempColor, CircleShape)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.DeviceThermostat,
                                        contentDescription = "Temperature",
                                        tint = tempColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f", temp)}°C",
                                        fontWeight = FontWeight.Bold,
                                        color = activeTextColor,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Temp", style = MaterialTheme.typography.bodySmall, color = activeTextColor)
                            Text(
                                text = when {
                                    temp > 40f -> "HOT SPIKE"
                                    temp > 36f -> "WARM"
                                    else -> "COOL"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = tempColor
                            )
                        }

                        // Battery Capacity
                        val batColor = if (isHighContrast) Color.White else when {
                            battery.percentage > 50 -> Color(0xFF4CAF50)
                            battery.percentage > 20 -> Color(0xFFFBC02D)
                            else -> Color.Red
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(batColor.copy(alpha = 0.15f), CircleShape)
                                    .border(2.dp, batColor, CircleShape)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${battery.percentage}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = activeTextColor
                                    )
                                    Text(
                                        text = battery.health,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = activeSubTextColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Battery Status", style = MaterialTheme.typography.bodySmall, color = activeTextColor)
                            Text(
                                text = if (battery.isCharging) "Charging" else "Unplugged",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeSubTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status output Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, if (isHighContrast) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Status",
                                tint = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                statusMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTextColor
                            )
                        }
                    }
                }
            }
        }

        // ----------------- BATTERY PERFORMANCE DASHBOARD -----------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
                    .testTag("battery_performance_dashboard"),
                colors = CardDefaults.cardColors(containerColor = activeCardBgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatterySaver,
                            contentDescription = "Battery Optimization Dashboard",
                            tint = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "BATTERY PERFORMANCE ENGINE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = activeTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Calibrate and manage how hardware resources are allocated to maximize power endurance or raw system performance parameters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeSubTextColor
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPowerSavingMode) "POWER-SAVING ACTIVE" else "HIGH-PERFORMANCE ACTIVE",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isHighContrast) Color.White else if (isPowerSavingMode) Color(0xFF4CAF50) else Color(0xFFFF5252)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPowerSavingMode) 
                                    "Extends battery backup; curbs background processes." 
                                    else "Dynamic peak performance; unlocks unrestricted clocks.",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeSubTextColor
                            )
                        }

                        Switch(
                            checked = !isPowerSavingMode,
                            onCheckedChange = { viewModel.togglePowerSavingMode() },
                            modifier = Modifier.testTag("performance_mode_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isHighContrast) Color.Black else Color.White,
                                checkedTrackColor = if (isHighContrast) Color.White else Color(0xFFFF5252),
                                uncheckedThumbColor = if (isHighContrast) Color.White else Color.White,
                                uncheckedTrackColor = if (isHighContrast) Color.DarkGray else Color(0xFF4CAF50)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Detailed Battery Health Status Display (Cycle Count & Estimated Lifespan)
                    DetailedBatteryHealthPanel(
                        battery = battery,
                        isHighContrast = isHighContrast,
                        activeTextColor = activeTextColor,
                        activeSubTextColor = activeSubTextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic Power Profiles Auto-Suggestion Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DYNAMIC POWER PROFILES",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = activeTextColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Auto-suggest Power Saving Mode when battery < 20% or hardware monitor detects temp >= 38.5°C.",
                                style = MaterialTheme.typography.labelSmall,
                                color = activeSubTextColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isDynamicProfilesEnabled,
                            onCheckedChange = { viewModel.toggleDynamicPowerProfiles(it) },
                            modifier = Modifier.testTag("dynamic_power_profiles_switch")
                        )
                    }
                }
            }
        }

        // ----------------- APP BATTERY DRAIN SCANNER & HIBERNATION -----------------
        item {
            AppBatteryDrainHibernationCard(
                candidates = drainScanResults,
                isScanning = isScanningAppDrain,
                isHighContrast = isHighContrast,
                activeCardBgColor = activeCardBgColor,
                activeBorderColor = activeBorderColor,
                activeTextColor = activeTextColor,
                activeSubTextColor = activeSubTextColor,
                onScanApps = { viewModel.triggerBgDrainScanNow() },
                onHibernateApp = { packageName -> viewModel.hibernateDrainingApp(packageName) },
                onHibernateAll = { viewModel.hibernateAllDrainingApps() },
                isBgScannerEnabled = isBgDrainScannerEnabled,
                lastBgScanSummary = lastBgDrainScanSummary,
                onToggleBgScanner = { viewModel.toggleBgDrainScanner(it) }
            )
        }

        // ----------------- HIGH-RISK PERMISSION MANAGER -----------------
        item {
            PermissionManagerPanel(
                apps = highRiskAppPermissions,
                isScanning = isScanningPermissions,
                isHighContrast = isHighContrast,
                activeCardBgColor = activeCardBgColor,
                activeBorderColor = activeBorderColor,
                activeTextColor = activeTextColor,
                activeSubTextColor = activeSubTextColor,
                onScanPermissions = { viewModel.scanHighRiskPermissions() },
                onRevokePermission = { app -> viewModel.revokeHighRiskPermission(app) },
                maxVisibleItems = 6
            )
        }

        // ----------------- JUNK FILES CLEANUP & SCHEDULED SAFE WORKER -----------------
        item {
            val context = LocalContext.current
            val haptic = LocalHapticFeedback.current
            val view = LocalView.current

            var simulationProgress by remember { mutableStateOf(0f) }
            var activePhase by remember { mutableStateOf("") }
            var isSimRunning by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            val tickedCount = listOf(tickSysCache, tickObsoleteLogs, tickApkRemnants, tickTempThumbnails).count { it }
            val formattedScheduleTime = JunkCleanupScheduler.formatTime12Hour(scheduledCleanHour, scheduledCleanMinute)
            val nextRunText = JunkCleanupScheduler.getNextRunDescription(scheduledCleanHour, scheduledCleanMinute)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
                    .testTag("junk_cleanup_dashboard"),
                colors = CardDefaults.cardColors(containerColor = activeCardBgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "Safe Junk File Cleanup",
                            tint = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SAFE JUNK FILE CLEANUP & SCHEDULER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = activeTextColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Manually tick the safe junk categories below. Both manual cleanup and the recurring background worker will ONLY clean your ticked categories.",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeSubTextColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Persisted Safe Manual Category Checklist
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSimRunning) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_SYS_CACHE, !tickSysCache)
                                }
                                .testTag("junk_checkbox_sys_cache")
                        ) {
                            Checkbox(
                                checked = tickSysCache,
                                onCheckedChange = {
                                    if (!isSimRunning) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_SYS_CACHE, it)
                                    }
                                },
                                enabled = !isSimRunning,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    checkmarkColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Column {
                                Text("System App Cache", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = activeTextColor)
                                Text("Temp variables & UI cache: ~142 MB", style = MaterialTheme.typography.labelSmall, color = activeSubTextColor)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSimRunning) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_OBSOLETE_LOGS, !tickObsoleteLogs)
                                }
                                .testTag("junk_checkbox_obsolete_logs")
                        ) {
                            Checkbox(
                                checked = tickObsoleteLogs,
                                onCheckedChange = {
                                    if (!isSimRunning) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_OBSOLETE_LOGS, it)
                                    }
                                },
                                enabled = !isSimRunning,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    checkmarkColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Column {
                                Text("Obsolete Sentinel Logs", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = activeTextColor)
                                Text("Older crash dumps & diagnostic audits: ~68 MB", style = MaterialTheme.typography.labelSmall, color = activeSubTextColor)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSimRunning) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_APK_REMNANTS, !tickApkRemnants)
                                }
                                .testTag("junk_checkbox_apk_remnants")
                        ) {
                            Checkbox(
                                checked = tickApkRemnants,
                                onCheckedChange = {
                                    if (!isSimRunning) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_APK_REMNANTS, it)
                                    }
                                },
                                enabled = !isSimRunning,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    checkmarkColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Column {
                                Text("Residual Installer Remnants", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = activeTextColor)
                                Text("Expired update packages: ~118 MB", style = MaterialTheme.typography.labelSmall, color = activeSubTextColor)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSimRunning) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_TEMP_THUMBNAILS, !tickTempThumbnails)
                                }
                                .testTag("junk_checkbox_temp_thumbnails")
                        ) {
                            Checkbox(
                                checked = tickTempThumbnails,
                                onCheckedChange = {
                                    if (!isSimRunning) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setJunkCategoryTicked(JunkCleanupPrefs.KEY_TICK_TEMP_THUMBNAILS, it)
                                    }
                                },
                                enabled = !isSimRunning,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    checkmarkColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            Column {
                                Text("Temp Thumbnails & Cache", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = activeTextColor)
                                Text("Safe temporary preview & ad buffers: ~54 MB", style = MaterialTheme.typography.labelSmall, color = activeSubTextColor)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- RECURRING BACKGROUND WORKMANAGER SCHEDULE PANEL ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                            .testTag("scheduled_junk_clean_panel")
                    ) {
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
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Scheduled Background Cleanup",
                                    tint = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "AUTO-CLEAN SCHEDULE (DAILY)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = activeTextColor
                                    )
                                    Text(
                                        text = if (isScheduledJunkCleanEnabled) {
                                            "Active • $nextRunText ($tickedCount ticked)"
                                        } else {
                                            "Background worker paused • Set daily time"
                                        },
                                        fontSize = 10.sp,
                                        color = activeSubTextColor
                                    )
                                }
                            }

                            Switch(
                                checked = isScheduledJunkCleanEnabled,
                                onCheckedChange = { enabled ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (enabled && tickedCount == 0) {
                                        Toast.makeText(
                                            context,
                                            "Tick at least one safe junk category first!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        viewModel.toggleScheduledJunkClean(enabled)
                                        Toast.makeText(
                                            context,
                                            if (enabled) "Daily auto-clean scheduled at $formattedScheduleTime" else "Scheduled auto-clean disabled",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.testTag("scheduled_junk_clean_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Time Picker Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Scheduled Clean Time",
                                    fontSize = 10.sp,
                                    color = activeSubTextColor
                                )
                                Text(
                                    text = formattedScheduleTime,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("scheduled_junk_time_text")
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    TimePickerDialog(
                                        context,
                                        { _, pickedHour, pickedMinute ->
                                            viewModel.updateScheduledCleanTime(pickedHour, pickedMinute)
                                            val newLabel = JunkCleanupScheduler.formatTime12Hour(pickedHour, pickedMinute)
                                            Toast.makeText(context, "Auto-clean time set to $newLabel", Toast.LENGTH_SHORT).show()
                                        },
                                        scheduledCleanHour,
                                        scheduledCleanMinute,
                                        false
                                    ).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("scheduled_junk_time_picker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Choose Clean Time",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SET TIME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Preset Time Chips (including 3:00 AM default)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val presets = listOf(
                                Triple("2:00 AM", 2, 0),
                                Triple("3:00 AM", 3, 0),
                                Triple("4:00 AM", 4, 0),
                                Triple("11:30 PM", 23, 30)
                            )
                            presets.forEach { (label, h, m) ->
                                val isSelected = scheduledCleanHour == h && scheduledCleanMinute == m
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) {
                                                if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) {
                                                if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.updateScheduledCleanTime(h, m)
                                        }
                                        .padding(vertical = 6.dp)
                                        .testTag("preset_time_${h}_${m}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) {
                                            if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            activeTextColor
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last run: $lastScheduledCleanSummary",
                                fontSize = 10.sp,
                                color = activeSubTextColor,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                    }
                                    viewModel.triggerScheduledWorkerNow()
                                    Toast.makeText(
                                        context,
                                        "Background safe cleanup worker queued now!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("run_scheduled_worker_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Run Worker Now",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("RUN WORKER", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isSimRunning) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Safe sweep status: $activePhase (${simulationProgress.toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = activeTextColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { simulationProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                trackColor = if (isHighContrast) Color.DarkGray else MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (tickedCount == 0) {
                                    Toast.makeText(context, "Please tick at least one safe junk category.", Toast.LENGTH_SHORT).show()
                                } else {
                                    isSimRunning = true
                                    coroutineScope.launch {
                                        activePhase = "Scanning $tickedCount ticked categories..."
                                        for (i in 0..100 step 20) {
                                            simulationProgress = i.toFloat()
                                            if (i == 40) activePhase = "Safely clearing ticked caches..."
                                            if (i == 80) activePhase = "Verifying storage integrity..."
                                            delay(350)
                                        }
                                        isSimRunning = false
                                        viewModel.cleanJunkFiles {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                            }
                                            Toast.makeText(context, "Cleaned $tickedCount ticked safe categories!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("simulate_cache_clean_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CLEAN $tickedCount TICKED SAFE CATEGORIES NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Action Optimisers Row (Scan, One tap Cache Clean, optimize battery)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // SCAN
                OutlinedButton(
                    onClick = { viewModel.scanSystem() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("scan_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = activeTextColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = activeTextColor)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "", modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DIAGNOSE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // ONE-TAP CLEAN
                Button(
                    onClick = { viewModel.cleanJunkFiles() },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .testTag("junk_clean_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                        contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.CleaningServices, contentDescription = "", modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ONE-TAP CLEAN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // BATTERY BOOST
                Button(
                    onClick = { viewModel.optimizeBattery() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("battery_optimise_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.secondary,
                        contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.BatterySaver, contentDescription = "", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BOOST BAT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // AI Intelligent Device Audit System
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
                    .testTag("ai_audit_card"),
                colors = CardDefaults.cardColors(containerColor = activeCardBgColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Audit Icon",
                                tint = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🧠 AI SYSTEM AUDIT REPORT",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = activeTextColor
                            )
                        }
                        
                        Text(
                            text = if (auditResult != null) "RE-AUDIT" else "RUN AUDIT",
                            modifier = Modifier
                                .background(
                                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
                                .clickable(enabled = !isAuditing) { viewModel.runDeviceAudit() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isAuditing) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = activeTextColor)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Analyzing RAM cache, background leaks & storage limits safely...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = activeTextColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    } else if (auditResult != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isHighContrast) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = auditResult!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTextColor
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, if (isHighContrast) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No audit results generated",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = activeTextColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Run Gemini-driven diagnostic report now to safely audit RAM, plan internal space recovery, safely stop background apps, and set up Google Drive archives.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = activeSubTextColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active processes details with bulk deep sleeping switches
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DEEP APP FREEZE ENGINE",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = activeTextColor
                    )
                    Text(
                        text = "Hibernate background battery leakers",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeSubTextColor
                    )
                }

                Text(
                    text = "FREEZE ALL",
                    modifier = Modifier
                        .background(
                            color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
                        .clickable { viewModel.deepSleepAllApps() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // List package systems
        if (backgroundApps.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No background processes active", color = activeTextColor)
                }
            }
        } else {
            items(backgroundApps) { app ->
                AppFreezeRow(
                    app = app,
                    onClickFreeze = { viewModel.toggleAppSleep(app.packageName) },
                    isHighContrast = isHighContrast
                )
            }
        }

        // Padding spacer along screen edges
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun AppFreezeRow(
    app: BackgroundApp,
    onClickFreeze: () -> Unit,
    isHighContrast: Boolean
) {
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontWeight = FontWeight.Bold,
                    color = activeTextColor,
                    fontSize = 14.sp
                )
                Text(
                    text = "${app.memoryUsageMB} MB RAM • ${app.packageName}",
                    fontSize = 11.sp,
                    color = activeSubTextColor
                )
            }

            // Power button sleep indicator
            val containerColor = when {
                app.isAsleep && isHighContrast -> Color.White
                app.isAsleep -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }
            val contentColor = when {
                app.isAsleep && isHighContrast -> Color.Black
                app.isAsleep -> MaterialTheme.colorScheme.onPrimaryContainer
                isHighContrast -> Color.White
                else -> MaterialTheme.colorScheme.outline
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(containerColor)
                    .border(1.dp, if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    .clickable { onClickFreeze() }
                    .testTag("freeze_${app.packageName}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (app.isAsleep) Icons.Default.Done else Icons.Default.PowerSettingsNew,
                    contentDescription = "Freeze",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AlertCard(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isHighContrast: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isHighContrast) Color.White else color.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Warning alert pointer",
                tint = if (isHighContrast) Color.White else color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = if (isHighContrast) Color.White else color
            )
        }
    }
}

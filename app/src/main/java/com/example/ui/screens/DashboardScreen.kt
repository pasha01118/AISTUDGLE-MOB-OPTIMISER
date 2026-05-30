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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.BackgroundApp
import com.example.viewmodel.OptimiserViewModel
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: OptimiserViewModel,
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
    val isHighContrast by viewModel.isHighContrast.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()
    val isAuditing by viewModel.isAuditing.collectAsState()

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
        // App Core Title Tag
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MOBILE OPTIMISER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        ),
                        color = activeTextColor
                    )
                    Text(
                        text = "Real-time AI Assistive Diagnostics",
                        style = MaterialTheme.typography.bodySmall,
                        color = activeSubTextColor
                    )
                }
            }
        }

        // Warning alerts list
        item {
            AnimatedVisibility(visible = thermalAlert != null || batteryAlert != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                text = statusMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = activeTextColor
                            )
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

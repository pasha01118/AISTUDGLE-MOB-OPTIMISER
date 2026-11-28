package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.repository.HighRiskAppPermission
import com.example.data.repository.RiskPermissionCategory

/**
 * Interactive Permission Manager UI that audits installed launcher apps for high-risk
 * permissions (Location, Microphone, Camera, Contacts, Phone/SMS, Body Sensors) and
 * provides a single-click Revoke action routing directly to Android App Permissions
 * with automatic live rescan on return.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PermissionManagerPanel(
    apps: List<HighRiskAppPermission>,
    isScanning: Boolean,
    isHighContrast: Boolean,
    activeCardBgColor: Color,
    activeBorderColor: Color,
    activeTextColor: Color,
    activeSubTextColor: Color,
    onScanPermissions: () -> Unit,
    onRevokePermission: (HighRiskAppPermission) -> Unit,
    maxVisibleItems: Int = Int.MAX_VALUE,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedFilter by remember { mutableStateOf<RiskPermissionCategory?>(null) }

    // Automatically re-verify live PackageManager permission flags when returning from Android Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onScanPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filteredApps = remember(apps, selectedFilter) {
        if (selectedFilter == null) apps
        else apps.filter { app -> app.permissions.any { it.category == selectedFilter } }
    }

    val criticalCount = apps.count { !it.isRevokedLocally && it.riskLevel == "CRITICAL" }
    val activeGrantedCount = apps.sumOf { app -> app.permissions.count { it.isGranted } }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, activeBorderColor, RoundedCornerShape(16.dp))
            .testTag("permission_manager_panel"),
        colors = CardDefaults.cardColors(containerColor = activeCardBgColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
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
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "High-Risk Permission Manager",
                        tint = if (isHighContrast) Color.White else Color(0xFFFF5252),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "HIGH-RISK PERMISSION MANAGER",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = activeTextColor
                        )
                        Text(
                            text = "${apps.size} apps audited • $activeGrantedCount active grants • $criticalCount critical",
                            fontSize = 10.sp,
                            color = activeSubTextColor
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onScanPermissions()
                    },
                    enabled = !isScanning,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isHighContrast) Color.White else Color(0xFFFF5252).copy(alpha = 0.6f)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("permission_manager_rescan_button")
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = if (isHighContrast) Color.White else Color(0xFFFF5252)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan App Permissions",
                            tint = if (isHighContrast) Color.White else Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SCAN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighContrast) Color.White else Color(0xFFFF5252)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PermissionFilterPill(
                    label = "All (${apps.size})",
                    selected = selectedFilter == null,
                    isHighContrast = isHighContrast,
                    onClick = { selectedFilter = null },
                    testTag = "perm_filter_all"
                )
                RiskPermissionCategory.entries.forEach { cat ->
                    val count = apps.count { app -> app.permissions.any { it.category == cat } }
                    PermissionFilterPill(
                        label = "${cat.shortLabel} ($count)",
                        selected = selectedFilter == cat,
                        isHighContrast = isHighContrast,
                        onClick = { selectedFilter = if (selectedFilter == cat) null else cat },
                        testTag = "perm_filter_${cat.name.lowercase()}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isScanning && apps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = if (isHighContrast) Color.White else Color(0xFFFF5252)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Inspecting installed apps for Location, Mic & Camera access...",
                            fontSize = 11.sp,
                            color = activeSubTextColor
                        )
                    }
                }
            } else if (filteredApps.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isHighContrast) Color.Black else Color(0xFF0F2417),
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isHighContrast) Color.White else Color(0xFF00E676).copy(alpha = 0.45f),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "No high-risk permissions found",
                        tint = if (isHighContrast) Color.White else Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (selectedFilter == null) {
                            "No installed launcher apps currently hold unreviewed high-risk permissions."
                        } else {
                            "No audited apps match the ${selectedFilter?.shortLabel} risk filter."
                        },
                        fontSize = 11.sp,
                        color = activeTextColor
                    )
                }
            } else {
                val visibleList = filteredApps.take(maxVisibleItems)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visibleList.forEach { app ->
                        val riskColor = when {
                            isHighContrast -> Color.White
                            app.isRevokedLocally -> Color(0xFF00E676)
                            app.riskLevel == "CRITICAL" -> Color(0xFFFF5252)
                            app.riskLevel == "HIGH" -> Color(0xFFFFAB40)
                            else -> Color(0xFF40C4FF)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isHighContrast) Color.White else riskColor.copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                                .testTag("perm_app_row_${app.packageName}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = app.appName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeTextColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    riskColor.copy(alpha = if (isHighContrast) 0.2f else 0.16f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (app.isRevokedLocally) "REVOKED" else "${app.riskLevel} ${app.riskScore}",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = riskColor
                                            )
                                        }
                                    }
                                    Text(
                                        text = app.packageName,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = activeSubTextColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                        }
                                        onRevokePermission(app)
                                        if (app.isInstalledOnDevice) {
                                            try {
                                                val intent = Intent(
                                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", app.packageName, null)
                                                ).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                                Toast.makeText(
                                                    context,
                                                    "Opened ${app.appName} permissions to revoke access",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } catch (_: Exception) {
                                                Toast.makeText(
                                                    context,
                                                    "Marked ${app.appName} high-risk permissions restricted",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Revoked high-risk access for ${app.appName}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    enabled = !app.isRevokedLocally,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (app.isRevokedLocally) {
                                            Color(0xFF2E7D32)
                                        } else if (isHighContrast) {
                                            Color.White
                                        } else {
                                            Color(0xFFD32F2F)
                                        },
                                        contentColor = if (isHighContrast && !app.isRevokedLocally) Color.Black else Color.White,
                                        disabledContainerColor = Color(0xFF1B3A24),
                                        disabledContentColor = Color(0xFF81C784)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .testTag("revoke_permission_${app.packageName}")
                                ) {
                                    Icon(
                                        imageVector = if (app.isRevokedLocally) Icons.Default.CheckCircle else Icons.Default.Block,
                                        contentDescription = if (app.isRevokedLocally) "Revoked" else "Revoke Permission",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (app.isRevokedLocally) "REVOKED" else "REVOKE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = activeSubTextColor.copy(alpha = 0.14f))
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                app.permissions.forEach { perm ->
                                    val chipColor = when {
                                        isHighContrast -> Color.White
                                        perm.isGranted -> Color(0xFFFF5252)
                                        else -> Color(0xFF90A4AE)
                                    }
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(chipColor.copy(alpha = 0.14f))
                                            .border(1.dp, chipColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon(perm.category),
                                            contentDescription = perm.label,
                                            tint = chipColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (perm.isGranted) "${perm.label} • ACTIVE" else perm.label,
                                            fontSize = 9.sp,
                                            fontWeight = if (perm.isGranted) FontWeight.Bold else FontWeight.Medium,
                                            color = chipColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionFilterPill(
    label: String,
    selected: Boolean,
    isHighContrast: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = when {
        selected && isHighContrast -> Color.White
        selected -> Color(0xFFFF5252).copy(alpha = 0.22f)
        isHighContrast -> Color.Black
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }
    val border = when {
        isHighContrast -> Color.White
        selected -> Color(0xFFFF5252)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    }
    val textColor = when {
        selected && isHighContrast -> Color.Black
        selected -> Color(0xFFFF8A80)
        isHighContrast -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

private fun categoryIcon(category: RiskPermissionCategory): ImageVector = when (category) {
    RiskPermissionCategory.LOCATION -> Icons.Default.LocationOn
    RiskPermissionCategory.MICROPHONE -> Icons.Default.Mic
    RiskPermissionCategory.CAMERA -> Icons.Default.CameraAlt
    RiskPermissionCategory.CONTACTS -> Icons.Default.Contacts
    RiskPermissionCategory.PHONE_SMS -> Icons.Default.Phone
    RiskPermissionCategory.BODY_SENSORS -> Icons.Default.MonitorHeart
}

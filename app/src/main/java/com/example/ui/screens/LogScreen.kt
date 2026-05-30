package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityLog
import com.example.data.model.BackupSnapshot
import com.example.viewmodel.OptimiserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogScreen(
    viewModel: OptimiserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logs by viewModel.activityLogs.collectAsState()
    val snapshots by viewModel.backupSnapshots.collectAsState()
    val isHighContrast by viewModel.isHighContrast.collectAsState()

    // Filter selectors
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedDateRangePreset by remember { mutableStateOf("All-time") } // All-time, Today, Past 7 days, Past 30 days
    
    // Backup snapshot configurations
    var showBackupCreatorDialog by remember { mutableStateOf(false) }
    var backupNoteText by remember { mutableStateOf("") }

    // Export Modal State
    var showExportedJsonDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }

    // Custom coloring theme
    val activeBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.background
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onBackground
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent
    val cardBgColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    // Calculate dates metrics
    val now = System.currentTimeMillis()
    val startLimit = when (selectedDateRangePreset) {
        "Today" -> now - 24 * 60 * 60 * 1000L
        "Past 7 days" -> now - 7 * 24 * 60 * 60 * 1000L
        "Past 30 days" -> now - 30 * 24 * 60 * 60 * 1000L
        else -> 0L // All-Time
    }

    // Filter logs in-memory for immediate UI responsiveness
    val filteredLogs = logs.filter { log ->
        val matchesType = selectedTypeFilter == "ALL" || log.activityType.equals(selectedTypeFilter, ignoreCase = true)
        val matchesDate = log.timestamp >= startLimit
        matchesType && matchesDate
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(activeBgColor)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Log Section Header
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "LOGS & ENCRYPTED BACKUPS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                ),
                color = activeTextColor
            )
            Text(
                text = "View immutable audit records and trigger secure AES local backups",
                style = MaterialTheme.typography.bodySmall,
                color = activeSubTextColor
            )
        }

        // --- Filter Panel Card ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FILTER AND EXPORT DIALOG",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeTextColor,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Type tags dropdown simulation
                    Text("Activity Type:", fontSize = 11.sp, color = activeSubTextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "JUNK_CLEAN", "BATTERY_SAVER", "DEEP_SLEEP", "BACKUP_SNAPSHOT", "CLOUD_SYNC", "THERMAL_ALERT").forEach { type ->
                            val isSelected = selectedTypeFilter == type
                            val tagBg = if (isSelected) {
                                if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
                            } else {
                                if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface
                            }
                            val tagText = if (isSelected) {
                                if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                            } else {
                                if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .padding(vertical = 3.dp)
                                    .background(tagBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isHighContrast) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { selectedTypeFilter = type }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("filter_type_$type"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(type, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date range selection presets
                    Text("Date Range Filter:", fontSize = 11.sp, color = activeSubTextColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All-time", "Today", "Past 7 days", "Past 30 days").forEach { rangePreset ->
                            val isSelected = selectedDateRangePreset == rangePreset
                            val btnBg = if (isSelected) {
                                if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                Color.Transparent
                            }
                            val btnText = if (isSelected) {
                                Color.Black
                            } else {
                                activeTextColor
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(btnBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { selectedDateRangePreset = rangePreset }
                                    .padding(vertical = 6.dp)
                                    .testTag("filter_date_$rangePreset"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(rangePreset, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = btnText)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // JSON Export Action Trigger
                    Button(
                        onClick = {
                            viewModel.exportHistoryJson(startLimit, now, selectedTypeFilter) { jsonString ->
                                exportedJsonText = jsonString
                                showExportedJsonDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("export_json_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                            contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GENERATE & EXPORT HISTORY IN JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Exported JSON Viewer Overlay (Responsive Dialog style box) ---
        item {
            AnimatedVisibility(visible = showExportedJsonDialog) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = "", tint = Color.Green, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("COMPLYING EXPORT LOGS (JSON)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { showExportedJsonDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            LazyColumn {
                                item {
                                    Text(
                                        text = exportedJsonText,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.Green
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Optimiser JSON Logs", exportedJsonText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "COPIED TO CLIPBOARD!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("COPY ALL TO CLIPBOARD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Encrypted Local Snapshots Card ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, activeBorderColor, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Backup, contentDescription = "", tint = activeTextColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENCRYPTED SNAPSHOTS (${snapshots.size})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = activeTextColor
                            )
                        }

                        IconButton(
                            onClick = { showBackupCreatorDialog = !showBackupCreatorDialog },
                            modifier = Modifier.testTag("create_backup_dialog_trigger")
                        ) {
                            Icon(Icons.Default.LibraryAdd, contentDescription = "Add snapshots", tint = activeTextColor)
                        }
                    }

                    // Dialog to type notes for backups
                    AnimatedVisibility(visible = showBackupCreatorDialog) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            OutlinedTextField(
                                value = backupNoteText,
                                onValueChange = { backupNoteText = it },
                                label = { Text("Backup Note / Identifier", color = activeTextColor) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("backup_note_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = activeTextColor,
                                    unfocusedTextColor = activeTextColor,
                                    focusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    val note = backupNoteText.ifEmpty { "Periodic snapshot backup" }
                                    viewModel.backUpNow(note)
                                    backupNoteText = ""
                                    showBackupCreatorDialog = false
                                    Toast.makeText(context, "Encrypted local snapshot saved successfully!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("submit_backup_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("RUN BACKUP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    if (snapshots.isEmpty()) {
                        Text(
                            "No snapshots currently stored. Tap (+) above to create an encrypted database snapshot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeSubTextColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        snapshots.forEach { snapshot ->
                            BackupSnapshotRow(
                                snapshot = snapshot,
                                onRestore = {
                                    viewModel.restoreBackup(snapshot)
                                    Toast.makeText(context, "Backup Restored! Database logs re-aligned.", Toast.LENGTH_SHORT).show()
                                },
                                onDelete = { viewModel.deleteBackup(snapshot) },
                                isHighContrast = isHighContrast
                            )
                        }
                    }
                }
            }
        }

        // --- Activity Logs Audit Scroll Section ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "",
                    tint = activeTextColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HISTORICAL AUDIT LOGS (${filteredLogs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = activeTextColor
                )
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(cardBgColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Activity audit trails matched filters.", color = activeSubTextColor, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredLogs) { log ->
                AuditLogCardRow(
                    log = log,
                    isHighContrast = isHighContrast
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun BackupSnapshotRow(
    snapshot: BackupSnapshot,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    isHighContrast: Boolean
) {
    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = snapshot.note,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = activeTextColor
            )
            Text(
                text = "${dateFormat.format(Date(snapshot.timestamp))} • ${snapshot.recordsCount} items • E2E AES Encrypted",
                fontSize = 11.sp,
                color = activeSubTextColor
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Restore btn
            IconButton(
                onClick = onRestore,
                modifier = Modifier.testTag("restore_backup_${snapshot.id}")
            ) {
                Icon(Icons.Default.Restore, contentDescription = "Restore backup", tint = if (isHighContrast) Color.White else Color(0xFF4CAF50))
            }
            // Delete btn
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_backup_${snapshot.id}")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete snapshot", tint = if (isHighContrast) Color.White else Color.Red)
            }
        }
    }
}

@Composable
fun AuditLogCardRow(
    log: ActivityLog,
    isHighContrast: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()) }
    val dateString = dateFormat.format(Date(log.timestamp))

    val colorAccent = when (log.activityType.uppercase(Locale.ROOT)) {
        "JUNK_CLEAN" -> Color(0xFF4CAF50)
        "BATTERY_SAVER" -> Color(0xFF03A9F4)
        "DEEP_SLEEP" -> Color(0xFF9C27B0)
        "BACKUP_SNAPSHOT" -> Color(0xFF2196F3)
        "CLOUD_SYNC" -> Color(0xFFE91E63)
        else -> Color(0xFFFF9800) // Theraml/Alert warning
    }

    val activeTextColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
    val activeSubTextColor = if (isHighContrast) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
    val activeBorderColor = if (isHighContrast) Color.White else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, activeBorderColor, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Accent Pill
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(if (isHighContrast) Color.White else colorAccent, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.activityType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHighContrast) Color.White else colorAccent,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = dateString,
                        fontSize = 10.sp,
                        color = activeSubTextColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = log.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = activeTextColor
                )
                
                if (log.isEncrypted && log.detailsJson.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted indicator lock",
                            tint = if (isHighContrast) Color.White else colorAccent.copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Record details AES payload protected",
                            fontSize = 9.sp,
                            color = activeSubTextColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

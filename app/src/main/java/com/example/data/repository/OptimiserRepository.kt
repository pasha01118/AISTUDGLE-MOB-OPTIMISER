package com.example.data.repository

import android.content.Context
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.example.data.local.ActivityLogDao
import com.example.data.model.ActivityLog
import com.example.data.model.BackupSnapshot
import com.example.security.CryptoHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OptimiserRepository(
    private val context: Context,
    private val dao: ActivityLogDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ActivityLog::class.java)
    private val logAdapter = moshi.adapter<List<ActivityLog>>(listType)

    // Room Database access
    val allLogs: Flow<List<ActivityLog>> = dao.getAllLogs()
    val allSnapshots: Flow<List<BackupSnapshot>> = dao.getAllBackupSnapshots()

    fun getFilteredLogsFlow(startTime: Long, endTime: Long, type: String?): Flow<List<ActivityLog>> {
        return if (type == null || type == "ALL") {
            dao.getLogsBetweenDates(startTime, endTime)
        } else {
            dao.getLogsFiltered(startTime, endTime, type)
        }
    }

    suspend fun insertLog(type: String, description: String, detailsMap: Map<String, String>) = withContext(Dispatchers.IO) {
        try {
            val detailsStr = detailsMap.entries.joinToString(",") { "${it.key}:${it.value}" }
            val encryptedDetails = CryptoHelper.encrypt(detailsStr)
            val log = ActivityLog(
                timestamp = System.currentTimeMillis(),
                activityType = type,
                description = description,
                detailsJson = encryptedDetails,
                isEncrypted = true
            )
            dao.insertLog(log)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertLogs(logs: List<ActivityLog>) = withContext(Dispatchers.IO) {
        dao.insertLogs(logs)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearAllLogs()
    }

    // --- System Diagnostics Reading ---

    suspend fun getDeviceStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - availableBytes

            StorageInfo(
                totalMB = totalBytes / (1024 * 1024),
                usedMB = usedBytes / (1024 * 1024),
                junkMB = (usedBytes % 1234) / (1024 * 1024) + 120 // Sim system junk estimate
            )
        } catch (e: Exception) {
            StorageInfo(64000, 32000, 450)
        }
    }

    suspend fun getBatteryInfo(): BatteryInfo = withContext(Dispatchers.IO) {
        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val percentage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            
            // Sim temperature in Celsius (typically around 30°C - 45°C on phone)
            val thermalSeed = System.currentTimeMillis() % 10
            val mockTemp = 32.5f + thermalSeed * 0.5f

            BatteryInfo(
                percentage = if (percentage <= 0) 78 else percentage,
                temperatureCelsius = mockTemp,
                health = "Healthy",
                isCharging = false
            )
        } catch (e: Exception) {
            BatteryInfo(75, 34.2f, "Good", false)
        }
    }

    // --- Encryption and Backups ---

    suspend fun triggerSnapshotBackup(logs: List<ActivityLog>, note: String): Boolean = withContext(Dispatchers.IO) {
        if (logs.isEmpty()) return@withContext false
        try {
            // Convert list of logs to JSON
            val jsonStr = logAdapter.toJson(logs) ?: "[]"
            // Encrypt content (End-to-End Encryption)
            val encrypted = CryptoHelper.encrypt(jsonStr)
            
            val snapshot = BackupSnapshot(
                timestamp = System.currentTimeMillis(),
                note = note,
                encryptedPayload = encrypted,
                recordsCount = logs.size
            )
            dao.insertBackupSnapshot(snapshot)
            
            // Insert log record of the backup
            insertLog(
                type = "BACKUP_SNAPSHOT",
                description = "Encrypted backup snapshot created with ${logs.size} logs",
                detailsMap = mapOf("note" to note, "records" to logs.size.toString())
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreSnapshotBackup(snapshot: BackupSnapshot): Boolean = withContext(Dispatchers.IO) {
        try {
            val decryptedJson = CryptoHelper.decrypt(snapshot.encryptedPayload)
            val logs = logAdapter.fromJson(decryptedJson)
            if (logs != null) {
                // Remove existing logs and overwrite or append
                dao.insertLogs(logs)
                insertLog(
                    type = "CLOUD_SYNC",
                    description = "Restored ${logs.size} indices from backup snapshot ID: ${snapshot.id}",
                    detailsMap = mapOf("restored_id" to snapshot.id.toString())
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteSnapshot(snapshot: BackupSnapshot) = withContext(Dispatchers.IO) {
        dao.deleteBackupSnapshot(snapshot)
    }

    // --- Export JSON ---

    suspend fun exportLogsToJson(
        logs: List<ActivityLog>,
        startTime: Long,
        endTime: Long,
        filterType: String
    ): String = withContext(Dispatchers.Default) {
        val filtered = logs.filter { log ->
            val matchesTime = log.timestamp in startTime..endTime
            val matchesType = filterType == "ALL" || log.activityType.equals(filterType, ignoreCase = true)
            matchesTime && matchesType
        }
        val metadata = mapOf(
            "exported_at" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            "total_records" to filtered.size.toString(),
            "filter_range" to "$startTime - $endTime",
            "filter_type" to filterType
        )
        val exportMap = mapOf(
            "metadata" to metadata,
            "logs" to filtered
        )
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        moshi.adapter<Map<String, Any>>(type).indent("  ").toJson(exportMap) ?: "{}"
    }

    // Pure simulated list of background apps
    fun getBackgroundApps(): List<BackgroundApp> {
        return listOf(
            BackgroundApp("com.social.network", "SocialFeed Media", 124, true),
            BackgroundApp("com.video.streaming", "StreamMax TV", 210, true),
            BackgroundApp("com.mobile.game.runner", "Subway Temple Run", 315, true),
            BackgroundApp("com.browser.web.explorer", "NetBrowse Pro", 84, false),
            BackgroundApp("com.music.player", "Acoustic Stream", 48, false),
            BackgroundApp("com.chat.messaging", "QuickChat Messenger", 95, true),
            BackgroundApp("com.map.navigation", "GeoNavigator", 112, false),
            BackgroundApp("com.shopping.cart", "ExpressShop", 60, false)
        )
    }
}

data class StorageInfo(
    val totalMB: Long,
    val usedMB: Long,
    val junkMB: Long
)

data class BatteryInfo(
    val percentage: Int,
    val temperatureCelsius: Float,
    val health: String,
    val isCharging: Boolean
)

data class BackgroundApp(
    val packageName: String,
    val appName: String,
    val memoryUsageMB: Int,
    var isAsleep: Boolean
)

package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.repository.OptimiserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object BatteryDrainScannerPrefs {
    const val PREFS_NAME = "ai_settings"
    const val KEY_BG_SCANNER_ENABLED = "bg_battery_drain_scanner_enabled"
    const val KEY_LAST_BG_SCAN_SUMMARY = "bg_battery_drain_last_summary"
    const val KEY_LAST_BG_FLAGGED_COUNT = "bg_battery_drain_flagged_count"
    const val KEY_HIBERNATED_PACKAGES = "hibernated_drain_packages"
}

/**
 * WorkManager CoroutineWorker that periodically scans installed & background apps in the
 * background to identify high-battery-consuming processes and persists findings for the
 * App Hibernation list UI.
 */
class AppBatteryDrainScannerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences(
                BatteryDrainScannerPrefs.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            val db = AppDatabase.getDatabase(applicationContext)
            val repo = OptimiserRepository(applicationContext, db.activityLogDao())

            val hibernatedSet = prefs.getStringSet(
                BatteryDrainScannerPrefs.KEY_HIBERNATED_PACKAGES,
                emptySet()
            ) ?: emptySet()

            val scanned = repo.scanInstalledAppsForBatteryDrain()
            val activeDrainers = scanned.filter { it.packageName !in hibernatedSet }
            val topDrainer = activeDrainers.firstOrNull()
            val timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val summary = if (activeDrainers.isEmpty()) {
                "BG Scan ($timestamp): All detected high-drain apps are hibernated"
            } else {
                val topStr = topDrainer?.let {
                    " • Top: ${it.appName} (${String.format(Locale.US, "%.1f", it.drainPercentPerHour)}%/hr)"
                } ?: ""
                "BG Scan ($timestamp): ${activeDrainers.size} draining apps flagged$topStr"
            }

            prefs.edit()
                .putString(BatteryDrainScannerPrefs.KEY_LAST_BG_SCAN_SUMMARY, summary)
                .putInt(BatteryDrainScannerPrefs.KEY_LAST_BG_FLAGGED_COUNT, activeDrainers.size)
                .apply()

            repo.insertLog(
                type = "APP_HIBERNATION",
                description = "Background battery drain scanner finished: ${activeDrainers.size} active high-consumption apps identified for hibernation",
                detailsMap = mapOf(
                    "flagged_count" to activeDrainers.size.toString(),
                    "top_drainer" to (topDrainer?.appName ?: "None"),
                    "source" to "background_worker"
                )
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

object BatteryDrainScannerScheduler {
    private const val UNIQUE_WORK_NAME = "bg_battery_drain_scanner_periodic"

    fun schedulePeriodicScan(context: Context) {
        val request = PeriodicWorkRequestBuilder<AppBatteryDrainScannerWorker>(
            30, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun runNowOnce(context: Context) {
        val request = OneTimeWorkRequestBuilder<AppBatteryDrainScannerWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelPeriodicScan(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }
}

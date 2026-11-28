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
import com.example.widget.GlanceBatteryWidgetHelper
import com.example.widget.Live3GBatteryWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object SmartPowerPrefs {
    const val PREFS_NAME = "ai_settings"
    const val KEY_SMART_POWER_ENABLED = "smart_power_schedule_enabled"
    const val KEY_SLEEP_START_HOUR = "smart_power_sleep_start_hour"
    const val KEY_SLEEP_START_MINUTE = "smart_power_sleep_start_minute"
    const val KEY_SLEEP_END_HOUR = "smart_power_sleep_end_hour"
    const val KEY_SLEEP_END_MINUTE = "smart_power_sleep_end_minute"
    const val KEY_DYNAMIC_PROFILES_ENABLED = "dynamic_power_profiles_enabled"
    const val KEY_LAST_WORKER_CHECK = "smart_power_last_worker_check"
}

/**
 * Background WorkManager worker that evaluates the user's configured sleep schedule
 * against the device clock and automatically activates Battery Saver mode during sleep hours.
 */
class SmartPowerScheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences(
                SmartPowerPrefs.PREFS_NAME,
                Context.MODE_PRIVATE
            )
            val isEnabled = prefs.getBoolean(SmartPowerPrefs.KEY_SMART_POWER_ENABLED, false)
            val nowLabel = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            if (!isEnabled) {
                prefs.edit()
                    .putString(SmartPowerPrefs.KEY_LAST_WORKER_CHECK, "Checked $nowLabel • Schedule paused")
                    .apply()
                return@withContext Result.success()
            }

            val startHour = prefs.getInt(SmartPowerPrefs.KEY_SLEEP_START_HOUR, 23)
            val startMin = prefs.getInt(SmartPowerPrefs.KEY_SLEEP_START_MINUTE, 0)
            val endHour = prefs.getInt(SmartPowerPrefs.KEY_SLEEP_END_HOUR, 7)
            val endMin = prefs.getInt(SmartPowerPrefs.KEY_SLEEP_END_MINUTE, 0)

            val inSleepWindow = SmartPowerScheduler.isWithinSleepWindow(
                startHour, startMin, endHour, endMin
            )
            val isCurrentlyPowerSaving = prefs.getBoolean(
                Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE,
                true
            )

            if (inSleepWindow && !isCurrentlyPowerSaving) {
                prefs.edit()
                    .putBoolean(Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE, true)
                    .putString(
                        SmartPowerPrefs.KEY_LAST_WORKER_CHECK,
                        "Worker $nowLabel • Activated Battery Saver (Sleep Window)"
                    )
                    .apply()
                Live3GBatteryWidgetProvider.updateAllWidgets(applicationContext)
                GlanceBatteryWidgetHelper.updateAll(applicationContext)

                val db = AppDatabase.getDatabase(applicationContext)
                val repo = OptimiserRepository(applicationContext, db.activityLogDao())
                repo.insertLog(
                    type = "SMART_POWER_SCHEDULE",
                    description = "Smart Power Scheduling Worker auto-enabled Battery Saver during sleep window (${SmartPowerScheduler.formatTime12Hour(startHour, startMin)} - ${SmartPowerScheduler.formatTime12Hour(endHour, endMin)})",
                    detailsMap = mapOf(
                        "window_start" to "$startHour:$startMin",
                        "window_end" to "$endHour:$endMin",
                        "action" to "ENABLED_BATTERY_SAVER"
                    )
                )
            } else {
                val statusNote = if (inSleepWindow) {
                    "Worker $nowLabel • Sleep window active (Saver ON)"
                } else {
                    "Worker $nowLabel • Standby until ${SmartPowerScheduler.formatTime12Hour(startHour, startMin)}"
                }
                prefs.edit()
                    .putString(SmartPowerPrefs.KEY_LAST_WORKER_CHECK, statusNote)
                    .apply()
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}

object SmartPowerScheduler {
    private const val UNIQUE_WORK_NAME = "smart_power_sleep_schedule_work"

    fun schedulePeriodicCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<SmartPowerScheduleWorker>(
            15, TimeUnit.MINUTES
        ).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun runImmediateCheck(context: Context) {
        val request = OneTimeWorkRequestBuilder<SmartPowerScheduleWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelPeriodicCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    /**
     * Checks whether the current device clock falls within [startHour:startMin .. endHour:endMin].
     * Properly handles overnight ranges that wrap past midnight (e.g. 23:00 -> 07:00).
     */
    fun isWithinSleepWindow(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        now: Calendar = Calendar.getInstance()
    ): Boolean {
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val startMinutes = startHour.coerceIn(0, 23) * 60 + startMinute.coerceIn(0, 59)
        val endMinutes = endHour.coerceIn(0, 23) * 60 + endMinute.coerceIn(0, 59)

        return if (startMinutes == endMinutes) {
            false
        } else if (startMinutes < endMinutes) {
            nowMinutes in startMinutes until endMinutes
        } else {
            // Overnight range (e.g., 23:00 to 07:00)
            nowMinutes >= startMinutes || nowMinutes < endMinutes
        }
    }

    fun formatTime12Hour(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
        }
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }
}

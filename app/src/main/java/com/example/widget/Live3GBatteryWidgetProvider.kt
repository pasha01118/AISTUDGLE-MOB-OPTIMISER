package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import java.util.Locale

class Live3GBatteryWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_SAVER) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getBoolean(KEY_POWER_SAVING_MODE, true)
            val next = !current
            prefs.edit().putBoolean(KEY_POWER_SAVING_MODE, next).apply()

            updateAllWidgets(context)

            val toastMsg = if (next) {
                "3G Live Widget: Battery Saver ENABLED"
            } else {
                "3G Live Widget: Performance Mode ACTIVE"
            }
            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTION_TOGGLE_SAVER = "com.example.widget.ACTION_TOGGLE_SAVER"
        const val PREFS_NAME = "ai_settings"
        const val KEY_POWER_SAVING_MODE = "is_power_saving_mode"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, Live3GBatteryWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_3g_live_battery)

            // Read live device battery metrics
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryPct = if (level >= 0 && scale > 0) {
                ((level * 100f) / scale.toFloat()).toInt().coerceIn(0, 100)
            } else {
                88
            }
            val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 315) ?: 315
            val tempC = tempTenths / 10f
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isSaverEnabled = prefs.getBoolean(KEY_POWER_SAVING_MODE, true)

            // Update battery readout and progress
            views.setTextViewText(R.id.widget_3g_battery_percent, "$batteryPct%")
            val stateLabel = if (isCharging) "CHARGING" else "LIVE CELL"
            views.setTextViewText(
                R.id.widget_3g_battery_subtext,
                String.format(Locale.getDefault(), "%s • %.1f°C", stateLabel, tempC)
            )
            views.setProgressBar(R.id.widget_3g_battery_progress, 100, batteryPct, false)

            // Style quick Enable Saver toggle button
            if (isSaverEnabled) {
                views.setTextViewText(R.id.widget_3g_status_chip, "ECO GUARD ON")
                views.setTextColor(R.id.widget_3g_status_chip, Color.parseColor("#FF00FF88"))
                views.setTextViewText(R.id.widget_3g_saver_toggle_btn, "⚡ SAVER ACTIVE")
                views.setTextColor(R.id.widget_3g_saver_toggle_btn, Color.parseColor("#FF041218"))
                views.setInt(R.id.widget_3g_saver_toggle_btn, "setBackgroundResource", R.drawable.widget_3g_toggle_on)
            } else {
                views.setTextViewText(R.id.widget_3g_status_chip, "TURBO 3G")
                views.setTextColor(R.id.widget_3g_status_chip, Color.parseColor("#FF00E5FF"))
                views.setTextViewText(R.id.widget_3g_saver_toggle_btn, "ENABLE SAVER")
                views.setTextColor(R.id.widget_3g_saver_toggle_btn, Color.parseColor("#FF00E5FF"))
                views.setInt(R.id.widget_3g_saver_toggle_btn, "setBackgroundResource", R.drawable.widget_3g_toggle_off)
            }

            // Wire quick Enable Saver toggle PendingIntent
            val toggleIntent = Intent(context, Live3GBatteryWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_SAVER
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_3g_saver_toggle_btn, togglePendingIntent)

            // Tapping the widget container opens MainActivity
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val launchPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 1000,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_3g_root, launchPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val myProvider = ComponentName(context, Live3GBatteryWidgetProvider::class.java)
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    appWidgetManager.requestPinAppWidget(myProvider, null, null)
                    return true
                }
            }
            return false
        }
    }
}

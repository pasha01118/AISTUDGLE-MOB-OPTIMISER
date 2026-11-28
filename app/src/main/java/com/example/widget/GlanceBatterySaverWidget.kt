package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.repository.OptimiserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Jetpack Compose Glance Home Screen Widget displaying real-time battery percentage,
 * thermal/charging status, segmented battery bar, and a one-tap Battery Saver toggle button.
 */
class GlanceBatterySaverWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val telemetry = readLiveBatteryTelemetry(context)
        provideContent {
            GlanceTheme {
                GlanceBatterySaverContent(telemetry)
            }
        }
    }

    private fun readLiveBatteryTelemetry(context: Context): GlanceBatterySnapshot {
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val propCap = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        val pct = when {
            propCap in 1..100 -> propCap
            level >= 0 && scale > 0 -> ((level * 100f) / scale.toFloat()).toInt().coerceIn(0, 100)
            else -> 82
        }
        val tempC = ((batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 315) ?: 315) / 10f)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val prefs = context.getSharedPreferences(
            Live3GBatteryWidgetProvider.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val isSaverActive = prefs.getBoolean(Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE, true)

        return GlanceBatterySnapshot(
            percentage = pct,
            temperatureC = tempC,
            isCharging = isCharging,
            isPowerSavingMode = isSaverActive
        )
    }

    @Composable
    private fun GlanceBatterySaverContent(state: GlanceBatterySnapshot) {
        val accentColor = when {
            state.percentage <= 20 -> Color(0xFFFF5252)
            state.isPowerSavingMode -> Color(0xFF00E676)
            else -> Color(0xFF00E5FF)
        }
        val surfaceBg = Color(0xFF0B131E)
        val panelBg = Color(0xFF132030)
        val filledSegments = ((state.percentage / 10f).toInt()).coerceIn(1, 10)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(surfaceBg))
                .cornerRadius(20.dp)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            // Top Status Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "GLANCE POWER MONITOR",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF90CAF9)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(ColorProvider(if (state.isPowerSavingMode) Color(0xFF0E3B25) else Color(0xFF132B42)))
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (state.isPowerSavingMode) "SAVER ON" else "PERFORMANCE",
                        style = TextStyle(
                            color = ColorProvider(accentColor),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Center Battery Percentage & One-Tap Toggle Button
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "${state.percentage}%",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    val chargeLabel = if (state.isCharging) "Charging" else "Discharging"
                    Text(
                        text = String.format(Locale.getDefault(), "%s • %.1f°C", chargeLabel, state.temperatureC),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFB0BEC5)),
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Button(
                    text = if (state.isPowerSavingMode) "SAVER: ON" else "ENABLE SAVER",
                    onClick = actionRunCallback<ToggleGlanceBatterySaverAction>(),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ColorProvider(
                            if (state.isPowerSavingMode) Color(0xFF00E676) else Color(0xFF1E3A5F)
                        ),
                        contentColor = ColorProvider(
                            if (state.isPowerSavingMode) Color(0xFF061610) else Color.White
                        )
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Live 10-Segment Battery Gauge Bar
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .background(ColorProvider(panelBg))
                    .cornerRadius(6.dp)
                    .padding(4.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                for (seg in 1..10) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .height(7.dp)
                            .background(
                                ColorProvider(
                                    if (seg <= filledSegments) accentColor else Color(0xFF233245)
                                )
                            )
                            .cornerRadius(3.dp)
                    ) {}
                    if (seg < 10) {
                        Spacer(modifier = GlanceModifier.width(3.dp))
                    }
                }
            }
        }
    }
}

data class GlanceBatterySnapshot(
    val percentage: Int,
    val temperatureC: Float,
    val isCharging: Boolean,
    val isPowerSavingMode: Boolean
)

/**
 * Glance ActionCallback executed when the user taps the one-tap Battery Saver button
 * on the Compose Glance home screen widget.
 */
class ToggleGlanceBatterySaverAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val prefs = context.getSharedPreferences(
            Live3GBatteryWidgetProvider.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val current = prefs.getBoolean(Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE, true)
        val next = !current
        prefs.edit()
            .putBoolean(Live3GBatteryWidgetProvider.KEY_POWER_SAVING_MODE, next)
            .apply()

        GlanceBatterySaverWidget().updateAll(context)
        Live3GBatteryWidgetProvider.updateAllWidgets(context)

        try {
            val db = AppDatabase.getDatabase(context)
            val repo = OptimiserRepository(context, db.activityLogDao())
            repo.insertLog(
                type = "BATTERY_SAVER",
                description = "Glance Widget toggled Battery Saver ${if (next) "ON" else "OFF"}",
                detailsMap = mapOf("source" to "glance_widget", "enabled" to next.toString())
            )
        } catch (_: Exception) {
            // Best-effort audit log
        }
    }
}

class GlanceBatterySaverWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlanceBatterySaverWidget()
}

object GlanceBatteryWidgetHelper {
    fun updateAll(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                GlanceBatterySaverWidget().updateAll(context.applicationContext)
            } catch (_: Exception) {
                // Safe no-op if no Glance instances are pinned yet
            }
        }
    }

    fun requestPinWidget(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, GlanceBatterySaverWidgetReceiver::class.java)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(provider, null, null)
                return true
            }
        }
        return false
    }
}

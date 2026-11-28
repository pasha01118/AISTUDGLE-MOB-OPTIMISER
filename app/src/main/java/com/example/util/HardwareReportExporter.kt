package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.ActivityLog
import com.example.data.repository.BatteryInfo
import com.example.data.repository.DailyHardwareTrendPoint
import com.example.data.repository.StorageInfo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReportExportResult(
    val format: String, // "CSV" or "PDF"
    val fileName: String,
    val file: File,
    val publicDisplayPath: String,
    val textPreview: String,
    val byteSize: Long
)

object HardwareReportExporter {

    private fun csvEscape(value: String): String {
        val sanitized = value.replace("\r\n", " ").replace("\n", " ")
        return if (sanitized.contains(",") || sanitized.contains("\"")) {
            "\"${sanitized.replace("\"", "\"\"")}\""
        } else {
            sanitized
        }
    }

    private fun resolveAppExportDir(context: Context): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun saveCopyToPublicDownloads(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): String? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        return try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
            resolver.openOutputStream(uri)?.use { out ->
                out.write(bytes)
                out.flush()
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            "Downloads/$fileName"
        } catch (_: Exception) {
            null
        }
    }

    fun exportCsvReport(
        context: Context,
        battery: BatteryInfo,
        storage: StorageInfo,
        cpuUsagePercent: Float,
        ramUsagePercent: Int,
        isPowerSavingMode: Boolean,
        trends: List<DailyHardwareTrendPoint>,
        logs: List<ActivityLog>
    ): ReportExportResult {
        val timestampFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val timestampReadable = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val fileName = "hardware_performance_report_$timestampFile.csv"

        val sb = StringBuilder()
        sb.appendLine("# MOBILE OPTIMISER HARDWARE PERFORMANCE REPORT (CSV)")
        sb.appendLine("Section,Metric,Value")
        sb.appendLine("Metadata,Export_Timestamp,${csvEscape(timestampReadable)}")
        sb.appendLine("Metadata,Device,${csvEscape("${Build.MANUFACTURER} ${Build.MODEL}")}")
        sb.appendLine("Metadata,Android_Release,${csvEscape("Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")}")
        sb.appendLine("Live_Telemetry,CPU_Usage_Pct,${String.format(Locale.US, "%.1f", cpuUsagePercent)}")
        sb.appendLine("Live_Telemetry,RAM_Usage_Pct,$ramUsagePercent")
        sb.appendLine("Live_Telemetry,Temperature_C,${String.format(Locale.US, "%.1f", battery.temperatureCelsius)}")
        sb.appendLine("Live_Telemetry,Battery_Level_Pct,${battery.percentage}")
        sb.appendLine("Live_Telemetry,Battery_Health,${csvEscape(battery.health)}")
        sb.appendLine("Live_Telemetry,Battery_Cycle_Count,${battery.cycleCount}")
        sb.appendLine("Live_Telemetry,Battery_Est_Lifespan_Months,${battery.estimatedLifespanMonths}")
        sb.appendLine("Live_Telemetry,Storage_Used_MB,${storage.usedMB}")
        sb.appendLine("Live_Telemetry,Storage_Total_MB,${storage.totalMB}")
        sb.appendLine("Live_Telemetry,Storage_Junk_MB,${storage.junkMB}")
        sb.appendLine("Live_Telemetry,Power_Saving_Mode,$isPowerSavingMode")
        sb.appendLine()
        sb.appendLine("# 7-DAY HARDWARE PERFORMANCE TRENDS")
        sb.appendLine("Date,Day_Label,Avg_CPU_Pct,Peak_CPU_Pct,Avg_RAM_Pct,Peak_Temp_C,Bottleneck_Score,Primary_Bottleneck")
        trends.forEach { day ->
            sb.appendLine(
                listOf(
                    csvEscape(day.dateIso),
                    csvEscape(day.dayLabel),
                    String.format(Locale.US, "%.1f", day.avgCpuPct),
                    String.format(Locale.US, "%.1f", day.peakCpuPct),
                    String.format(Locale.US, "%.1f", day.avgRamPct),
                    String.format(Locale.US, "%.1f", day.peakTempC),
                    day.bottleneckScore.toString(),
                    csvEscape(day.primaryBottleneck)
                ).joinToString(",")
            )
        }
        sb.appendLine()
        sb.appendLine("# HARDWARE & OPTIMIZATION ACTIVITY LOGS")
        sb.appendLine("Log_ID,Timestamp,Activity_Type,Description")
        val logTimeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        logs.forEach { log ->
            sb.appendLine(
                listOf(
                    log.id.toString(),
                    csvEscape(logTimeFmt.format(Date(log.timestamp))),
                    csvEscape(log.activityType),
                    csvEscape(log.description)
                ).joinToString(",")
            )
        }

        val csvContent = sb.toString()
        val bytes = csvContent.toByteArray(Charsets.UTF_8)
        val outFile = File(resolveAppExportDir(context), fileName)
        outFile.writeBytes(bytes)

        val publicPath = saveCopyToPublicDownloads(context, fileName, "text/csv", bytes)
            ?: outFile.absolutePath

        return ReportExportResult(
            format = "CSV",
            fileName = fileName,
            file = outFile,
            publicDisplayPath = publicPath,
            textPreview = csvContent,
            byteSize = bytes.size.toLong()
        )
    }

    fun exportPdfReport(
        context: Context,
        battery: BatteryInfo,
        storage: StorageInfo,
        cpuUsagePercent: Float,
        ramUsagePercent: Int,
        isPowerSavingMode: Boolean,
        trends: List<DailyHardwareTrendPoint>,
        logs: List<ActivityLog>
    ): ReportExportResult {
        val timestampFile = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val timestampReadable = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val fileName = "hardware_performance_report_$timestampFile.pdf"

        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - margin * 2

        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas

        val headerBgPaint = Paint().apply { color = Color.parseColor("#0D1B2A") }
        val accentPaint = Paint().apply { color = Color.parseColor("#00E676") }
        val cardBgPaint = Paint().apply { color = Color.parseColor("#F4F7FA") }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D0D7DE")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B0BEC5")
            textSize = 9.5f
        }
        val sectionTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D1B2A")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#212529")
            textSize = 9.5f
        }
        val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#607D8B")
            textSize = 8.5f
        }
        val alertPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#C62828")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // --- PAGE 1 HEADER BANNER ---
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 88f, headerBgPaint)
        canvas.drawRect(0f, 84f, pageWidth.toFloat(), 88f, accentPaint)
        canvas.drawText("MOBILE OPTIMISER — HARDWARE PERFORMANCE REPORT", margin, 38f, titlePaint)
        canvas.drawText(
            "Generated: $timestampReadable  |  Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})",
            margin,
            58f,
            subtitlePaint
        )
        canvas.drawText(
            "7-Day Bottleneck Telemetry, Battery Health Diagnostics & Optimization Audit Trail",
            margin,
            73f,
            subtitlePaint
        )

        var y = 110f

        // --- SECTION 1: LIVE HARDWARE & BATTERY SNAPSHOT ---
        canvas.drawText("1. HARDWARE & BATTERY HEALTH SNAPSHOT", margin, y, sectionTitlePaint)
        y += 10f
        val snapshotRect = RectF(margin, y, pageWidth - margin, y + 86f)
        canvas.drawRoundRect(snapshotRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(snapshotRect, 8f, 8f, borderPaint)

        val col1X = margin + 12f
        val col2X = margin + contentWidth / 2f + 8f
        val rowY1 = y + 20f
        val rowY2 = y + 38f
        val rowY3 = y + 56f
        val rowY4 = y + 74f

        canvas.drawText("CPU Load: ${String.format(Locale.US, "%.1f", cpuUsagePercent)}%", col1X, rowY1, boldBodyPaint)
        canvas.drawText("RAM Utilization: $ramUsagePercent%", col1X, rowY2, bodyPaint)
        canvas.drawText("Thermal Sensor: ${String.format(Locale.US, "%.1f", battery.temperatureCelsius)} °C", col1X, rowY3, bodyPaint)
        canvas.drawText("Power Profile: ${if (isPowerSavingMode) "Power Saving" else "High Performance"}", col1X, rowY4, bodyPaint)

        canvas.drawText("Battery: ${battery.percentage}% (${battery.health})", col2X, rowY1, boldBodyPaint)
        canvas.drawText("Charge Cycles: ${battery.cycleCount} / ${battery.maxRatedCycles} (${battery.healthCapacityPercent}% cap.)", col2X, rowY2, bodyPaint)
        canvas.drawText("Est. Lifespan: ${battery.estimatedLifespanMonths} months  |  ${battery.voltageMv} mV", col2X, rowY3, bodyPaint)
        canvas.drawText("Storage: ${storage.usedMB} MB / ${storage.totalMB} MB (Junk: ${storage.junkMB} MB)", col2X, rowY4, bodyPaint)

        y += 108f

        // --- SECTION 2: 7-DAY PERFORMANCE TRENDS VECTOR CHART ---
        canvas.drawText("2. 7-DAY HARDWARE PERFORMANCE TRENDS", margin, y, sectionTitlePaint)
        y += 10f

        val chartRect = RectF(margin, y, pageWidth - margin, y + 130f)
        canvas.drawRoundRect(chartRect, 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(chartRect, 8f, 8f, borderPaint)

        if (trends.isNotEmpty()) {
            val plotLeft = chartRect.left + 36f
            val plotRight = chartRect.right - 16f
            val plotTop = chartRect.top + 22f
            val plotBottom = chartRect.bottom - 24f
            val plotW = plotRight - plotLeft
            val plotH = plotBottom - plotTop

            // Horizontal grid & threshold line at 75%
            val gridPaint = Paint().apply {
                color = Color.parseColor("#CFD8DC")
                strokeWidth = 0.8f
            }
            listOf(0, 25, 50, 75, 100).forEach { pct ->
                val gy = plotBottom - (pct / 100f) * plotH
                canvas.drawLine(plotLeft, gy, plotRight, gy, gridPaint)
                canvas.drawText("$pct%", chartRect.left + 8f, gy + 3f, mutedPaint)
            }

            val cpuLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#7C4DFF")
                style = Paint.Style.STROKE
                strokeWidth = 2.4f
            }
            val ramLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00897B")
                style = Paint.Style.STROKE
                strokeWidth = 2.4f
            }
            val tempLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E65100")
                style = Paint.Style.STROKE
                strokeWidth = 2.2f
            }

            val stepX = if (trends.size > 1) plotW / (trends.size - 1) else 0f
            val cpuPath = Path()
            val ramPath = Path()
            val tempPath = Path()

            trends.forEachIndexed { idx, pt ->
                val px = plotLeft + idx * stepX
                val cpuY = plotBottom - (pt.peakCpuPct.coerceIn(0f, 100f) / 100f) * plotH
                val ramY = plotBottom - (pt.avgRamPct.coerceIn(0f, 100f) / 100f) * plotH
                val tempNorm = ((pt.peakTempC - 20f) / 30f * 100f).coerceIn(0f, 100f)
                val tempY = plotBottom - (tempNorm / 100f) * plotH

                if (idx == 0) {
                    cpuPath.moveTo(px, cpuY)
                    ramPath.moveTo(px, ramY)
                    tempPath.moveTo(px, tempY)
                } else {
                    cpuPath.lineTo(px, cpuY)
                    ramPath.lineTo(px, ramY)
                    tempPath.lineTo(px, tempY)
                }
                canvas.drawText(pt.dayLabel, px - 10f, plotBottom + 14f, mutedPaint)
            }

            canvas.drawPath(cpuPath, cpuLinePaint)
            canvas.drawPath(ramPath, ramLinePaint)
            canvas.drawPath(tempPath, tempLinePaint)

            // Legend
            canvas.drawText("■ Peak CPU %", plotLeft, chartRect.top + 13f, Paint(mutedPaint).apply { color = Color.parseColor("#7C4DFF") })
            canvas.drawText("■ Avg RAM %", plotLeft + 88f, chartRect.top + 13f, Paint(mutedPaint).apply { color = Color.parseColor("#00897B") })
            canvas.drawText("■ Thermal Index", plotLeft + 172f, chartRect.top + 13f, Paint(mutedPaint).apply { color = Color.parseColor("#E65100") })
        }

        y += 146f

        // --- SECTION 2B: 7-DAY BOTTLENECK TABLE ---
        canvas.drawText("Date", margin + 4f, y, boldBodyPaint)
        canvas.drawText("Avg CPU", margin + 96f, y, boldBodyPaint)
        canvas.drawText("Peak CPU", margin + 158f, y, boldBodyPaint)
        canvas.drawText("Avg RAM", margin + 224f, y, boldBodyPaint)
        canvas.drawText("Peak Temp", margin + 288f, y, boldBodyPaint)
        canvas.drawText("Primary Bottleneck", margin + 362f, y, boldBodyPaint)
        y += 6f
        canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
        y += 13f

        trends.forEach { pt ->
            val isHot = pt.bottleneckScore >= 70
            canvas.drawText("${pt.dayLabel} (${pt.dateIso.takeLast(5)})", margin + 4f, y, bodyPaint)
            canvas.drawText("${String.format(Locale.US, "%.0f", pt.avgCpuPct)}%", margin + 96f, y, bodyPaint)
            canvas.drawText("${String.format(Locale.US, "%.0f", pt.peakCpuPct)}%", margin + 158f, y, if (pt.peakCpuPct >= 75f) alertPaint else bodyPaint)
            canvas.drawText("${String.format(Locale.US, "%.0f", pt.avgRamPct)}%", margin + 224f, y, if (pt.avgRamPct >= 75f) alertPaint else bodyPaint)
            canvas.drawText("${String.format(Locale.US, "%.1f", pt.peakTempC)}°C", margin + 288f, y, if (pt.peakTempC >= 38f) alertPaint else bodyPaint)
            canvas.drawText(pt.primaryBottleneck.take(26), margin + 362f, y, if (isHot) alertPaint else bodyPaint)
            y += 15f
        }

        y += 14f

        // --- SECTION 3: HARDWARE & OPTIMIZATION LOGS (WITH AUTO PAGINATION) ---
        canvas.drawText("3. OPTIMIZATION & DIAGNOSTIC ACTIVITY LOGS (${logs.size} RECORDS)", margin, y, sectionTitlePaint)
        y += 14f

        fun drawLogTableHeader() {
            canvas.drawText("Timestamp", margin + 4f, y, boldBodyPaint)
            canvas.drawText("Type", margin + 112f, y, boldBodyPaint)
            canvas.drawText("Description", margin + 226f, y, boldBodyPaint)
            y += 6f
            canvas.drawLine(margin, y, pageWidth - margin, y, borderPaint)
            y += 13f
        }

        drawLogTableHeader()

        val logFmt = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault())
        val logsToPrint = logs.take(80)
        if (logsToPrint.isEmpty()) {
            canvas.drawText("No activity logs recorded yet.", margin + 4f, y, mutedPaint)
        } else {
            logsToPrint.forEach { log ->
                if (y > pageHeight - 48f) {
                    canvas.drawText("Page $pageNumber — Mobile Optimiser Report", margin, pageHeight - 20f, mutedPaint)
                    pdfDocument.finishPage(page)
                    pageNumber += 1
                    page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                    canvas = page.canvas
                    y = 44f
                    canvas.drawText("3. OPTIMIZATION & DIAGNOSTIC ACTIVITY LOGS (CONT.)", margin, y, sectionTitlePaint)
                    y += 14f
                    drawLogTableHeader()
                }
                val timeStr = logFmt.format(Date(log.timestamp))
                val typeStr = log.activityType.take(18)
                val descStr = log.description.replace("\n", " ").take(58)
                canvas.drawText(timeStr, margin + 4f, y, mutedPaint)
                canvas.drawText(typeStr, margin + 112f, y, boldBodyPaint)
                canvas.drawText(descStr, margin + 226f, y, bodyPaint)
                y += 15f
            }
        }

        canvas.drawText("Page $pageNumber — Mobile Optimiser Report", margin, pageHeight - 20f, mutedPaint)
        pdfDocument.finishPage(page)

        val outFile = File(resolveAppExportDir(context), fileName)
        FileOutputStream(outFile).use { fos ->
            pdfDocument.writeTo(fos)
        }
        pdfDocument.close()

        val bytes = outFile.readBytes()
        val publicPath = saveCopyToPublicDownloads(context, fileName, "application/pdf", bytes)
            ?: outFile.absolutePath

        return ReportExportResult(
            format = "PDF",
            fileName = fileName,
            file = outFile,
            publicDisplayPath = publicPath,
            textPreview = "PDF Report ($pageNumber page${if (pageNumber > 1) "s" else ""}, ${bytes.size / 1024} KB) containing 7-day trend chart, battery health, and ${logsToPrint.size} logs.",
            byteSize = bytes.size.toLong()
        )
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String): Boolean {
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Mobile Optimiser Hardware Report (${file.name})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Share or Open Hardware Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }
}

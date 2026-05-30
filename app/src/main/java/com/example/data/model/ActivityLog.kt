package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "activity_logs")
@JsonClass(generateAdapter = true)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val activityType: String, // JUNK_CLEAN, BATTERY_SAVER, DEEP_SLEEP, BACKUP_SNAPSHOT, CLOUD_SYNC, THERMAL_ALERT
    val description: String,
    val detailsJson: String = "", // Holds encrypted system metadata snapshot
    val isEncrypted: Boolean = true
)

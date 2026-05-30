package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "backup_snapshots")
@JsonClass(generateAdapter = true)
data class BackupSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val note: String,
    val encryptedPayload: String, // String encrypted with CryptoHelper
    val recordsCount: Int
)

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActivityLog
import com.example.data.model.BackupSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLogsBetweenDates(startTime: Long, endTime: Long): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE activityType = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startTime AND timestamp <= :endTime AND activityType = :type ORDER BY timestamp DESC")
    fun getLogsFiltered(startTime: Long, endTime: Long, type: String): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ActivityLog>)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()

    // --- Backup Snapshot operations ---
    @Query("SELECT * FROM backup_snapshots ORDER BY timestamp DESC")
    fun getAllBackupSnapshots(): Flow<List<BackupSnapshot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupSnapshot(snapshot: BackupSnapshot): Long

    @Delete
    suspend fun deleteBackupSnapshot(snapshot: BackupSnapshot)
}

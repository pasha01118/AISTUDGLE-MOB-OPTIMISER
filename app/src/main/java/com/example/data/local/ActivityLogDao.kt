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

    // --- Agent Skills operations ---
    @Query("SELECT * FROM agent_skills ORDER BY lastUpdated DESC")
    fun getAllSkills(): Flow<List<com.example.data.model.AgentSkill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: com.example.data.model.AgentSkill): Long

    @Query("DELETE FROM agent_skills WHERE id = :id")
    suspend fun deleteSkillById(id: Long)

    // --- Agent Memories operations ---
    @Query("SELECT * FROM agent_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<com.example.data.model.AgentMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: com.example.data.model.AgentMemory): Long

    @Query("DELETE FROM agent_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM agent_memories")
    suspend fun clearAllMemories()
}

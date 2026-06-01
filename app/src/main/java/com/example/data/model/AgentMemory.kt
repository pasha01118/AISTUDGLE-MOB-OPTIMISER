package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "agent_memories")
@JsonClass(generateAdapter = true)
data class AgentMemory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String, // BEHAVIOR, PREFERENCE, SYSTEM_RULE, OBSERVATION
    val content: String,
    val importance: Int = 5 // 1 to 10
)

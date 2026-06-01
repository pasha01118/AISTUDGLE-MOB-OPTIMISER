package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "agent_skills")
@JsonClass(generateAdapter = true)
data class AgentSkill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val level: Int = 1,
    val status: String = "ENABLED", // ENABLED, TUNING, SLEEPING
    val source: String = "Core", // Core, GitHub, Hugging Face
    val url: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

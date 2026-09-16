package com.jarvis.assistant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String, // "voice_command", "text_command", "tool_execution", "confirmation_prompt"
    val command: String,
    val riskLevel: String, // "LOW", "MEDIUM", "HIGH"
    val result: String, // "SUCCESS", "FAILURE", "CONFIRMED", "REJECTED"
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

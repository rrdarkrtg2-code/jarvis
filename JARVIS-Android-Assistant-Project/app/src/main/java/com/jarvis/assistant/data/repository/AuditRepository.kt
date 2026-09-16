package com.jarvis.assistant.data.repository

import com.jarvis.assistant.data.local.dao.AuditLogDao
import com.jarvis.assistant.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

class AuditRepository(private val auditLogDao: AuditLogDao) {
    val recentLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()

    suspend fun recordAction(
        actionType: String,
        command: String,
        riskLevel: String,
        result: String,
        details: String? = null
    ) {
        val entity = AuditLogEntity(
            actionType = actionType,
            command = command,
            riskLevel = riskLevel,
            result = result,
            details = details,
            timestamp = System.currentTimeMillis()
        )
        auditLogDao.insertLog(entity)
    }

    suspend fun clearLogs() {
        auditLogDao.clearLogs()
    }
}

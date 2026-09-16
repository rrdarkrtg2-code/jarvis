package com.jarvis.assistant.data.repository

import com.jarvis.assistant.data.local.dao.ReminderDao
import com.jarvis.assistant.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()

    suspend fun addReminder(title: String, triggerTimeMillis: Long, isRecurring: Boolean = false, recurrenceRule: String? = null): Long {
        val entity = ReminderEntity(
            title = title.trim(),
            triggerTimeMillis = triggerTimeMillis,
            isRecurring = isRecurring,
            recurrenceRule = recurrenceRule
        )
        return reminderDao.insertReminder(entity)
    }

    suspend fun getActiveRemindersList(): List<ReminderEntity> {
        return reminderDao.getActiveRemindersList()
    }

    suspend fun completeReminder(id: Long) {
        reminderDao.markCompleted(id)
    }

    suspend fun deleteReminder(id: Long) {
        reminderDao.deleteById(id)
    }

    suspend fun clearAll() {
        reminderDao.clearAll()
    }
}

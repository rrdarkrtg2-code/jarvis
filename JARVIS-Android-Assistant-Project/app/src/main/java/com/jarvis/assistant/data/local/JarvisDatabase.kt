package com.jarvis.assistant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jarvis.assistant.data.local.dao.AuditLogDao
import com.jarvis.assistant.data.local.dao.ConversationDao
import com.jarvis.assistant.data.local.dao.MemoryDao
import com.jarvis.assistant.data.local.dao.PreferenceDao
import com.jarvis.assistant.data.local.dao.ReminderDao
import com.jarvis.assistant.data.local.entity.AuditLogEntity
import com.jarvis.assistant.data.local.entity.ConversationEntity
import com.jarvis.assistant.data.local.entity.MemoryEntity
import com.jarvis.assistant.data.local.entity.MessageEntity
import com.jarvis.assistant.data.local.entity.PreferenceEntity
import com.jarvis.assistant.data.local.entity.ReminderEntity

@Database(
    entities = [
        MemoryEntity::class,
        ReminderEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        AuditLogEntity::class,
        PreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun reminderDao(): ReminderDao
    abstract fun conversationDao(): ConversationDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun preferenceDao(): PreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_core.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

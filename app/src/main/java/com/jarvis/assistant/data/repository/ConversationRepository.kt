package com.jarvis.assistant.data.repository

import com.jarvis.assistant.data.local.dao.ConversationDao
import com.jarvis.assistant.data.local.entity.ConversationEntity
import com.jarvis.assistant.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversationDao: ConversationDao) {
    val conversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    suspend fun createConversation(title: String): Long {
        val entity = ConversationEntity(title = title)
        return conversationDao.insertConversation(entity)
    }

    fun getMessages(conversationId: Long): Flow<List<MessageEntity>> {
        return conversationDao.getMessagesForConversation(conversationId)
    }

    suspend fun getRecentMessages(conversationId: Long, limit: Int = 10): List<MessageEntity> {
        return conversationDao.getRecentMessages(conversationId, limit).reversed()
    }

    suspend fun addMessage(conversationId: Long, sender: String, content: String): Long {
        val msg = MessageEntity(
            conversationId = conversationId,
            sender = sender,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val id = conversationDao.insertMessage(msg)
        conversationDao.updateTimestamp(conversationId, System.currentTimeMillis())
        return id
    }

    suspend fun clearAll() {
        conversationDao.clearAll()
    }
}

package com.jarvis.assistant.data.repository

import com.jarvis.assistant.data.local.dao.MemoryDao
import com.jarvis.assistant.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun addMemory(category: String, content: String, importance: Int = 1): Long {
        val memory = MemoryEntity(
            category = category.trim().lowercase(),
            content = content.trim(),
            importance = importance
        )
        return memoryDao.insertMemory(memory)
    }

    suspend fun searchMemories(query: String): List<MemoryEntity> {
        return memoryDao.searchMemories(query.trim())
    }

    suspend fun getRelevantContextForQuery(query: String, limit: Int = 5): List<MemoryEntity> {
        val all = memoryDao.getAllMemoriesList()
        if (all.isEmpty()) return emptyList()

        val keywords = query.lowercase().split("\\s+".toRegex()).filter { it.length > 2 }
        if (keywords.isEmpty()) {
            return all.take(limit)
        }

        // Score memories by keyword overlap and importance
        return all.map { mem ->
            var score = mem.importance
            val memContent = mem.content.lowercase()
            for (kw in keywords) {
                if (memContent.contains(kw)) score += 3
            }
            Pair(mem, score)
        }
        .filter { it.second > 1 }
        .sortedByDescending { it.second }
        .map { it.first }
        .take(limit)
    }

    suspend fun deleteMemory(id: Long) {
        memoryDao.deleteById(id)
    }

    suspend fun clearAll() {
        memoryDao.clearAll()
    }
}

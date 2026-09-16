package com.jarvis.assistant.ai

import com.jarvis.assistant.automation.DeviceController
import com.jarvis.assistant.data.repository.ConversationRepository
import com.jarvis.assistant.data.repository.MemoryRepository

class AIContextBuilder(
    private val memoryRepository: MemoryRepository,
    private val conversationRepository: ConversationRepository,
    private val deviceController: DeviceController
) {
    suspend fun buildContext(query: String, conversationId: Long?): AIRequestContext {
        val memories = memoryRepository.getRelevantContextForQuery(query).map { "${it.category.uppercase()}: ${it.content}" }
        val recentMessages = if (conversationId != null) {
            conversationRepository.getRecentMessages(conversationId, 6).map { Pair(it.sender, it.content) }
        } else emptyList()

        val status = mapOf(
            "time" to deviceController.getCurrentTime(),
            "date" to deviceController.getCurrentDate(),
            "battery" to deviceController.getBatteryLevel()
        )

        return AIRequestContext(
            conversationHistory = recentMessages,
            relevantMemories = memories,
            deviceStatus = status,
            availableTools = ToolRegistry.tools
        )
    }
}

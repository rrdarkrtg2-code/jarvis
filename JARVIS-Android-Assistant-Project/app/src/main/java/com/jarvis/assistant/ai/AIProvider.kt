package com.jarvis.assistant.ai

data class AIRequestContext(
    val conversationHistory: List<Pair<String, String>> = emptyList(), // Pair(sender, message)
    val relevantMemories: List<String> = emptyList(),
    val deviceStatus: Map<String, String> = emptyMap(),
    val availableTools: List<ToolDefinition> = emptyList()
)

data class AIResponse(
    val text: String,
    val toolCall: ToolCall? = null,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class ToolCall(
    val name: String,
    val arguments: Map<String, Any>
)

interface AIProvider {
    val name: String
    suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse
    suspend fun testConnection(): Boolean
}

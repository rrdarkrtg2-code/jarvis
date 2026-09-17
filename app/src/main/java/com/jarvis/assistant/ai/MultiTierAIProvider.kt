package com.jarvis.assistant.ai

class MultiTierAIProvider(
    private val primaryProvider: AIProvider,
    private val fallbackProvider: AIProvider?
) : AIProvider {

    override val name: String
        get() = "${primaryProvider.name} (Fallback: ${fallbackProvider?.name ?: "None"})"

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse {
        val primaryResponse = primaryProvider.generateResponse(userPrompt, context)
        if (primaryResponse.isSuccess) {
            return primaryResponse
        }

        if (fallbackProvider != null) {
            val fallbackResponse = fallbackProvider.generateResponse(userPrompt, context)
            if (fallbackResponse.isSuccess) {
                return fallbackResponse
            }
        }

        return primaryResponse
    }

    override suspend fun testConnection(): Boolean {
        return primaryProvider.testConnection() || (fallbackProvider?.testConnection() == true)
    }
}

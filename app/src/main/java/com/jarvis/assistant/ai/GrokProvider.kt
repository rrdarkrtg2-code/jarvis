package com.jarvis.assistant.ai

class GrokProvider(
    apiKeyProvider: () -> String,
    model: String = "grok-2-mini"
) : AIProvider {
    override val name: String = "xAI Grok"

    private val delegate = OpenAIProvider(
        apiKeyProvider = apiKeyProvider,
        model = model,
        endpoint = "https://api.x.ai/v1/chat/completions"
    )

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse {
        return delegate.generateResponse(userPrompt, context)
    }

    override suspend fun testConnection(): Boolean {
        return delegate.testConnection()
    }
}

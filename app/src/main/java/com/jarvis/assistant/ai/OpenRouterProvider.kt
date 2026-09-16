package com.jarvis.assistant.ai

class OpenRouterProvider(
    apiKeyProvider: () -> String,
    model: String = "meta-llama/llama-3.1-8b-instruct"
) : AIProvider {
    override val name: String = "OpenRouter"

    private val delegate = OpenAIProvider(
        apiKeyProvider = apiKeyProvider,
        model = model,
        endpoint = "https://openrouter.ai/api/v1/chat/completions"
    )

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse {
        return delegate.generateResponse(userPrompt, context)
    }

    override suspend fun testConnection(): Boolean {
        return delegate.testConnection()
    }
}

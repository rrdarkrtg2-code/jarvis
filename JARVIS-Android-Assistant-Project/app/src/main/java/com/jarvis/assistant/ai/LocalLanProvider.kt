package com.jarvis.assistant.ai

class LocalLanProvider(
    private val endpointUrl: () -> String,
    private val modelName: () -> String
) : AIProvider {
    override val name: String = "Local LAN / Ollama"

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse {
        val endpoint = endpointUrl().ifEmpty { "http://192.168.1.100:11434/v1/chat/completions" }
        val model = modelName().ifEmpty { "llama3" }

        val delegate = OpenAIProvider(
            apiKeyProvider = { "local-lan" },
            model = model,
            endpoint = endpoint
        )
        return delegate.generateResponse(userPrompt, context)
    }

    override suspend fun testConnection(): Boolean {
        return try {
            val endpoint = endpointUrl()
            val req = okhttp3.Request.Builder().url(endpoint).get().build()
            val res = okhttp3.OkHttpClient().newCall(req).execute()
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

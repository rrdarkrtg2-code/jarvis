package com.jarvis.assistant.ai

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class OpenAIProvider(
    private val apiKeyProvider: () -> String,
    private val model: String = "gpt-4o-mini",
    private val endpoint: String = "https://api.openai.com/v1/chat/completions"
) : AIProvider {

    override val name: String = "OpenAI"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateResponse(userPrompt: String, context: AIRequestContext): AIResponse = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty()) {
            return@withContext AIResponse(
                text = "OpenAI API key is not configured. Please set your key in Settings -> AI Providers.",
                isSuccess = false,
                errorMessage = "Missing API Key"
            )
        }

        val root = JsonObject()
        root.addProperty("model", model)

        val messages = JsonArray()

        // System prompt
        val sysMsg = JsonObject()
        sysMsg.addProperty("role", "system")
        sysMsg.addProperty("content", buildString {
            append("You are J.A.R.V.I.S., an intelligent, calm, concise personal AI operating assistant.\n")
            if (context.deviceStatus.isNotEmpty()) append("Device status: ${context.deviceStatus}\n")
            if (context.relevantMemories.isNotEmpty()) {
                append("Relevant user memories:\n")
                context.relevantMemories.forEach { append("- $it\n") }
            }
            append("\n${ToolRegistry.getToolPromptDescription()}\n")
        })
        messages.add(sysMsg)

        // History
        for ((sender, content) in context.conversationHistory) {
            val msg = JsonObject()
            msg.addProperty("role", if (sender == "user") "user" else "assistant")
            msg.addProperty("content", content)
            messages.add(msg)
        }

        // Current user message
        val curMsg = JsonObject()
        curMsg.addProperty("role", "user")
        curMsg.addProperty("content", userPrompt)
        messages.add(curMsg)

        root.add("messages", messages)

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(root.toString().toRequestBody(jsonMedia))
            .build()

        return@withContext try {
            val res = client.newCall(request).execute()
            val body = res.body?.string() ?: ""
            if (!res.isSuccessful) {
                return@withContext AIResponse("OpenAI API error: HTTP ${res.code}", isSuccess = false, errorMessage = body)
            }

            val resObj = gson.fromJson(body, JsonObject::class.java)
            val choices = resObj.getAsJsonArray("choices")
            if (choices == null || choices.size() == 0) {
                return@withContext AIResponse("Empty response received from OpenAI.", isSuccess = false)
            }

            val reply = choices.get(0).asJsonObject.getAsJsonObject("message").get("content")?.asString ?: ""
            val toolCall = ToolCallParser.extractToolCall(reply)
            AIResponse(text = reply, toolCall = toolCall, isSuccess = true)
        } catch (e: Exception) {
            AIResponse("Connection failed to OpenAI service.", isSuccess = false, errorMessage = e.localizedMessage)
        }
    }

    override suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty()) return@withContext false
        val req = Request.Builder()
            .url("https://api.openai.com/v1/models")
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()
        return@withContext try {
            val res = client.newCall(req).execute()
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

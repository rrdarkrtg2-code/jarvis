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

class GeminiProvider(
    private val apiKeyProvider: () -> String,
    private val model: String = "gemini-1.5-flash"
) : AIProvider {

    override val name: String = "Google Gemini"

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
                text = "Google Gemini API key is not configured. Please set your key in Settings -> AI Providers.",
                isSuccess = false,
                errorMessage = "Missing API Key"
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val root = JsonObject()
        val contents = JsonArray()

        // System instructions & context
        val systemPrompt = buildString {
            append("You are J.A.R.V.I.S., an intelligent, calm, concise personal AI operating assistant.\n")
            append("Respond naturally, professionally, and concisely without unnecessary filler.\n")
            if (context.deviceStatus.isNotEmpty()) {
                append("Device status: ${context.deviceStatus}\n")
            }
            if (context.relevantMemories.isNotEmpty()) {
                append("Long-term memories retrieved:\n")
                context.relevantMemories.forEach { append("- $it\n") }
            }
            append("\n${ToolRegistry.getToolPromptDescription()}\n")
        }

        val sysContent = JsonObject()
        sysContent.addProperty("role", "user")
        val sysParts = JsonArray()
        val sysPart = JsonObject()
        sysPart.addProperty("text", systemPrompt)
        sysParts.add(sysPart)
        sysContent.add("parts", sysParts)
        contents.add(sysContent)

        // Conversation history
        for ((sender, text) in context.conversationHistory) {
            val historyContent = JsonObject()
            historyContent.addProperty("role", if (sender == "user") "user" else "model")
            val parts = JsonArray()
            val p = JsonObject()
            p.addProperty("text", text)
            parts.add(p)
            historyContent.add("parts", parts)
            contents.add(historyContent)
        }

        // Current query
        val currentContent = JsonObject()
        currentContent.addProperty("role", "user")
        val currentParts = JsonArray()
        val curPart = JsonObject()
        curPart.addProperty("text", userPrompt)
        currentParts.add(curPart)
        currentContent.add("parts", currentParts)
        contents.add(currentContent)

        root.add("contents", contents)

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMedia))
            .build()

        return@withContext try {
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext AIResponse(
                    text = "Gemini API error (${response.code}). Please check your key or network.",
                    isSuccess = false,
                    errorMessage = body
                )
            }

            val resObj = gson.fromJson(body, JsonObject::class.java)
            val candidates = resObj.getAsJsonArray("candidates")
            if (candidates == null || candidates.size() == 0) {
                return@withContext AIResponse("No response received from Gemini.", isSuccess = false)
            }

            val content = candidates.get(0).asJsonObject.getAsJsonObject("content")
            val parts = content.getAsJsonArray("parts")
            val replyText = parts.get(0).asJsonObject.get("text")?.asString ?: ""

            val toolCall = ToolCallParser.extractToolCall(replyText)
            AIResponse(text = replyText, toolCall = toolCall, isSuccess = true)
        } catch (e: Exception) {
            AIResponse(
                text = "Network connection failed. Unable to reach Gemini.",
                isSuccess = false,
                errorMessage = e.localizedMessage
            )
        }
    }

    override suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider().trim()
        if (apiKey.isEmpty()) return@withContext false
        val url = "https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey"
        return@withContext try {
            val req = Request.Builder().url(url).get().build()
            val res = client.newCall(req).execute()
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}

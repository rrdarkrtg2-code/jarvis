package com.jarvis.assistant.ai

import com.google.gson.Gson
import com.google.gson.JsonObject

object ToolCallParser {

    private val gson = Gson()

    fun extractToolCall(text: String): ToolCall? {
        val jsonPattern = Regex("""```json\s*(\{.*?\})\s*```""", RegexOption.DOT_MATCHES_ALL)
        val match = jsonPattern.find(text)
        val jsonStr = match?.groupValues?.get(1) ?: run {
            val rawPattern = Regex("""\{"tool":\s*"[^"]+",\s*"arguments":\s*\{.*?\}\}""", RegexOption.DOT_MATCHES_ALL)
            rawPattern.find(text)?.value
        } ?: return null

        return try {
            val obj = gson.fromJson(jsonStr, JsonObject::class.java)
            val toolName = obj.get("tool")?.asString ?: return null
            val argsObj = obj.getAsJsonObject("arguments") ?: JsonObject()
            val argsMap = mutableMapOf<String, Any>()
            for (key in argsObj.keySet()) {
                val elem = argsObj.get(key)
                if (elem.isJsonPrimitive) {
                    val prim = elem.asJsonPrimitive
                    if (prim.isBoolean) argsMap[key] = prim.asBoolean
                    else if (prim.isNumber) argsMap[key] = prim.asNumber
                    else argsMap[key] = prim.asString
                }
            }
            ToolCall(toolName, argsMap)
        } catch (e: Exception) {
            null
        }
    }
}

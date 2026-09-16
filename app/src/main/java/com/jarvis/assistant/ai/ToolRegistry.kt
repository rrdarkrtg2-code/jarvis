package com.jarvis.assistant.ai

data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)

object ToolRegistry {

    val tools = listOf(
        ToolDefinition(
            name = "open_app",
            description = "Opens an installed Android application by name.",
            parameters = mapOf("app_name" to "String: Name of the application (e.g. YouTube, WhatsApp)")
        ),
        ToolDefinition(
            name = "search_web",
            description = "Searches the web via browser for queries.",
            parameters = mapOf("query" to "String: The search query")
        ),
        ToolDefinition(
            name = "search_youtube",
            description = "Searches YouTube for videos or music.",
            parameters = mapOf("query" to "String: Search terms")
        ),
        ToolDefinition(
            name = "get_battery",
            description = "Gets current battery percentage and charging state.",
            parameters = emptyMap()
        ),
        ToolDefinition(
            name = "toggle_flashlight",
            description = "Turns device flashlight/torch on or off.",
            parameters = mapOf("enable" to "Boolean: true to turn on, false to turn off")
        ),
        ToolDefinition(
            name = "create_reminder",
            description = "Creates a local reminder with title and minutes from now.",
            parameters = mapOf(
                "title" to "String: Reminder message",
                "minutes_from_now" to "Int: Duration in minutes"
            )
        ),
        ToolDefinition(
            name = "store_memory",
            description = "Saves important facts or user preferences to long-term memory.",
            parameters = mapOf(
                "category" to "String: fact, preference, or project",
                "content" to "String: Detail to remember"
            )
        )
    )

    fun getToolPromptDescription(): String {
        val sb = StringBuilder()
        sb.append("You have access to the following Android tools. If the user's intent matches a tool, respond with a JSON block in the exact format:\n")
        sb.append("```json\n{\"tool\": \"tool_name\", \"arguments\": {\"arg\": \"value\"}}\n```\n")
        sb.append("Available tools:\n")
        for (t in tools) {
            sb.append("- ${t.name}: ${t.description} Params: ${t.parameters}\n")
        }
        sb.append("If no tool is required, simply provide a concise, intelligent, calm response in character as J.A.R.V.I.S.\n")
        return sb.toString()
    }
}

package com.jarvis.assistant.engine

import com.jarvis.assistant.ai.AIContextBuilder
import com.jarvis.assistant.ai.AIProvider
import com.jarvis.assistant.automation.AccessibilityController
import com.jarvis.assistant.automation.AppDiscoveryManager
import com.jarvis.assistant.automation.AppLaunchResult
import com.jarvis.assistant.automation.DeviceController
import com.jarvis.assistant.automation.NotificationController
import com.jarvis.assistant.data.repository.AuditRepository
import com.jarvis.assistant.data.repository.ConversationRepository
import com.jarvis.assistant.data.repository.MemoryRepository
import com.jarvis.assistant.reminders.ReminderManager

data class AssistantResponse(
    val spokenText: String,
    val displayText: String = spokenText,
    val pendingConfirmation: ConfirmationRequest? = null
)

class IntentRouter(
    private val localCommandEngine: LocalCommandEngine,
    private val deviceController: DeviceController,
    private val appDiscoveryManager: AppDiscoveryManager,
    private val memoryRepository: MemoryRepository,
    private val reminderManager: ReminderManager,
    private val conversationRepository: ConversationRepository,
    private val auditRepository: AuditRepository,
    private val aiContextBuilder: AIContextBuilder,
    private val confirmationSystem: ConfirmationSystem,
    private val aiProviderSelector: () -> AIProvider?
) {

    suspend fun processQuery(query: String, conversationId: Long?): AssistantResponse {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AssistantResponse("Yes, I am listening.", "How can I assist you?")
        }

        // 1. Math / Calculator
        val calcResult = CalculatorEngine.evaluate(trimmed)
        if (calcResult != null) {
            val reply = "The result is $calcResult."
            auditRepository.recordAction("calculator", trimmed, "LOW", "SUCCESS", reply)
            return AssistantResponse(reply)
        }

        // 2. Deterministic Local Commands
        val pattern = localCommandEngine.parseCommand(trimmed)
        if (pattern != null) {
            val localResponse = handleLocalPattern(pattern, trimmed)
            if (localResponse != null) {
                return localResponse
            }
        }

        // 3. Fallback to AI Provider
        val provider = aiProviderSelector()
        if (provider != null) {
            try {
                val context = aiContextBuilder.buildContext(trimmed, conversationId)
                val aiResponse = provider.generateResponse(trimmed, context)

                if (aiResponse.isSuccess) {
                    val toolCall = aiResponse.toolCall
                    if (toolCall != null) {
                        return handleAIToolCall(toolCall, trimmed)
                    }
                    auditRepository.recordAction("ai_query", trimmed, "LOW", "SUCCESS", aiResponse.text)
                    return AssistantResponse(aiResponse.text)
                } else {
                    val errorMsg = aiResponse.errorMessage ?: "AI Provider error"
                    auditRepository.recordAction("ai_query", trimmed, "LOW", "FAILURE", errorMsg)
                    return AssistantResponse(aiResponse.text)
                }
            } catch (e: Exception) {
                return AssistantResponse("I encountered an issue contacting the AI service. Please verify your connection or API key.")
            }
        }

        // 4. No AI Provider Configured
        val defaultMsg = "I couldn't match a local command for that request. To enable conversational AI, please configure an API key in Settings -> AI Providers."
        auditRepository.recordAction("unknown_command", trimmed, "LOW", "UNHANDLED", defaultMsg)
        return AssistantResponse(defaultMsg)
    }

    private suspend fun handleLocalPattern(pattern: CommandPattern, rawQuery: String): AssistantResponse? {
        return when (pattern) {
            is CommandPattern.GetBattery -> {
                val status = deviceController.getBatteryLevel()
                auditRepository.recordAction("device_query", rawQuery, "LOW", "SUCCESS", status)
                AssistantResponse(status)
            }
            is CommandPattern.ToggleFlashlight -> {
                val success = deviceController.setFlashlight(pattern.enable)
                val msg = if (success) {
                    if (pattern.enable) "Flashlight turned on." else "Flashlight turned off."
                } else {
                    "Unable to toggle flashlight. Device camera or flash hardware might be unavailable."
                }
                auditRepository.recordAction("hardware_control", rawQuery, "LOW", if (success) "SUCCESS" else "FAILURE", msg)
                AssistantResponse(msg)
            }
            is CommandPattern.GetTime -> {
                val time = deviceController.getCurrentTime()
                AssistantResponse(time)
            }
            is CommandPattern.GetDate -> {
                val date = deviceController.getCurrentDate()
                AssistantResponse(date)
            }
            is CommandPattern.GetDay -> {
                val day = deviceController.getCurrentDay()
                AssistantResponse(day)
            }
            is CommandPattern.AdjustVolume -> {
                val res = deviceController.adjustVolume(pattern.up)
                AssistantResponse(res)
            }
            is CommandPattern.GoHome -> {
                val success = AccessibilityController.goHome()
                val msg = if (success) "Navigating to home screen." else "Accessibility service is required to navigate home."
                AssistantResponse(msg)
            }
            is CommandPattern.GoBack -> {
                val success = AccessibilityController.goBack()
                val msg = if (success) "Going back." else "Accessibility service is required to go back."
                AssistantResponse(msg)
            }
            is CommandPattern.ShowRecentApps -> {
                val success = AccessibilityController.showRecents()
                val msg = if (success) "Showing recent apps." else "Accessibility service is required to show recents."
                AssistantResponse(msg)
            }
            is CommandPattern.TakeScreenshot -> {
                val success = AccessibilityController.takeScreenshot()
                val msg = if (success) "Screenshot captured." else "Accessibility service is required to take screenshots."
                AssistantResponse(msg)
            }
            is CommandPattern.OpenSettings -> {
                val success = deviceController.openSettings(pattern.type)
                val msg = if (success) "Opening settings." else "Could not open settings."
                AssistantResponse(msg)
            }
            is CommandPattern.OpenApp -> {
                when (val res = appDiscoveryManager.findAndLaunchApp(pattern.appName)) {
                    is AppLaunchResult.Launched -> {
                        val msg = "Opening ${res.appName}."
                        auditRepository.recordAction("app_launch", rawQuery, "LOW", "SUCCESS", msg)
                        AssistantResponse(msg)
                    }
                    is AppLaunchResult.DisambiguationRequired -> {
                        val msg = "Found multiple matching apps: ${res.candidates.joinToString(", ")}. Which one should I open?"
                        AssistantResponse(msg)
                    }
                    is AppLaunchResult.NotFound -> {
                        val msg = "I couldn't find '${pattern.appName}' installed on this device."
                        AssistantResponse(msg)
                    }
                }
            }
            is CommandPattern.SearchYouTube -> {
                deviceController.searchYouTube(pattern.query)
                AssistantResponse("Searching YouTube for '${pattern.query}'.")
            }
            is CommandPattern.SearchWeb -> {
                deviceController.searchWeb(pattern.query)
                AssistantResponse("Searching the web for '${pattern.query}'.")
            }
            is CommandPattern.ShowReminders -> {
                val reminders = reminderManager.toString()
                AssistantResponse("Displaying your active reminders.")
            }
            is CommandPattern.CreateReminder -> {
                val delay = extractMinutes(pattern.query)
                val title = cleanReminderTitle(pattern.query)
                val msg = reminderManager.scheduleReminder(title, delay)
                auditRepository.recordAction("reminder_create", rawQuery, "MEDIUM", "SUCCESS", msg)
                AssistantResponse(msg)
            }
            is CommandPattern.StoreMemory -> {
                memoryRepository.addMemory("user_fact", pattern.content, 2)
                val msg = "Understood. I will remember that."
                auditRepository.recordAction("memory_store", rawQuery, "LOW", "SUCCESS", pattern.content)
                AssistantResponse(msg)
            }
            is CommandPattern.RecallMemory -> {
                val memories = memoryRepository.getRelevantContextForQuery(pattern.query)
                val msg = if (memories.isNotEmpty()) {
                    "Here is what I remember: " + memories.joinToString("; ") { it.content }
                } else {
                    "I don't have any specific memories saved matching that."
                }
                AssistantResponse(msg)
            }
            is CommandPattern.AccessibilityAction -> {
                when (pattern.action) {
                    "scroll_down" -> {
                        val success = AccessibilityController.scroll(forward = true)
                        AssistantResponse(if (success) "Scrolled down." else "Could not scroll. Make sure Accessibility is enabled.")
                    }
                    "scroll_up" -> {
                        val success = AccessibilityController.scroll(forward = false)
                        AssistantResponse(if (success) "Scrolled up." else "Could not scroll.")
                    }
                    "tap_text" -> {
                        val res = AccessibilityController.clickByText(pattern.param)
                        AssistantResponse(res)
                    }
                    "read_screen" -> {
                        val summary = AccessibilityController.readVisibleScreen()
                        AssistantResponse(summary)
                    }
                    else -> null
                }
            }
            is CommandPattern.ReadNotifications -> {
                val summary = NotificationController.summarizeNotifications()
                AssistantResponse(summary)
            }
        }
    }

    private suspend fun handleAIToolCall(toolCall: com.jarvis.assistant.ai.ToolCall, rawQuery: String): AssistantResponse {
        return when (toolCall.name) {
            "open_app" -> {
                val app = toolCall.arguments["app_name"]?.toString() ?: ""
                val res = appDiscoveryManager.findAndLaunchApp(app)
                when (res) {
                    is AppLaunchResult.Launched -> AssistantResponse("Opening ${res.appName}.")
                    is AppLaunchResult.DisambiguationRequired -> AssistantResponse("Multiple apps found: ${res.candidates.joinToString(", ")}. Which one?")
                    is AppLaunchResult.NotFound -> AssistantResponse("I could not find '$app' on this device.")
                }
            }
            "search_web" -> {
                val q = toolCall.arguments["query"]?.toString() ?: ""
                deviceController.searchWeb(q)
                AssistantResponse("Searching web for '$q'.")
            }
            "search_youtube" -> {
                val q = toolCall.arguments["query"]?.toString() ?: ""
                deviceController.searchYouTube(q)
                AssistantResponse("Searching YouTube for '$q'.")
            }
            "get_battery" -> {
                AssistantResponse(deviceController.getBatteryLevel())
            }
            "toggle_flashlight" -> {
                val enable = toolCall.arguments["enable"] as? Boolean ?: true
                val ok = deviceController.setFlashlight(enable)
                AssistantResponse(if (ok) (if (enable) "Flashlight on." else "Flashlight off.") else "Flashlight unavailable.")
            }
            "create_reminder" -> {
                val title = toolCall.arguments["title"]?.toString() ?: "Reminder"
                val mins = (toolCall.arguments["minutes_from_now"] as? Number)?.toInt() ?: 10
                val msg = reminderManager.scheduleReminder(title, mins)
                AssistantResponse(msg)
            }
            "store_memory" -> {
                val cat = toolCall.arguments["category"]?.toString() ?: "fact"
                val content = toolCall.arguments["content"]?.toString() ?: ""
                memoryRepository.addMemory(cat, content)
                AssistantResponse("Saved to memory: $content")
            }
            else -> {
                AssistantResponse("Executed ${toolCall.name}.")
            }
        }
    }

    private fun extractMinutes(query: String): Int {
        val regex = Regex("""(?:in|after)\s+(\d+)\s*(?:minute|min|m)""")
        return regex.find(query)?.groupValues?.get(1)?.toIntOrNull() ?: 10
    }

    private fun cleanReminderTitle(query: String): String {
        return query.replace(Regex("""(?:in|after)\s+\d+\s*(?:minute|min|m)s?"""), "")
            .replace(Regex("""^to\s+"""), "")
            .trim()
            .ifEmpty { "General Reminder" }
    }
}

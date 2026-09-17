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
    private val usageLimitManager: UsageLimitManager,
    private val aiProviderSelector: () -> AIProvider?
) {

    suspend fun processQuery(query: String, conversationId: Long?): AssistantResponse {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return AssistantResponse("Yes, I am listening.", "How can I assist you?")
        }

        // 1. Math / Calculator (Always 100% FREE & OFFLINE)
        val calcResult = CalculatorEngine.evaluate(trimmed)
        if (calcResult != null) {
            val reply = "The result is $calcResult."
            auditRepository.recordAction("calculator", trimmed, "LOW", "SUCCESS", reply)
            return AssistantResponse(reply)
        }

        // 2. Deterministic Local Commands (Always 100% FREE & OFFLINE)
        val pattern = localCommandEngine.parseCommand(trimmed)
        if (pattern != null) {
            val localResponse = handleLocalPattern(pattern, trimmed)
            if (localResponse != null) {
                return localResponse
            }
        }

        // 3. Conversational AI Queries (Subject to Free Usage Limit / Quota)
        if (!usageLimitManager.hasAvailableTime()) {
            val quotaMsg = "Your 5 hours of free AI talk time has expired. Please watch a quick ad in Settings or on the Home screen to renew and add 1 more hour of AI time."
            auditRepository.recordAction("ai_query", trimmed, "LOW", "LIMIT_REACHED", quotaMsg)
            return AssistantResponse(quotaMsg)
        }

        val provider = aiProviderSelector()
        if (provider != null) {
            try {
                usageLimitManager.deductTime(12L)

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
                return AssistantResponse("I encountered an issue contacting the AI service. Please verify your connection.")
            }
        }

        val defaultMsg = "I couldn't match a local command for that request. AI services are currently unconfigured by the administrator."
        auditRepository.recordAction("unknown_command", trimmed, "LOW", "UNHANDLED", defaultMsg)
        return AssistantResponse(defaultMsg)
    }

    private suspend fun handleLocalPattern(pattern: CommandPattern, rawQuery: String): AssistantResponse? {
        return when (pattern) {
            is CommandPattern.GetBattery -> AssistantResponse(deviceController.getBatteryLevel())
            is CommandPattern.ToggleFlashlight -> {
                val success = deviceController.setFlashlight(pattern.enable)
                AssistantResponse(if (success) (if (pattern.enable) "Flashlight turned on." else "Flashlight turned off.") else "Unable to toggle flashlight.")
            }
            is CommandPattern.GetTime -> AssistantResponse(deviceController.getCurrentTime())
            is CommandPattern.GetDate -> AssistantResponse(deviceController.getCurrentDate())
            is CommandPattern.GetDay -> AssistantResponse(deviceController.getCurrentDay())
            is CommandPattern.AdjustVolume -> AssistantResponse(deviceController.adjustVolume(pattern.up))
            is CommandPattern.GoHome -> AssistantResponse(if (AccessibilityController.goHome()) "Navigating home." else "Accessibility required.")
            is CommandPattern.GoBack -> AssistantResponse(if (AccessibilityController.goBack()) "Going back." else "Accessibility required.")
            is CommandPattern.ShowRecentApps -> AssistantResponse(if (AccessibilityController.showRecents()) "Showing recents." else "Accessibility required.")
            is CommandPattern.TakeScreenshot -> AssistantResponse(if (AccessibilityController.takeScreenshot()) "Screenshot captured." else "Accessibility required.")
            is CommandPattern.OpenSettings -> AssistantResponse(if (deviceController.openSettings(pattern.type)) "Opening settings." else "Could not open settings.")
            is CommandPattern.OpenApp -> {
                when (val res = appDiscoveryManager.findAndLaunchApp(pattern.appName)) {
                    is AppLaunchResult.Launched -> AssistantResponse("Opening ${res.appName}.")
                    is AppLaunchResult.DisambiguationRequired -> AssistantResponse("Found multiple apps: ${res.candidates.joinToString(", ")}. Which one?")
                    is AppLaunchResult.NotFound -> AssistantResponse("I couldn't find '${pattern.appName}' installed on this device.")
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
            is CommandPattern.ShowReminders -> AssistantResponse("Displaying active reminders.")
            is CommandPattern.CreateReminder -> {
                val delay = extractMinutes(pattern.query)
                val title = cleanReminderTitle(pattern.query)
                val msg = reminderManager.scheduleReminder(title, delay)
                AssistantResponse(msg)
            }
            is CommandPattern.StoreMemory -> {
                memoryRepository.addMemory("user_fact", pattern.content, 2)
                AssistantResponse("Understood. I will remember that.")
            }
            is CommandPattern.RecallMemory -> {
                val memories = memoryRepository.getRelevantContextForQuery(pattern.query)
                AssistantResponse(if (memories.isNotEmpty()) "I remember: " + memories.joinToString("; ") { it.content } else "No memories found matching that.")
            }
            is CommandPattern.AccessibilityAction -> {
                when (pattern.action) {
                    "scroll_down" -> AssistantResponse(if (AccessibilityController.scroll(forward = true)) "Scrolled down." else "Could not scroll.")
                    "scroll_up" -> AssistantResponse(if (AccessibilityController.scroll(forward = false)) "Scrolled up." else "Could not scroll.")
                    "tap_text" -> AssistantResponse(AccessibilityController.clickByText(pattern.param))
                    "read_screen" -> AssistantResponse(AccessibilityController.readVisibleScreen())
                    else -> null
                }
            }
            is CommandPattern.ReadNotifications -> AssistantResponse(NotificationController.summarizeNotifications())
        }
    }

    private suspend fun handleAIToolCall(toolCall: com.jarvis.assistant.ai.ToolCall, rawQuery: String): AssistantResponse {
        return when (toolCall.name) {
            "open_app" -> {
                val app = toolCall.arguments["app_name"]?.toString() ?: ""
                val res = appDiscoveryManager.findAndLaunchApp(app)
                when (res) {
                    is AppLaunchResult.Launched -> AssistantResponse("Opening ${res.appName}.")
                    is AppLaunchResult.DisambiguationRequired -> AssistantResponse("Multiple apps: ${res.candidates.joinToString(", ")}. Which one?")
                    is AppLaunchResult.NotFound -> AssistantResponse("Couldn't find '$app' on this device.")
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
            "get_battery" -> AssistantResponse(deviceController.getBatteryLevel())
            "toggle_flashlight" -> {
                val enable = toolCall.arguments["enable"] as? Boolean ?: true
                val ok = deviceController.setFlashlight(enable)
                AssistantResponse(if (ok) (if (enable) "Flashlight on." else "Flashlight off.") else "Flashlight unavailable.")
            }
            "create_reminder" -> {
                val title = toolCall.arguments["title"]?.toString() ?: "Reminder"
                val mins = (toolCall.arguments["minutes_from_now"] as? Number)?.toInt() ?: 10
                AssistantResponse(reminderManager.scheduleReminder(title, mins))
            }
            "store_memory" -> {
                val cat = toolCall.arguments["category"]?.toString() ?: "fact"
                val content = toolCall.arguments["content"]?.toString() ?: ""
                memoryRepository.addMemory(cat, content)
                AssistantResponse("Saved to memory: $content")
            }
            else -> AssistantResponse("Executed ${toolCall.name}.")
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

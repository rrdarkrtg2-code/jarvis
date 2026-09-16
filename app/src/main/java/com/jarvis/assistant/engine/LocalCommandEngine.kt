package com.jarvis.assistant.engine

sealed class CommandResult {
    data class Success(val speechResponse: String, val displayMessage: String = speechResponse) : CommandResult()
    data class Failure(val errorMessage: String) : CommandResult()
    data class RequiresConfirmation(val request: ConfirmationRequest) : CommandResult()
    object NotHandled : CommandResult()
}

class LocalCommandEngine {

    fun parseCommand(rawText: String): CommandPattern? {
        val text = rawText.lowercase().trim()
            .replace(Regex("^(hey\\s+)?jarvis[,\\s]*"), "")
            .trim()

        return when {
            // Battery queries
            text.contains("battery") || text.contains("battery percentage") || text.contains("battery kitni") -> {
                CommandPattern.GetBattery
            }

            // Flashlight / Torch
            text.contains("flashlight on") || text.contains("turn on flashlight") || text.contains("torch on") || text.contains("torch chalu") -> {
                CommandPattern.ToggleFlashlight(true)
            }
            text.contains("flashlight off") || text.contains("turn off flashlight") || text.contains("torch off") || text.contains("torch band") -> {
                CommandPattern.ToggleFlashlight(false)
            }

            // Time & Date
            text.contains("what time") || text.contains("current time") || text.contains("time kya") || text == "time" -> {
                CommandPattern.GetTime
            }
            text.contains("what's today's date") || text.contains("what date") || text.contains("today's date") || text.contains("date kya") -> {
                CommandPattern.GetDate
            }
            text.contains("what day") || text.contains("which day") -> {
                CommandPattern.GetDay
            }

            // Volume controls
            text.contains("volume up") || text.contains("increase volume") || text.contains("awaz badhao") -> {
                CommandPattern.AdjustVolume(up = true)
            }
            text.contains("volume down") || text.contains("decrease volume") || text.contains("awaz kam") -> {
                CommandPattern.AdjustVolume(up = false)
            }

            // Navigation
            text == "go home" || text == "home screen" || text == "open home" -> {
                CommandPattern.GoHome
            }
            text == "go back" || text == "back" -> {
                CommandPattern.GoBack
            }
            text.contains("recent apps") || text.contains("show recents") -> {
                CommandPattern.ShowRecentApps
            }
            text.contains("take screenshot") || text.contains("screenshot lo") -> {
                CommandPattern.TakeScreenshot
            }

            // Settings
            text == "open settings" -> CommandPattern.OpenSettings("general")
            text.contains("wifi settings") || text.contains("open wifi") -> CommandPattern.OpenSettings("wifi")
            text.contains("bluetooth settings") || text.contains("open bluetooth") -> CommandPattern.OpenSettings("bluetooth")
            text.contains("display settings") -> CommandPattern.OpenSettings("display")

            // App launching: "open youtube", "launch spotify", "kholo whatsapp"
            text.startsWith("open ") || text.startsWith("launch ") || text.startsWith("start ") -> {
                val appName = text.replace(Regex("^(open|launch|start)\\s+"), "").trim()
                if (appName.isNotEmpty()) CommandPattern.OpenApp(appName) else null
            }
            text.endsWith(" kholo") || text.endsWith(" open karo") -> {
                val appName = text.replace(Regex("\\s+(kholo|open karo)$"), "").trim()
                if (appName.isNotEmpty()) CommandPattern.OpenApp(appName) else null
            }

            // YouTube specific
            text.startsWith("search youtube for ") -> {
                val query = text.replace("search youtube for ", "").trim()
                CommandPattern.SearchYouTube(query)
            }
            text.startsWith("play ") && text.contains("on youtube") -> {
                val query = text.replace("play ", "").replace("on youtube", "").trim()
                CommandPattern.SearchYouTube(query)
            }

            // Web search
            text.startsWith("search the web for ") || text.startsWith("search web for ") || text.startsWith("google ") -> {
                val query = text.replace(Regex("^(search the web for|search web for|google)\\s+"), "").trim()
                CommandPattern.SearchWeb(query)
            }

            // Reminders
            text.startsWith("show my reminders") || text == "my reminders" -> {
                CommandPattern.ShowReminders
            }
            text.startsWith("remind me to ") || text.startsWith("remind me in ") || text.startsWith("remind me ") -> {
                CommandPattern.CreateReminder(text.replace(Regex("^remind me (to )?"), "").trim())
            }

            // Memory: "remember that my minecraft project is sub-terra", "remember this: ..."
            text.startsWith("remember that ") || text.startsWith("remember this: ") || text.startsWith("remember ") -> {
                val mem = text.replace(Regex("^remember (that |this: )?"), "").trim()
                CommandPattern.StoreMemory(mem)
            }
            text.contains("what do you remember") || text.contains("show my memories") -> {
                CommandPattern.RecallMemory(text)
            }

            // Accessibility automation commands
            text == "scroll down" -> CommandPattern.AccessibilityAction("scroll_down")
            text == "scroll up" -> CommandPattern.AccessibilityAction("scroll_up")
            text.startsWith("tap ") || text.startsWith("click ") -> {
                val target = text.replace(Regex("^(tap|click)( the button that says)?\\s+"), "").trim()
                CommandPattern.AccessibilityAction("tap_text", target)
            }
            text.contains("what does this screen say") || text.contains("read screen") -> {
                CommandPattern.AccessibilityAction("read_screen")
            }

            // Notifications
            text.contains("read my notifications") || text.contains("check notifications") || text.contains("do i have any messages") -> {
                CommandPattern.ReadNotifications
            }

            else -> null
        }
    }
}

sealed class CommandPattern {
    object GetBattery : CommandPattern()
    data class ToggleFlashlight(val enable: Boolean) : CommandPattern()
    object GetTime : CommandPattern()
    object GetDate : CommandPattern()
    object GetDay : CommandPattern()
    data class AdjustVolume(val up: Boolean) : CommandPattern()
    object GoHome : CommandPattern()
    object GoBack : CommandPattern()
    object ShowRecentApps : CommandPattern()
    object TakeScreenshot : CommandPattern()
    data class OpenSettings(val type: String) : CommandPattern()
    data class OpenApp(val appName: String) : CommandPattern()
    data class SearchYouTube(val query: String) : CommandPattern()
    data class SearchWeb(val query: String) : CommandPattern()
    object ShowReminders : CommandPattern()
    data class CreateReminder(val query: String) : CommandPattern()
    data class StoreMemory(val content: String) : CommandPattern()
    data class RecallMemory(val query: String) : CommandPattern()
    data class AccessibilityAction(val action: String, val param: String = "") : CommandPattern()
    object ReadNotifications : CommandPattern()
}

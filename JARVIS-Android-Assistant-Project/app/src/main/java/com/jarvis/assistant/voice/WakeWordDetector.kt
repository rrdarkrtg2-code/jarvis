package com.jarvis.assistant.voice

import android.content.Context

class WakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: () -> Unit
) {
    var isEnabled: Boolean = false

    fun checkSpeechForWakePhrase(text: String): Boolean {
        val cleaned = text.lowercase().trim()
        if (cleaned.startsWith("hey jarvis") || cleaned.startsWith("jarvis") || cleaned.contains("hey jarvis")) {
            onWakeWordDetected()
            return true
        }
        return false
    }
}

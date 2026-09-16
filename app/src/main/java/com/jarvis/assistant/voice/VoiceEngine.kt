package com.jarvis.assistant.voice

import android.content.Context

class VoiceEngine(
    context: Context,
    private val onSpeechRecognized: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onAudioLevel: (Float) -> Unit
) {
    val speechRecognizer = SpeechRecognizerManager(
        context = context,
        onResult = { text ->
            wakeWordDetector.checkSpeechForWakePhrase(text)
            onSpeechRecognized(text)
        },
        onError = onError,
        onRmsChangedCallback = onAudioLevel
    )

    val tts = TtsManager(context)

    val wakeWordDetector = WakeWordDetector(context) {
        // Wake phrase detected
    }

    fun startListening() {
        // Stop speaking immediately for barge-in
        if (tts.isSpeaking) {
            tts.stop()
        }
        speechRecognizer.startListening()
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun speak(text: String, speechRate: Float = 1.0f, pitch: Float = 1.0f) {
        tts.speak(text, speechRate, pitch)
    }

    fun stopSpeaking() {
        tts.stop()
    }

    fun destroy() {
        speechRecognizer.destroy()
        tts.shutdown()
    }
}

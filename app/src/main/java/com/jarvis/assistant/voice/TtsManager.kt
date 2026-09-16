package com.jarvis.assistant.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onInitSuccess: () -> Unit = {},
    private val onSpeechDone: () -> Unit = {}
) {
    private var tts: TextToSpeech? = null
    var isSpeaking: Boolean = false
        private set

    private var lastSpokenText: String = ""

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking = false
                        onSpeechDone()
                    }

                    override fun onError(utteranceId: String?) {
                        isSpeaking = false
                        onSpeechDone()
                    }
                })
                onInitSuccess()
            }
        }
    }

    fun speak(text: String, speechRate: Float = 1.0f, pitch: Float = 1.0f) {
        lastSpokenText = text
        tts?.setSpeechRate(speechRate)
        tts?.setPitch(pitch)
        val utteranceId = System.currentTimeMillis().toString()
        isSpeaking = true
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        isSpeaking = false
    }

    fun replay(speechRate: Float = 1.0f, pitch: Float = 1.0f) {
        if (lastSpokenText.isNotEmpty()) {
            speak(lastSpokenText, speechRate, pitch)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isSpeaking = false
    }
}

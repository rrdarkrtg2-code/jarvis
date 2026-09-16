package com.jarvis.assistant.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.assistant.JarvisApp
import com.jarvis.assistant.data.local.entity.MessageEntity
import com.jarvis.assistant.engine.ConfirmationRequest
import com.jarvis.assistant.ui.components.OrbState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UIState(
    val orbState: OrbState = OrbState.IDLE,
    val audioLevel: Float = 0f,
    val statusMessage: String = "SYSTEM ONLINE",
    val greetingMessage: String = "Good day, Sir. How may I assist you?",
    val lastResponse: String = "",
    val isMicListening: Boolean = false,
    val pendingConfirmation: ConfirmationRequest? = null,
    val activeConversationId: Long = 1L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as JarvisApp
    private val intentRouter = app.intentRouter
    private val voiceEngine = app.voiceEngine
    private val conversationRepo = app.conversationRepository

    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            conversationRepo.createConversation("Default Session")
        }
    }

    fun onMicrophoneClicked() {
        if (_uiState.value.isMicListening) {
            voiceEngine.stopListening()
            _uiState.value = _uiState.value.copy(
                isMicListening = false,
                orbState = OrbState.IDLE,
                statusMessage = "READY"
            )
        } else {
            _uiState.value = _uiState.value.copy(
                isMicListening = true,
                orbState = OrbState.LISTENING,
                statusMessage = "LISTENING..."
            )
            voiceEngine.startListening()
        }
    }

    fun onSpeechRecognized(text: String) {
        _uiState.value = _uiState.value.copy(
            isMicListening = false,
            statusMessage = "THINKING..."
        )
        processQuery(text)
    }

    fun onAudioLevelChanged(level: Float) {
        _uiState.value = _uiState.value.copy(audioLevel = level)
    }

    fun processQuery(queryText: String) {
        val trimmed = queryText.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(
                orbState = OrbState.THINKING,
                statusMessage = "THINKING..."
            )

            // Save user message to DB
            val convId = _uiState.value.activeConversationId
            conversationRepo.addMessage(convId, "user", trimmed)

            // Process through IntentRouter
            val response = intentRouter.processQuery(trimmed, convId)

            if (response.pendingConfirmation != null) {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.IDLE,
                    statusMessage = "CONFIRMATION REQUIRED",
                    pendingConfirmation = response.pendingConfirmation,
                    lastResponse = response.displayText
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    orbState = OrbState.SPEAKING,
                    statusMessage = "RESPONDING",
                    lastResponse = response.displayText
                )

                // Save JARVIS message to DB
                conversationRepo.addMessage(convId, "jarvis", response.displayText)

                // Speak via TTS
                voiceEngine.speak(response.spokenText)

                // After speaking completes or short delay, return to IDLE
                kotlinx.coroutines.delay(2000)
                if (_uiState.value.orbState == OrbState.SPEAKING) {
                    _uiState.value = _uiState.value.copy(
                        orbState = OrbState.IDLE,
                        statusMessage = "SYSTEM ONLINE"
                    )
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        _uiState.value = _uiState.value.copy(pendingConfirmation = null, orbState = OrbState.EXECUTING)
        viewModelScope.launch(Dispatchers.IO) {
            val result = pending.onConfirmed()
            voiceEngine.speak(result)
            _uiState.value = _uiState.value.copy(
                orbState = OrbState.IDLE,
                lastResponse = result,
                statusMessage = "DONE"
            )
        }
    }

    fun cancelPendingAction() {
        val pending = _uiState.value.pendingConfirmation ?: return
        _uiState.value = _uiState.value.copy(pendingConfirmation = null, orbState = OrbState.IDLE)
        viewModelScope.launch(Dispatchers.IO) {
            val result = pending.onRejected()
            voiceEngine.speak(result)
            _uiState.value = _uiState.value.copy(lastResponse = result, statusMessage = "CANCELLED")
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
        _uiState.value = _uiState.value.copy(orbState = OrbState.IDLE)
    }

    fun replayLastResponse() {
        voiceEngine.tts.replay()
    }
}

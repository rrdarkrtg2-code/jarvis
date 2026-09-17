package com.jarvis.assistant.engine

import com.jarvis.assistant.core.Constants
import com.jarvis.assistant.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsageLimitManager(private val settingsRepository: SettingsRepository) {

    private val _remainingSecondsFlow = MutableStateFlow(Constants.DEFAULT_FREE_TALK_TIME_SECONDS)
    val remainingSecondsFlow: StateFlow<Long> = _remainingSecondsFlow.asStateFlow()

    suspend fun initialize() {
        val stored = settingsRepository.getString(Constants.KEY_TALK_TIME_REMAINING, "")
        val seconds = stored.toLongOrNull() ?: Constants.DEFAULT_FREE_TALK_TIME_SECONDS
        _remainingSecondsFlow.value = seconds
    }

    suspend fun isOwnerUnlocked(): Boolean {
        return settingsRepository.getBoolean(Constants.KEY_IS_OWNER_UNLOCKED, false)
    }

    suspend fun verifyAndUnlockOwner(pin: String): Boolean {
        val configuredPin = settingsRepository.getString(Constants.KEY_OWNER_PIN, Constants.DEFAULT_OWNER_PIN)
        if (pin.trim() == configuredPin.trim()) {
            settingsRepository.setBoolean(Constants.KEY_IS_OWNER_UNLOCKED, true)
            return true
        }
        return false
    }

    suspend fun lockOwner() {
        settingsRepository.setBoolean(Constants.KEY_IS_OWNER_UNLOCKED, false)
    }

    suspend fun updateOwnerPin(newPin: String) {
        settingsRepository.setString(Constants.KEY_OWNER_PIN, newPin.trim())
    }

    suspend fun hasAvailableTime(): Boolean {
        if (isOwnerUnlocked()) return true
        return _remainingSecondsFlow.value > 0L
    }

    suspend fun deductTime(seconds: Long = 10L) {
        if (isOwnerUnlocked()) return
        val current = _remainingSecondsFlow.value
        val updated = (current - seconds).coerceAtLeast(0L)
        _remainingSecondsFlow.value = updated
        settingsRepository.setString(Constants.KEY_TALK_TIME_REMAINING, updated.toString())
    }

    suspend fun rewardAddOneHour() {
        val current = _remainingSecondsFlow.value
        val updated = current + Constants.REWARD_ADD_SECONDS
        _remainingSecondsFlow.value = updated
        settingsRepository.setString(Constants.KEY_TALK_TIME_REMAINING, updated.toString())
    }

    fun formatRemainingTime(seconds: Long): String {
        if (seconds <= 0L) return "Expired (Watch Ad to Renew)"
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        return if (hours > 0) "${hours}h ${mins}m left" else "${mins}m left"
    }
}

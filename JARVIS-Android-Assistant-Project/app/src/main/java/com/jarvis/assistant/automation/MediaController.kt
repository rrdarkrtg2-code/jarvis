package com.jarvis.assistant.automation

import android.content.Context
import android.media.AudioManager
import android.view.KeyEvent

class MediaController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun sendMediaKey(keyCode: Int): Boolean {
        return try {
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            audioManager?.dispatchMediaKeyEvent(eventDown)
            audioManager?.dispatchMediaKeyEvent(eventUp)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun togglePlayPause(): Boolean = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    fun nextTrack(): Boolean = sendMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    fun previousTrack(): Boolean = sendMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
}

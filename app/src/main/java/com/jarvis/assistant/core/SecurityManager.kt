package com.jarvis.assistant.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(provider: String, apiKey: String) {
        securePrefs.edit().putString("${Constants.KEY_API_KEY}_$provider", apiKey.trim()).apply()
    }

    fun getApiKey(provider: String): String {
        return securePrefs.getString("${Constants.KEY_API_KEY}_$provider", "") ?: ""
    }

    fun clearApiKey(provider: String) {
        securePrefs.edit().remove("${Constants.KEY_API_KEY}_$provider").apply()
    }

    fun clearAllSecureData() {
        securePrefs.edit().clear().apply()
    }

    fun maskApiKey(key: String): String {
        if (key.length <= 8) return "••••••••"
        return key.take(4) + "••••••••" + key.takeLast(4)
    }
}

package com.jarvis.assistant.data.repository

import com.jarvis.assistant.data.local.dao.PreferenceDao
import com.jarvis.assistant.data.local.entity.PreferenceEntity
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val preferenceDao: PreferenceDao) {
    suspend fun setString(key: String, value: String) {
        preferenceDao.setPreference(PreferenceEntity(key, value))
    }

    suspend fun getString(key: String, defaultValue: String = ""): String {
        return preferenceDao.getPreference(key) ?: defaultValue
    }

    fun observeString(key: String): Flow<String?> {
        return preferenceDao.observePreference(key)
    }

    suspend fun setBoolean(key: String, value: Boolean) {
        setString(key, value.toString())
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key, defaultValue.toString()).toBoolean()
    }

    suspend fun setFloat(key: String, value: Float) {
        setString(key, value.toString())
    }

    suspend fun getFloat(key: String, defaultValue: Float = 1.0f): Float {
        return getString(key, defaultValue.toString()).toFloatOrNull() ?: defaultValue
    }

    suspend fun clearAll() {
        preferenceDao.clearAll()
    }
}

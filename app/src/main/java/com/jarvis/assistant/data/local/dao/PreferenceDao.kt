package com.jarvis.assistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jarvis.assistant.data.local.entity.PreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: PreferenceEntity)

    @Query("SELECT value FROM preferences WHERE `key` = :key")
    suspend fun getPreference(key: String): String?

    @Query("SELECT value FROM preferences WHERE `key` = :key")
    fun observePreference(key: String): Flow<String?>

    @Query("DELETE FROM preferences WHERE `key` = :key")
    suspend fun removePreference(key: String)

    @Query("DELETE FROM preferences")
    suspend fun clearAll()
}

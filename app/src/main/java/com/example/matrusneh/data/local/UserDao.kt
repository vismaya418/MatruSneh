package com.example.matrusneh.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET languagePreference = :lang")
    suspend fun updateLanguage(lang: String)
    
    @Query("UPDATE users SET healthAlertsEnabled = :enabled")
    suspend fun updateHealthAlertsPreference(enabled: Boolean)

    @Query("UPDATE users SET remindersEnabled = :enabled")
    suspend fun updateReminderPreference(enabled: Boolean)

    @Query("UPDATE users SET isDarkMode = :enabled")
    suspend fun updateDarkMode(enabled: Boolean)

    @Query("DELETE FROM users")
    suspend fun clearAll()
}

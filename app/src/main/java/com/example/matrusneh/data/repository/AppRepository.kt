package com.example.matrusneh.data.repository

import com.example.matrusneh.data.local.*
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val userDao: UserDao,
    private val kickDao: KickDao,
    private val waterDao: WaterDao
) {
    val user: Flow<UserEntity?> = userDao.getUser()
    suspend fun getUserSync(): UserEntity? = userDao.getUserSync()
    val totalKicks: Flow<Int> = kickDao.getTotalKicks()

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }

    suspend fun updateLanguage(lang: String) {
        userDao.updateLanguage(lang)
    }

    suspend fun updateHealthAlertsPreference(enabled: Boolean) {
        userDao.updateHealthAlertsPreference(enabled)
    }

    suspend fun updateReminderPreference(enabled: Boolean) {
        userDao.updateReminderPreference(enabled)
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        userDao.updateDarkMode(enabled)
    }

    suspend fun insertKick(timestamp: Long) {
        kickDao.insertKick(KickEntity(timestamp = timestamp))
    }

    fun getKicksForToday(startOfDay: Long, endOfDay: Long): Flow<Int> {
        return kickDao.getKicksForToday(startOfDay, endOfDay)
    }

    // Water tracking
    fun getWaterForDate(date: String): Flow<WaterEntity?> {
        return waterDao.getWaterForDate(date)
    }

    suspend fun incrementWater(date: String) {
        val existing = waterDao.getWaterForDate(date)
        // Note: Flow check is complex in suspend, but we can use Dao increment query
        waterDao.incrementWater(date)
    }

    suspend fun ensureWaterEntry(date: String) {
        // We'll call this to make sure an entry exists for the date if we want to use increment
        // Or just use a replace insert with count 1 if not exists
    }

    suspend fun insertWater(water: WaterEntity) {
        waterDao.insertWater(water)
    }

    suspend fun clearUserData() {
        userDao.clearAll()
        kickDao.clearAll()
        waterDao.clearAll()
    }
}

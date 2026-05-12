package com.example.matrusneh.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KickDao {
    @Insert
    suspend fun insertKick(kick: KickEntity)

    @Query("SELECT COUNT(*) FROM kicks")
    fun getTotalKicks(): Flow<Int>

    // Optional: Get kicks for today (using simple timestamp range if needed)
    @Query("SELECT COUNT(*) FROM kicks WHERE timestamp >= :startOfDay AND timestamp <= :endOfDay")
    fun getKicksForToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("DELETE FROM kicks")
    suspend fun clearAll()
}

package com.example.matrusneh.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_tracking WHERE date = :date")
    fun getWaterForDate(date: String): Flow<WaterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWater(water: WaterEntity)

    @Query("UPDATE water_tracking SET count = count + 1 WHERE date = :date")
    suspend fun incrementWater(date: String)

    @Query("DELETE FROM water_tracking")
    suspend fun clearAll()
}

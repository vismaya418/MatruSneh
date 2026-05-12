package com.example.matrusneh.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_tracking")
data class WaterEntity(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val count: Int,
    val goal: Int = 8
)

package com.example.matrusneh.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kicks")
data class KickEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long
)

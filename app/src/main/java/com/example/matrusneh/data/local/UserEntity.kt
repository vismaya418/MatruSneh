package com.example.matrusneh.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val languagePreference: String = "en",
    val healthAlertsEnabled: Boolean = true,
    val remindersEnabled: Boolean = false,
    val isDarkMode: Boolean = false
)

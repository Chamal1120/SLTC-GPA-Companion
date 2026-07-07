package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val degreeName: String,
    val degreeType: String, // "Honours (Engineering/Tech/IT/Music)", "Honours (Management/Tourism/Hospitality)", "General (Except Tourism/Hospitality)", "General (Tourism/Hospitality)"
    val indexNumber: String = "",
    val faculty: String = "",
    val fullName: String = "",
    val themeMode: String = "auto", // "light", "dark", "auto"
    val isOnboarded: Boolean = false
)

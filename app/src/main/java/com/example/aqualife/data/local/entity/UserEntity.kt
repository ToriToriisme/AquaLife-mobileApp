package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val displayName: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val isEmailVerified: Boolean = false,
    val lastLogin: Long = System.currentTimeMillis()
)


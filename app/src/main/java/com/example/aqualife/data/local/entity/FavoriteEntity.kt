package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_table",
    primaryKeys = ["userId", "fishId"]
)
data class FavoriteEntity(
    val userId: String,
    val fishId: String,
    val addedAt: Long = System.currentTimeMillis()
)


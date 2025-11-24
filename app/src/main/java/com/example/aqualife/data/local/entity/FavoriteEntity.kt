package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_table")
data class FavoriteEntity(
    @PrimaryKey val fishId: String,
    val addedAt: Long = System.currentTimeMillis()
)


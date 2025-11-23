package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish_table")
data class FishEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val priceInt: Int,
    val category: String,
    val habitat: String,
    val maxWeight: String,
    val diet: String,
    val imageUrl: String,
    val description: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)


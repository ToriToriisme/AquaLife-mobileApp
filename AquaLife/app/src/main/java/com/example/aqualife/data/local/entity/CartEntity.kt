package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_table")
data class CartEntity(
    @PrimaryKey val fishId: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)


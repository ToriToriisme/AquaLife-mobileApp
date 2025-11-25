package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_table",
    primaryKeys = ["userId", "fishId"]
)
data class CartEntity(
    val userId: String,
    val fishId: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)


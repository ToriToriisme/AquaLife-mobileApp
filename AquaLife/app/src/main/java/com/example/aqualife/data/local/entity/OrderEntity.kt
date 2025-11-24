package com.example.aqualife.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_table")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userId: String,
    val totalAmount: Double,
    val status: String, // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    val paymentMethod: String, // MOMO, BANK, CASH
    val paymentStatus: String, // PENDING, PAID, FAILED
    val transactionCode: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


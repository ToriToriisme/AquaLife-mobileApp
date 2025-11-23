package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM order_table WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersByUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_table WHERE orderId = :orderId")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE order_table SET status = :status, updatedAt = :timestamp WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, timestamp: Long = System.currentTimeMillis())
}


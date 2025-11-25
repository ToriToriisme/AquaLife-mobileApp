package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_table WHERE userId = :userId")
    fun getAllCartItems(userId: String): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_table WHERE userId = :userId AND fishId = :fishId")
    fun getCartItem(userId: String, fishId: String): Flow<CartEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

    @Update
    suspend fun updateCartItem(item: CartEntity)

    @Query("UPDATE cart_table SET quantity = :quantity WHERE userId = :userId AND fishId = :fishId")
    suspend fun updateQuantity(userId: String, fishId: String, quantity: Int)

    @Query("DELETE FROM cart_table WHERE userId = :userId AND fishId = :fishId")
    suspend fun deleteCartItem(userId: String, fishId: String)

    @Query("DELETE FROM cart_table WHERE userId = :userId")
    suspend fun clearCart(userId: String)
    
    @Query("DELETE FROM cart_table")
    suspend fun clearAllCart()
}


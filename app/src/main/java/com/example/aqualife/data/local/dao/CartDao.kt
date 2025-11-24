package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.CartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_table")
    fun getAllCartItems(): Flow<List<CartEntity>>

    @Query("SELECT * FROM cart_table WHERE fishId = :fishId")
    fun getCartItem(fishId: String): Flow<CartEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartEntity)

    @Update
    suspend fun updateCartItem(item: CartEntity)

    @Query("UPDATE cart_table SET quantity = :quantity WHERE fishId = :fishId")
    suspend fun updateQuantity(fishId: String, quantity: Int)

    @Query("DELETE FROM cart_table WHERE fishId = :fishId")
    suspend fun deleteCartItem(fishId: String)

    @Query("DELETE FROM cart_table")
    suspend fun clearCart()
}


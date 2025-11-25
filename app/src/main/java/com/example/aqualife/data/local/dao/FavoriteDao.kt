package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_table WHERE userId = :userId ORDER BY addedAt DESC")
    fun getAllFavorites(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite_table WHERE userId = :userId AND fishId = :fishId")
    fun isFavorite(userId: String, fishId: String): Flow<FavoriteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_table WHERE userId = :userId AND fishId = :fishId")
    suspend fun removeFavorite(userId: String, fishId: String)

    @Query("DELETE FROM favorite_table WHERE userId = :userId")
    suspend fun clearFavorites(userId: String)
    
    @Query("DELETE FROM favorite_table")
    suspend fun clearAllFavorites()
}


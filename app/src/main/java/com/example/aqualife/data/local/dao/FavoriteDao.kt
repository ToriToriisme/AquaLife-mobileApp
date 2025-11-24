package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_table ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite_table WHERE fishId = :fishId")
    fun isFavorite(fishId: String): Flow<FavoriteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_table WHERE fishId = :fishId")
    suspend fun removeFavorite(fishId: String)

    @Query("DELETE FROM favorite_table")
    suspend fun clearFavorites()
}


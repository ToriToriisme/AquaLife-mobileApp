package com.example.aqualife.data.local.dao

import androidx.room.*
import com.example.aqualife.data.local.entity.FishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {
    @Query("SELECT * FROM fish_table ORDER BY name ASC")
    fun getAllFish(): Flow<List<FishEntity>>

    @Query("SELECT * FROM fish_table WHERE category = :category ORDER BY name ASC")
    fun getFishByCategory(category: String): Flow<List<FishEntity>>

    @Query("SELECT * FROM fish_table WHERE id = :fishId")
    fun getFishById(fishId: String): Flow<FishEntity?>

    @Query("SELECT * FROM fish_table WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchFish(query: String): Flow<List<FishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: FishEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFish(fishList: List<FishEntity>)

    @Update
    suspend fun updateFish(fish: FishEntity)

    @Query("UPDATE fish_table SET price = :newPrice, priceInt = :newPriceInt, lastUpdated = :timestamp WHERE id = :fishId")
    suspend fun updatePrice(fishId: String, newPrice: Double, newPriceInt: Int, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM fish_table WHERE id = :fishId")
    suspend fun deleteFish(fishId: String)

    @Query("DELETE FROM fish_table")
    suspend fun deleteAllFish()
}


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
    
    @Query("SELECT COUNT(*) FROM fish_table")
    suspend fun getCount(): Int

    /**
     * Advanced filter query for Search/Filter UI
     * Supports: category, price range, rating, discount filter, sorting
     */
    @Query("""
        SELECT * FROM fish_table
        WHERE (:category IS NULL OR category = :category)
        AND (price BETWEEN :minPrice AND :maxPrice)
        AND (:minRating IS NULL OR rating >= :minRating)
        AND (:onlyDiscount = 0 OR hasDiscount = 1)
        ORDER BY
            CASE WHEN :sortBy = 'price_asc' THEN price END ASC,
            CASE WHEN :sortBy = 'price_desc' THEN price END DESC,
            CASE WHEN :sortBy = 'best_seller' THEN soldCount END DESC,
            CASE WHEN :sortBy = 'rating' THEN rating END DESC,
            name ASC
    """)
    fun getFilteredFish(
        category: String? = null,
        minPrice: Double = 0.0,
        maxPrice: Double = 100000000.0,
        minRating: Float? = null,
        onlyDiscount: Int = 0, // 0 = all, 1 = only discount
        sortBy: String = "name" // price_asc, price_desc, best_seller, rating
    ): Flow<List<FishEntity>>

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


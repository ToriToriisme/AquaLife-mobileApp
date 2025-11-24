package com.example.aqualife.data.repository

import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.data.remote.AquaLifeApiService
import com.example.aqualife.data.remote.FirebaseSyncService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FishRepository @Inject constructor(
    private val fishDao: FishDao,
    private val apiService: AquaLifeApiService,
    private val firebaseSyncService: FirebaseSyncService
) {
    /**
     * Offline-First: Always return data from local database
     * UI will automatically update when database changes
     */
    fun getAllFish(): Flow<List<FishEntity>> = fishDao.getAllFish()

    fun getFishByCategory(category: String): Flow<List<FishEntity>> = 
        fishDao.getFishByCategory(category)

    fun getFishById(fishId: String): Flow<FishEntity?> = fishDao.getFishById(fishId)

    fun searchFish(query: String): Flow<List<FishEntity>> = fishDao.searchFish(query)

    /**
     * Start real-time synchronization from Firebase
     * Call this once when app starts (in Application or MainViewModel)
     */
    fun startRealtimeSync(): Flow<List<FishEntity>> {
        return firebaseSyncService.observeFishChanges()
            .onEach { fishList ->
                // When Firebase data changes, save to local database
                fishDao.insertAllFish(fishList)
            }
            .catch { e ->
                // Handle error - continue showing cached data
            }
    }

    /**
     * Initial data load from API (fallback if Firebase not available)
     */
    suspend fun loadFishFromApi() {
        try {
            val response = apiService.getAllFish()
            if (response.isSuccessful) {
                val fishList = response.body()?.map { dto ->
                    FishEntity(
                        id = dto.id,
                        name = dto.name,
                        price = dto.price,
                        priceInt = dto.priceInt,
                        category = dto.category,
                        habitat = dto.habitat,
                        maxWeight = dto.maxWeight,
                        diet = dto.diet,
                        imageUrl = dto.imageUrl,
                        description = dto.description,
                        lastUpdated = dto.lastUpdated
                    )
                } ?: emptyList()
                fishDao.insertAllFish(fishList)
            }
        } catch (e: Exception) {
            // Handle error - app will show cached data
        }
    }

    /**
     * Initialize database with default fish data (40-50 per category)
     * Called on first app install
     */
    suspend fun initializeDefaultFish() {
        val defaultFish = generateDefaultFishData()
        fishDao.insertAllFish(defaultFish)
    }

    private fun generateDefaultFishData(): List<FishEntity> {
        // Generate 40-50 fish per category
        val categories = listOf("Cá biển", "Cá sông", "Cá nước lợ", "Cá cảnh")
        val fishList = mutableListOf<FishEntity>()
        var idCounter = 1

        categories.forEach { category ->
            val count = when (category) {
                "Cá biển" -> 45
                "Cá sông" -> 42
                "Cá nước lợ" -> 40
                "Cá cảnh" -> 50
                else -> 40
            }

            repeat(count) {
                fishList.add(
                    FishEntity(
                        id = "${category}_$idCounter",
                        name = "$category Fish $idCounter",
                        price = (10000..5000000).random().toDouble(),
                        priceInt = (10000..5000000).random(),
                        category = category,
                        habitat = "Habitat for $category",
                        maxWeight = "${(100..5000).random()}g",
                        diet = "Diet for $category",
                        imageUrl = "https://images.unsplash.com/photo-${(1500000000000..1600000000000).random()}?w=500",
                        description = "Description for $category fish number $idCounter",
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                idCounter++
            }
        }

        return fishList
    }
}


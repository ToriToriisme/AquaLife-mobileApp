package com.example.aqualife.data.repository

// ============================================================================
// ANDROID IMPORTS
// ============================================================================
import android.util.Log

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
// (None - using Kotlin coroutines and Flow)

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.data.local.FishSeedData
import com.example.aqualife.data.remote.AquaLifeApiService
import com.example.aqualife.data.remote.FirebaseSyncService

// ============================================================================
// FISH REPOSITORY
// ============================================================================

@Singleton
class FishRepository @Inject constructor(
    private val fishDao: FishDao,
    private val apiService: AquaLifeApiService,
    private val firebaseSyncService: FirebaseSyncService,
    private val firestore: FirebaseFirestore
) {
    /**
     * Offline-First: Always return data from local database
     */
    fun getAllFish(): Flow<List<FishEntity>> {
        return fishDao.getAllFish()
    }

    /**
     * Get filtered fish by category
     */
    fun getFilteredFishStream(category: String): Flow<List<FishEntity>> {
        return fishDao.getFishByCategory(category)
    }

    /**
     * Get fish by category
     */
    fun getFishByCategory(category: String): Flow<List<FishEntity>> {
        return fishDao.getFishByCategory(category)
    }

    /**
     * Get filtered fish with advanced filters
     */
    fun getFilteredFish(
        category: String? = null,
        minPrice: Double = 0.0,
        maxPrice: Double = 100000000.0,
        minRating: Float? = null,
        onlyDiscount: Boolean = false,
        sortBy: String = "name"
    ): Flow<List<FishEntity>> {
        return fishDao.getFilteredFish(
            category = category,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minRating = minRating,
            onlyDiscount = if (onlyDiscount) 1 else 0,
            sortBy = sortBy
        )
    }

    /**
     * Search fish by query (with Vietnamese normalization)
     */
    fun searchFish(query: String): Flow<List<FishEntity>> {
        return fishDao.searchFish("%$query%")
    }

    /**
     * Initialize data: Load from Firebase or seed from local data
     */
    fun initializeData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if database is empty
                val existingFish = fishDao.getCount()
                
                if (existingFish == 0) {
                    // Database is empty - try to fetch from Firebase first
                    Log.d("FishRepository", "Database is empty, fetching from Firebase...")
                    fetchFromFirebase()
                    
                    // Check again after Firebase fetch
                    val afterFirebaseCount = fishDao.getCount()
                    if (afterFirebaseCount == 0) {
                        // Firebase is also empty - seed from local data
                        Log.d("FishRepository", "Firebase is empty, seeding from local data...")
                        seedFromLocalData()
                    } else {
                        Log.d("FishRepository", "Loaded $afterFirebaseCount fish from Firebase")
                    }
                } else {
                    // Database has data - still try to sync from Firebase in background
                    Log.d("FishRepository", "Database has $existingFish fish, syncing from Firebase in background...")
                    syncFromFirebase()
                }
            } catch (e: Exception) {
                Log.e("FishRepository", "Error initializing data", e)
                // Fallback: seed from local data if everything fails
                try {
                    seedFromLocalData()
                } catch (seedError: Exception) {
                    Log.e("FishRepository", "Error seeding from local data", seedError)
                }
            }
        }
    }

    /**
     * Fetch fish data from Firebase Firestore
     */
    private suspend fun fetchFromFirebase() {
        try {
            val snapshot = firestore.collection("products").get().await()
            val fishList = mutableListOf<FishEntity>()
            
            for (document in snapshot.documents) {
                try {
                    val fish = document.toObject(FishEntity::class.java)
                    if (fish != null) {
                        fishList.add(fish)
                    }
                } catch (e: Exception) {
                    Log.e("FishRepository", "Error parsing document ${document.id}", e)
                }
            }
            
            if (fishList.isNotEmpty()) {
                fishDao.insertAllFish(fishList)
                Log.d("FishRepository", "Saved ${fishList.size} fish from Firebase to Database")
            } else {
                Log.d("FishRepository", "Downloaded 0 fish from Firebase")
            }
        } catch (e: Exception) {
            Log.e("FishRepository", "Error fetching from Firebase", e)
            throw e
        }
    }

    /**
     * Sync fish data from Firebase (update existing records)
     */
    private suspend fun syncFromFirebase() {
        try {
            val snapshot = firestore.collection("products").get().await()
            var updatedCount = 0
            
            for (document in snapshot.documents) {
                try {
                    val fish = document.toObject(FishEntity::class.java)
                    if (fish != null) {
                        // Check if fish exists
                        val existing: FishEntity? = try {
                            fishDao.getFishById(fish.id).first()
                        } catch (e: Exception) {
                            null
                        }
                        if (existing != null) {
                            // Update if image URL changed
                            if (existing.imageUrl != fish.imageUrl) {
                                fishDao.updateFish(fish)
                                updatedCount++
                            }
                        } else {
                            // Insert new fish
                            fishDao.insertFish(fish)
                            updatedCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FishRepository", "Error syncing document ${document.id}", e)
                }
            }
            
            if (updatedCount > 0) {
                Log.d("FishRepository", "Updated $updatedCount fish with latest image URLs from Firebase")
            }
        } catch (e: Exception) {
            Log.e("FishRepository", "Error syncing from Firebase", e)
        }
    }

    /**
     * Seed fish data from local FishSeedData
     */
    private suspend fun seedFromLocalData() {
        try {
            val seedData = FishSeedData.generateRealFishData()
            if (seedData.isNotEmpty()) {
                fishDao.insertAllFish(seedData)
                Log.d("FishRepository", "Saved ${seedData.size} fish to the Database")
                
                // Push to Firebase for future syncs
                pushToFirebase(seedData)
            }
        } catch (e: Exception) {
            Log.e("FishRepository", "Error seeding from local data", e)
            throw e
        }
    }

    /**
     * Push seed data to Firebase
     */
    private suspend fun pushToFirebase(fishList: List<FishEntity>) {
        try {
            val batch = firestore.batch()
            fishList.forEach { fish ->
                val docRef = firestore.collection("products").document(fish.id)
                batch.set(docRef, fish, SetOptions.merge())
            }
            batch.commit().await()
            Log.d("FishRepository", "Pushed ${fishList.size} fish to Firebase")
        } catch (e: Exception) {
            Log.e("FishRepository", "Error pushing to Firebase", e)
            // Don't throw - this is not critical
        }
    }
}

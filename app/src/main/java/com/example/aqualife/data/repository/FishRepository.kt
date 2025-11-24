package com.example.aqualife.data.repository

import android.util.Log
import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.data.local.FishSeedData
import com.example.aqualife.data.remote.AquaLifeApiService
import com.example.aqualife.data.remote.FirebaseSyncService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FishRepository @Inject constructor(
    private val fishDao: FishDao,
    private val apiService: AquaLifeApiService,
    private val firebaseSyncService: FirebaseSyncService,
    private val firestore: FirebaseFirestore
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

    fun getFilteredFish(
        category: String? = null,
        minPrice: Double = 0.0,
        maxPrice: Double = 100_000_000.0,
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
     * Initialize database with real fish data
     * Auto-loads on first app launch - checks Local DB -> Firebase -> Seed Data
     */
    fun initializeData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step A: Check if local database has data
                val localCount = fishDao.getCount()
                
                if (localCount == 0) {
                    Log.d("AquaLife", "Database empty. Checking Firebase...")
                    
                    // Step B: Check if Firebase has data
                    try {
                        val snapshot = firestore.collection("products").get().await()
                        
                        if (snapshot.isEmpty) {
                            Log.d("AquaLife", "Firebase empty. Loading seed data (80 real fish)...")
                            seedLocalDatabaseWithDefaults()
                        } else {
                            Log.d("AquaLife", "Firebase has data. Downloading to local...")
                            val remoteList = snapshot.documents.mapNotNull { doc ->
                                try {
                                    FishEntity(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        price = doc.getDouble("price") ?: 0.0,
                                        priceInt = doc.getLong("priceInt")?.toInt() ?: 0,
                                        category = doc.getString("category") ?: "",
                                        habitat = doc.getString("habitat") ?: "",
                                        maxWeight = doc.getString("maxWeight") ?: "",
                                        diet = doc.getString("diet") ?: "",
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        description = doc.getString("description") ?: "",
                                        lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis(),
                                        rating = doc.getDouble("rating")?.toFloat() ?: 4.5f,
                                        soldCount = doc.getLong("soldCount")?.toInt() ?: 0,
                                        hasDiscount = doc.getBoolean("hasDiscount") ?: false,
                                        discountPrice = doc.getDouble("discountPrice")
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (remoteList.isEmpty()) {
                                Log.w("AquaLife", "Firebase returned 0 fish. Seeding defaults locally.")
                                seedLocalDatabaseWithDefaults()
                            } else {
                                fishDao.insertAllFish(remoteList)
                                Log.d("AquaLife", "Downloaded ${remoteList.size} fish from Firebase")
                            }
                        }
                    } catch (e: Exception) {
                        // Firebase connection failed -> Use seed data
                        Log.w("AquaLife", "Firebase unavailable, using seed data: ${e.message}")
                        seedLocalDatabaseWithDefaults()
                    }
                    
                } else {
                    Log.d("AquaLife", "Database has $localCount fish. Ready to use.")
                    // Database already populated, start real-time sync
                    startRealtimeSyncListener()
                }
            } catch (e: Exception) {
                Log.e("AquaLife", "Error initializing data: ${e.message}")
            }
        }
    }

    private suspend fun seedLocalDatabaseWithDefaults() {
        val seedData = FishSeedData.generateRealFishData()
        fishDao.insertAllFish(seedData)
        Log.d("AquaLife", "Saved ${seedData.size} fish to the database")
        startRealtimeSyncListener()
        pushToFirebase(seedData)
    }
    
    /**
     * Push fish data to Firebase (batch write)
     */
    private fun pushToFirebase(fishList: List<FishEntity>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val batch = firestore.batch()
                
                fishList.forEach { fish ->
                    val docRef = firestore.collection("products").document(fish.id)
                    val fishMap = hashMapOf(
                        "name" to fish.name,
                        "price" to fish.price,
                        "priceInt" to fish.priceInt,
                        "category" to fish.category,
                        "imageUrl" to fish.imageUrl,
                        "description" to fish.description,
                        "habitat" to fish.habitat,
                        "diet" to fish.diet,
                        "maxWeight" to fish.maxWeight,
                        "rating" to fish.rating,
                        "soldCount" to fish.soldCount,
                        "hasDiscount" to fish.hasDiscount,
                        "discountPrice" to fish.discountPrice,
                        "lastUpdated" to fish.lastUpdated
                    )
                    batch.set(docRef, fishMap, SetOptions.merge())
                }
                
                batch.commit().await()
                Log.d("AquaLife", "Pushed ${fishList.size} fish to Firebase successfully!")
            } catch (e: Exception) {
                Log.e("AquaLife", "Error pushing to Firebase: ${e.message}")
            }
        }
    }
    
    /**
     * Listen to Firebase real-time updates (price changes from admin dashboard)
     */
    private fun startRealtimeSyncListener() {
        firestore.collection("products").addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.w("AquaLife", "Realtime sync error: ${e.message}")
                return@addSnapshotListener
            }
            
            val updatedList = snapshots?.documents?.mapNotNull { doc ->
                try {
                    FishEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        priceInt = doc.getLong("priceInt")?.toInt() ?: 0,
                        category = doc.getString("category") ?: "",
                        habitat = doc.getString("habitat") ?: "",
                        maxWeight = doc.getString("maxWeight") ?: "",
                        diet = doc.getString("diet") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        description = doc.getString("description") ?: "",
                        lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis(),
                        rating = doc.getDouble("rating")?.toFloat() ?: 4.5f,
                        soldCount = doc.getLong("soldCount")?.toInt() ?: 0,
                        hasDiscount = doc.getBoolean("hasDiscount") ?: false,
                        discountPrice = doc.getDouble("discountPrice")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            if (updatedList != null && updatedList.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    fishDao.insertAllFish(updatedList)
                    Log.d("AquaLife", "Synced ${updatedList.size} fish from Firebase")
                }
            }
        }
    }
}


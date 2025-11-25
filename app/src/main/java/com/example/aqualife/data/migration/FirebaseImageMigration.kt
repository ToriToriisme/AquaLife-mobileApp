package com.example.aqualife.data.migration

import android.util.Log
import com.example.aqualife.data.local.FishSeedData
import com.example.aqualife.data.local.entity.FishEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Migration tool to update Firebase image URLs from Unsplash to Picsum
 * 
 * Usage:
 * 1. Call migrateFirebaseImages() once when app starts (in Application or MainActivity)
 * 2. Or call it manually from a debug menu
 * 
 * This will:
 * - Fetch all fish from Firebase
 * - Check if they have old Unsplash URLs
 * - Update to new Picsum URLs based on FishSeedData mapping
 * - Save back to Firebase
 */
@Singleton
class FirebaseImageMigration @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val TAG = "FirebaseMigration"
    
    /**
     * Main migration function
     * Call this once to migrate all Firebase documents
     */
    fun migrateFirebaseImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting Firebase image migration...")
                
                // Get all fish from seed data to create mapping
                val seedData = FishSeedData.generateRealFishData()
                val nameToImageMap = seedData.associate { it.name to it.imageUrl }
                
                // Fetch all documents from Firebase
                val snapshot = firestore.collection("products").get().await()
                
                if (snapshot.isEmpty) {
                    Log.d(TAG, "Firebase collection is empty. No migration needed.")
                    return@launch
                }
                
                Log.d(TAG, "Found ${snapshot.documents.size} documents in Firebase")
                
                val batch = firestore.batch()
                var updateCount = 0
                
                snapshot.documents.forEach { doc ->
                    val name = doc.getString("name") ?: ""
                    val currentImageUrl = doc.getString("imageUrl") ?: ""
                    
                    // Check if URL needs migration (contains unsplash)
                    if (currentImageUrl.contains("unsplash.com", ignoreCase = true)) {
                        // Find matching seed data by name
                        val newImageUrl = nameToImageMap[name]
                        
                        if (newImageUrl != null && newImageUrl != currentImageUrl) {
                            Log.d(TAG, "Updating: $name")
                            Log.d(TAG, "  Old: $currentImageUrl")
                            Log.d(TAG, "  New: $newImageUrl")
                            
                            val docRef = firestore.collection("products").document(doc.id)
                            batch.update(docRef, "imageUrl", newImageUrl)
                            updateCount++
                        } else {
                            Log.w(TAG, "No matching seed data found for: $name")
                        }
                    } else {
                        Log.d(TAG, "Skipping $name (already using Picsum or other URL)")
                    }
                }
                
                if (updateCount > 0) {
                    batch.commit().await()
                    Log.d(TAG, "✅ Migration completed! Updated $updateCount documents")
                } else {
                    Log.d(TAG, "✅ No documents needed migration")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Migration failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Check migration status
     * Returns count of documents that still use Unsplash URLs
     */
    suspend fun checkMigrationStatus(): Int {
        return try {
            val snapshot = firestore.collection("products").get().await()
            snapshot.documents.count { doc ->
                val imageUrl = doc.getString("imageUrl") ?: ""
                imageUrl.contains("unsplash.com", ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking migration status: ${e.message}", e)
            0
        }
    }
    
    /**
     * Force re-seed: Delete all Firebase data and push new seed data
     * WARNING: This will delete all existing data in Firebase!
     */
    fun forceReseed() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "⚠️ Starting force re-seed (will delete all Firebase data)...")
                
                // Delete all documents
                val snapshot = firestore.collection("products").get().await()
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                
                Log.d(TAG, "Deleted ${snapshot.documents.size} old documents")
                
                // Push new seed data
                val seedData = FishSeedData.generateRealFishData()
                val newBatch = firestore.batch()
                
                seedData.forEach { fish ->
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
                    newBatch.set(docRef, fishMap)
                }
                
                newBatch.commit().await()
                Log.d(TAG, "✅ Force re-seed completed! Pushed ${seedData.size} new documents")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Force re-seed failed: ${e.message}", e)
            }
        }
    }
}


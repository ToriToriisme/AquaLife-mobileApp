package com.example.aqualife.data.remote

import com.example.aqualife.data.local.entity.FishEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    /**
     * Listen to real-time changes from Firestore
     * When admin updates price on web dashboard, this will emit new data
     */
    fun observeFishChanges(): Flow<List<FishEntity>> = callbackFlow {
        val listener = firestore.collection("products")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val fishList = snapshots?.documents?.mapNotNull { doc ->
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
                            lastUpdated = doc.getLong("lastUpdated") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(fishList)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Send order data to Firestore for admin dashboard
     */
    suspend fun sendOrder(orderData: Map<String, Any>) {
        try {
            firestore.collection("orders")
                .add(orderData)
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }
}


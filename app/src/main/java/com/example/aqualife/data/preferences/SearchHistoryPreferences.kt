package com.example.aqualife.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history_prefs")

@Singleton
class SearchHistoryPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) {

    private fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "guest"
    }

    private fun getHistoryKey(userId: String): Preferences.Key<String> {
        return stringPreferencesKey("history_$userId")
    }

    val historyFlow: Flow<List<String>> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val userId = auth.currentUser?.uid ?: "guest"
            trySend(userId)
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(getCurrentUserId())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
        .flatMapLatest { userId ->
            context.searchHistoryDataStore.data
                .map { prefs ->
                    val key = getHistoryKey(userId)
                    val raw = prefs[key].orEmpty()
                    if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
                }
        }

    suspend fun saveHistory(history: List<String>) {
        val userId = getCurrentUserId()
        val key = getHistoryKey(userId)
        context.searchHistoryDataStore.edit { prefs ->
            prefs[key] = history.joinToString("|")
        }
    }

    suspend fun clear() {
        val userId = getCurrentUserId()
        val key = getHistoryKey(userId)
        context.searchHistoryDataStore.edit { it.remove(key) }
    }
}


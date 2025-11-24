package com.example.aqualife.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history_prefs")

@Singleton
class SearchHistoryPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val HISTORY_KEY = stringPreferencesKey("history")
    }

    val historyFlow: Flow<List<String>> = context.searchHistoryDataStore.data
        .map { prefs ->
            val raw = prefs[HISTORY_KEY].orEmpty()
            if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
        }

    suspend fun saveHistory(history: List<String>) {
        context.searchHistoryDataStore.edit { prefs ->
            prefs[HISTORY_KEY] = history.joinToString("|")
        }
    }

    suspend fun clear() {
        context.searchHistoryDataStore.edit { it.remove(HISTORY_KEY) }
    }
}


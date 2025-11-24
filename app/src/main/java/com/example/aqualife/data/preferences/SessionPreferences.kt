package com.example.aqualife.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

enum class SessionProvider {
    Firebase, Google, Facebook, Guest, None
}

data class SessionState(
    val isLoggedIn: Boolean = false,
    val provider: SessionProvider = SessionProvider.None,
    val displayName: String? = null,
    val email: String? = null
)

@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("session_logged_in")
        val PROVIDER = stringPreferencesKey("session_provider")
        val DISPLAY_NAME = stringPreferencesKey("session_display_name")
        val EMAIL = stringPreferencesKey("session_email")
    }

    val sessionFlow: Flow<SessionState> = context.sessionDataStore.data.map { prefs ->
        val providerValue = prefs[Keys.PROVIDER]
        SessionState(
            isLoggedIn = prefs[Keys.IS_LOGGED_IN] ?: false,
            provider = providerValue?.let { runCatching { SessionProvider.valueOf(it) }.getOrDefault(SessionProvider.None) }
                ?: SessionProvider.None,
            displayName = prefs[Keys.DISPLAY_NAME],
            email = prefs[Keys.EMAIL]
        )
    }

    suspend fun setSession(provider: SessionProvider, displayName: String?, email: String?) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.PROVIDER] = provider.name
            prefs[Keys.DISPLAY_NAME] = displayName ?: ""
            prefs[Keys.EMAIL] = email ?: ""
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}


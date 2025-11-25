package com.example.aqualife.ui.viewmodel

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.data.local.dao.FavoriteDao
import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.FishEntity

// ============================================================================
// FAVORITE VIEWMODEL
// ============================================================================

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val fishDao: FishDao,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "guest"
    }

    val favoriteFish: StateFlow<List<FishEntity>> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val userId = auth.currentUser?.uid ?: "guest"
            trySend(userId)
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(getCurrentUserId())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
        .flatMapLatest { userId ->
            favoriteDao.getAllFavorites(userId)
                .flatMapLatest { favorites ->
                    if (favorites.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            favorites.map { favorite ->
                                fishDao.getFishById(favorite.fishId)
                            }
                        ) { fishList ->
                            fishList.filterNotNull()
                        }
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun isFavorite(fishId: String): Flow<Boolean> {
        val userId = getCurrentUserId()
        return favoriteDao.isFavorite(userId, fishId)
            .map { it != null }
    }

    fun toggleFavorite(fishId: String) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val existing = favoriteDao.isFavorite(userId, fishId).first()
            if (existing != null) {
                favoriteDao.removeFavorite(userId, fishId)
            } else {
                favoriteDao.addFavorite(
                    com.example.aqualife.data.local.entity.FavoriteEntity(
                        userId = userId,
                        fishId = fishId
                    )
                )
            }
        }
    }
}

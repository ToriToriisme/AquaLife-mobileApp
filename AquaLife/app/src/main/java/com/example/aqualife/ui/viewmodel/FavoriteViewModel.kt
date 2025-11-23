package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.local.dao.FavoriteDao
import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.FishEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val fishDao: FishDao
) : ViewModel() {

    val favoriteFish: StateFlow<List<FishEntity>> = favoriteDao.getAllFavorites()
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
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun isFavorite(fishId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(fishId)
            .map { it != null }
    }

    fun toggleFavorite(fishId: String) {
        viewModelScope.launch {
            val existing = favoriteDao.isFavorite(fishId).first()
            if (existing != null) {
                favoriteDao.removeFavorite(fishId)
            } else {
                favoriteDao.addFavorite(
                    com.example.aqualife.data.local.entity.FavoriteEntity(
                        fishId = fishId
                    )
                )
            }
        }
    }
}


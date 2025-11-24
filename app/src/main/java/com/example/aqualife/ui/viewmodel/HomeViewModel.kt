package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FishRepository
) : ViewModel() {

    // UI State - Always from local database (Offline-First)
    val allFish: StateFlow<List<FishEntity>> = repository.getAllFish()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Loading state for Skeleton animation
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        // Start real-time sync when ViewModel is created
        startRealtimeSync()
        
        // Initialize database if empty
        initializeIfNeeded()
        
        // Check loading state
        checkDataStatus()
    }

    private fun startRealtimeSync() {
        viewModelScope.launch {
            repository.startRealtimeSync()
                .catch { e ->
                    _error.value = e.message
                }
                .collect {
                    // Data synced, UI will auto-update via allFish Flow
                }
        }
    }

    private fun initializeIfNeeded() {
        viewModelScope.launch {
            repository.getAllFish().first().let { fishList ->
                if (fishList.isEmpty()) {
                    // Database is empty, initialize with default data
                    repository.initializeDefaultFish()
                }
            }
        }
    }

    private fun checkDataStatus() {
        viewModelScope.launch {
            repository.getAllFish().collect { list ->
                _isLoading.value = list.isEmpty()
            }
        }
    }

    fun getFishByCategory(category: String): Flow<List<FishEntity>> {
        return if (category == "all") {
            repository.getAllFish()
        } else {
            repository.getFishByCategory(category)
        }
    }

    fun searchFish(query: String): Flow<List<FishEntity>> {
        return repository.searchFish(query)
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.loadFishFromApi()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}


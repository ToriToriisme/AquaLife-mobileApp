package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.data.preferences.SearchHistoryPreferences
import com.example.aqualife.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HomeDisplayState {
    object DefaultView : HomeDisplayState()
    data class FilteredView(val category: String) : HomeDisplayState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FishRepository,
    private val searchHistoryPreferences: SearchHistoryPreferences
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

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private var hasObservedInitialData = false

    private val _homeFishList = MutableStateFlow<List<FishEntity>>(emptyList())
    val homeFishList: StateFlow<List<FishEntity>> = _homeFishList.asStateFlow()

    private val _displayState = MutableStateFlow<HomeDisplayState>(HomeDisplayState.DefaultView)
    val displayState: StateFlow<HomeDisplayState> = _displayState.asStateFlow()

    private var homeFishJob: Job? = null
    private var historyJob: Job? = null

    init {
        // Auto-initialize database with 80 real fish on first launch
        // Handles: Local DB check -> Firebase check -> Seed data
        repository.initializeData()
        
        // Start real-time sync for price updates
        startRealtimeSync()
        
        // Monitor data loading status
        checkDataStatus()

        observeAllFishForHome()
        observeHistory()
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

    private fun checkDataStatus() {
        viewModelScope.launch {
            repository.getAllFish().collect {
                if (!hasObservedInitialData) {
                    hasObservedInitialData = true
                    _isLoading.value = false
                } else if (_isLoading.value) {
                    _isLoading.value = false
                }
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

    fun getFilteredFish(
        query: String,
        category: String?,
        minPrice: Double,
        maxPrice: Double,
        minRating: Float?,
        discountOnly: Boolean,
        sortBy: String
    ): Flow<List<FishEntity>> {
        return repository.getFilteredFish(
            category = when {
                query.isNotBlank() && category == null -> null
                category == "all" -> null
                else -> category
            },
            minPrice = minPrice,
            maxPrice = maxPrice,
            minRating = minRating,
            onlyDiscount = discountOnly,
            sortBy = sortBy
        ).let { baseFlow ->
            if (query.isBlank()) {
                baseFlow
            } else {
                baseFlow.map { list ->
                    list.filter {
                        it.name.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                    }
                }
            }
        }
    }

    fun addSearchHistory(term: String) {
        if (term.isBlank()) return
        val current = _searchHistory.value.toMutableList()
        current.remove(term)
        current.add(0, term)
        val updated = current.take(8)
        _searchHistory.value = updated
        viewModelScope.launch {
            searchHistoryPreferences.saveHistory(updated)
        }
    }

    private fun observeAllFishForHome() {
        homeFishJob?.cancel()
        homeFishJob = viewModelScope.launch {
            repository.getAllFish().collectLatest { list ->
                _homeFishList.value = list
            }
        }
        _displayState.value = HomeDisplayState.DefaultView
    }

    fun selectCategoryFilter(category: String) {
        if (category.equals("tất cả", ignoreCase = true)) {
            resetToDefaultView()
            return
        }
        _displayState.value = HomeDisplayState.FilteredView(category)
        homeFishJob?.cancel()
        homeFishJob = viewModelScope.launch {
            repository.getFishByCategory(category).collectLatest { list ->
                _homeFishList.value = list
            }
        }
    }

    fun resetToDefaultView() {
        observeAllFishForHome()
    }

    private fun observeHistory() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            searchHistoryPreferences.historyFlow.collectLatest { stored ->
                _searchHistory.value = stored
            }
        }
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


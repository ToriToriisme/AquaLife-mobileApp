package com.example.aqualife.ui.viewmodel

// ============================================================================
// ANDROIDX IMPORTS
// ============================================================================
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// ============================================================================
// THIRD-PARTY IMPORTS
// ============================================================================
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============================================================================
// LOCAL IMPORTS
// ============================================================================
import com.example.aqualife.data.local.entity.FishEntity
import com.example.aqualife.data.preferences.SearchHistoryPreferences
import com.example.aqualife.data.repository.FishRepository

// ============================================================================
// HOME VIEWMODEL
// ============================================================================

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

    // Home fish list (for filtered view)
    private val _homeFishList = MutableStateFlow<List<FishEntity>>(emptyList())
    val homeFishList: StateFlow<List<FishEntity>> = _homeFishList.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Display State (Default or Filtered)
    private val _displayState = MutableStateFlow<HomeDisplayState>(HomeDisplayState.DefaultView)
    val displayState: StateFlow<HomeDisplayState> = _displayState.asStateFlow()

    // Search History
    val searchHistory: StateFlow<List<String>> = searchHistoryPreferences.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Always initialize data when ViewModel is created
        repository.initializeData()
        
        // Observe all fish for home list
        viewModelScope.launch {
            repository.getAllFish().collect { list ->
                _homeFishList.value = list
                _isLoading.value = false
            }
        }
    }

    /**
     * Called when user clicks on a category icon (Cá biển, Cá sông...)
     */
    fun selectCategoryFilter(category: String) {
        if (category == "Tất cả") {
            _displayState.value = HomeDisplayState.DefaultView
        } else {
            _displayState.value = HomeDisplayState.FilteredView(category)
        }
    }

    /**
     * Reset to default view
     */
    fun resetToDefaultView() {
        _displayState.value = HomeDisplayState.DefaultView
    }

    /**
     * Get fish by category
     */
    fun getFishByCategory(category: String): StateFlow<List<FishEntity>> {
        return repository.getFishByCategory(category)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Get filtered fish with advanced filters
     */
    fun getFilteredFish(
        query: String = "",
        category: String? = null,
        minPrice: Double = 0.0,
        maxPrice: Double = 100000000.0,
        minRating: Float? = null,
        discountOnly: Boolean = false,
        sortBy: String = "name"
    ): StateFlow<List<FishEntity>> {
        val baseFlow = repository.getFilteredFish(
            category = category,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minRating = minRating,
            onlyDiscount = discountOnly,
            sortBy = sortBy
        )
        
        return if (query.isBlank()) {
            baseFlow.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        } else {
            val normalizedQuery = normalizeVietnamese(query.trim().lowercase())
            baseFlow.map { list ->
                list.filter { fish ->
                    val normalizedName = normalizeVietnamese(fish.name.lowercase())
                    val normalizedCategory = normalizeVietnamese(fish.category.lowercase())
                    normalizedName.contains(normalizedQuery) ||
                        normalizedCategory.contains(normalizedQuery) ||
                        fish.name.contains(query, ignoreCase = true) ||
                        fish.category.contains(query, ignoreCase = true)
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        }
    }

    /**
     * Search fish by query (with Vietnamese normalization)
     */
    fun searchFish(query: String): StateFlow<List<FishEntity>> {
        val normalizedQuery = normalizeVietnamese(query.trim().lowercase())
        return repository.searchFish(normalizedQuery)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    /**
     * Add search term to history
     */
    fun addSearchHistory(term: String) {
        viewModelScope.launch {
            val currentHistory = searchHistory.value.toMutableList()
            // Remove if exists
            currentHistory.remove(term)
            // Add to front
            currentHistory.add(0, term)
            // Keep only last 10
            val updatedHistory = currentHistory.take(10)
            searchHistoryPreferences.saveHistory(updatedHistory)
        }
    }

    /**
     * Clear search history
     */
    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryPreferences.clear()
        }
    }

    /**
     * Normalize Vietnamese text for accent-insensitive search
     */
    private fun normalizeVietnamese(text: String): String {
        return text
            .replace("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ".toRegex(), "a")
            .replace("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ".toRegex(), "e")
            .replace("ì|í|ị|ỉ|ĩ".toRegex(), "i")
            .replace("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ".toRegex(), "o")
            .replace("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ".toRegex(), "u")
            .replace("ỳ|ý|ỵ|ỷ|ỹ".toRegex(), "y")
            .replace("đ", "d")
            .replace("À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ".toRegex(), "A")
            .replace("È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ".toRegex(), "E")
            .replace("Ì|Í|Ị|Ỉ|Ĩ".toRegex(), "I")
            .replace("Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ".toRegex(), "O")
            .replace("Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ".toRegex(), "U")
            .replace("Ỳ|Ý|Ỵ|Ỷ|Ỹ".toRegex(), "Y")
            .replace("Đ", "D")
    }
}

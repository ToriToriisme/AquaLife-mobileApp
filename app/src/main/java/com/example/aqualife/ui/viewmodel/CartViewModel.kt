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
import com.example.aqualife.data.local.dao.CartDao
import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.CartEntity
import com.example.aqualife.data.local.entity.FishEntity

// ============================================================================
// CART VIEWMODEL
// ============================================================================

data class CartItemUi(
    val fish: FishEntity,
    val quantity: Int
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao,
    private val fishDao: FishDao,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private fun getCurrentUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "guest"
    }

    // Combine cart items with fish details
    val cartItems: StateFlow<List<CartItemUi>> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val userId = auth.currentUser?.uid ?: "guest"
            trySend(userId)
        }
        firebaseAuth.addAuthStateListener(listener)
        trySend(getCurrentUserId())
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }
        .flatMapLatest { userId ->
            cartDao.getAllCartItems(userId)
                .flatMapLatest { cartItems ->
                    if (cartItems.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            cartItems.map { cartItem ->
                                fishDao.getFishById(cartItem.fishId)
                                    .map { it to cartItem.quantity }
                            }
                        ) { fishQuantityPairs ->
                            fishQuantityPairs.mapNotNull { (fish, quantity) ->
                                fish?.let { CartItemUi(it, quantity) }
                            }
                        }
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalPrice: StateFlow<Double> = cartItems
        .map { items ->
            items.sumOf { it.fish.price * it.quantity }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun addToCart(fish: FishEntity, quantity: Int = 1) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val existingItem = cartDao.getCartItem(userId, fish.id).first()
            if (existingItem != null) {
                cartDao.updateQuantity(userId, fish.id, existingItem.quantity + quantity)
            } else {
                cartDao.insertCartItem(
                    CartEntity(
                        userId = userId,
                        fishId = fish.id,
                        quantity = quantity
                    )
                )
            }
        }
    }

    fun updateQuantity(fishId: String, newQuantity: Int) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            if (newQuantity <= 0) {
                removeFromCart(fishId)
            } else {
                cartDao.updateQuantity(userId, fishId, newQuantity)
            }
        }
    }

    fun removeFromCart(fishId: String) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            cartDao.deleteCartItem(userId, fishId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            cartDao.clearCart(userId)
        }
    }
}

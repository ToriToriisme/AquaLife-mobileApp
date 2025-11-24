package com.example.aqualife.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aqualife.data.local.dao.CartDao
import com.example.aqualife.data.local.dao.FishDao
import com.example.aqualife.data.local.entity.CartEntity
import com.example.aqualife.data.local.entity.FishEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItemUi(
    val fish: FishEntity,
    val quantity: Int
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartDao: CartDao,
    private val fishDao: FishDao
) : ViewModel() {

    // Combine cart items with fish details
    val cartItems: StateFlow<List<CartItemUi>> = cartDao.getAllCartItems()
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
            val existingItem = cartDao.getCartItem(fish.id).first()
            if (existingItem != null) {
                cartDao.updateQuantity(fish.id, existingItem.quantity + quantity)
            } else {
                cartDao.insertCartItem(
                    CartEntity(
                        fishId = fish.id,
                        quantity = quantity
                    )
                )
            }
        }
    }

    fun updateQuantity(fishId: String, newQuantity: Int) {
        viewModelScope.launch {
            if (newQuantity <= 0) {
                removeFromCart(fishId)
            } else {
                cartDao.updateQuantity(fishId, newQuantity)
            }
        }
    }

    fun removeFromCart(fishId: String) {
        viewModelScope.launch {
            cartDao.deleteCartItem(fishId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartDao.clearCart()
        }
    }
}


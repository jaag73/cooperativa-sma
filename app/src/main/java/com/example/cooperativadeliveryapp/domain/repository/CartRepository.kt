package com.example.cooperativadeliveryapp.domain.repository

import com.example.cooperativadeliveryapp.domain.model.CartItem
import com.example.cooperativadeliveryapp.domain.model.MultiBusinessCart
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartFlow: StateFlow<MultiBusinessCart>
    fun addToCart(item: CartItem): Result<Unit>
    fun updateQuantity(cartItemId: String, newQuantity: Int)
    fun removeFromCart(cartItemId: String)
    fun clearCart()
}

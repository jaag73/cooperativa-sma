package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.CartItem
import mx.cooperativasma.app.domain.model.MultiBusinessCart
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val cartFlow: StateFlow<MultiBusinessCart>
    fun addToCart(item: CartItem): Result<Unit>
    fun updateQuantity(cartItemId: String, newQuantity: Int)
    fun removeFromCart(cartItemId: String)
    fun clearCart()
}

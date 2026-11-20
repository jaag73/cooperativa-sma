package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.CartItem
import mx.cooperativasma.app.domain.model.MultiBusinessCart
import mx.cooperativasma.app.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor() : CartRepository {

    private val _cartFlow = MutableStateFlow(MultiBusinessCart())
    override val cartFlow: StateFlow<MultiBusinessCart> = _cartFlow.asStateFlow()

    override fun addToCart(item: CartItem): Result<Unit> {
        _cartFlow.update { currentCart ->
            val updatedItems = currentCart.items.toMutableList()

            // Si el item con los mismos options ya existe, incrementamos quantity
            val existingItemIndex = updatedItems.indexOfFirst {
                it.product.id == item.product.id && it.selectedOptions == item.selectedOptions && it.specialNotes == item.specialNotes
            }

            if (existingItemIndex != -1) {
                val existingItem = updatedItems[existingItemIndex]
                updatedItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + item.quantity)
            } else {
                updatedItems.add(item)
            }
            currentCart.copy(items = updatedItems)
        }
        return Result.success(Unit)
    }

    override fun updateQuantity(cartItemId: String, newQuantity: Int) {
        _cartFlow.update { currentCart ->
            val updatedItems = currentCart.items.map {
                if (it.cartItemId == cartItemId) it.copy(quantity = newQuantity) else it
            }.filter { it.quantity > 0 }
            currentCart.copy(items = updatedItems)
        }
    }

    override fun removeFromCart(cartItemId: String) {
        _cartFlow.update { currentCart ->
            val updatedItems = currentCart.items.filter { it.cartItemId != cartItemId }
            currentCart.copy(items = updatedItems)
        }
    }

    override fun clearCart() {
        _cartFlow.value = MultiBusinessCart()
    }
}

package mx.cooperativasma.app.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.MultiBusinessCart
import mx.cooperativasma.app.domain.model.PaymentMethod
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.CartRepository
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import mx.cooperativasma.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartCheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderLiveRepository: OrderLiveRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val cartFlow: StateFlow<MultiBusinessCart> = cartRepository.cartFlow

    private val _selectedPaymentMethod = MutableStateFlow(PaymentMethod.CASH_ON_DELIVERY)
    val selectedPaymentMethod: StateFlow<PaymentMethod> = _selectedPaymentMethod.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartRepository.removeFromCart(cartItemId)
        } else {
            cartRepository.updateQuantity(cartItemId, newQuantity)
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.update { method }
    }

    fun submitOrder(onSuccess: () -> Unit) {
        val cart = cartFlow.value
        if (cart.items.isEmpty() || cart.isCapacityExceeded) return

        viewModelScope.launch {
            _isProcessing.value = true
            
            // Check availability before submitting
            val allAvailable = cart.items.all { it.product.isAvailable }
            if (!allAvailable) {
                // Should show error to user but for now we just cancel
                _isProcessing.value = false
                return@launch
            }

            val uid = authRepository.getCurrentUserId()
            if (uid != null) {
                val profileResult = userRepository.getUserProfile(uid)
                val profile = profileResult.getOrNull()
                
                if (profile != null) {
                    orderLiveRepository.createOrderFromCart(
                        cart = cart,
                        clientProfile = profile,
                        paymentMethod = _selectedPaymentMethod.value
                    )
                }
            }
            
            kotlinx.coroutines.delay(1000) 
            cartRepository.clearCart()
            _isProcessing.value = false
            onSuccess()
        }
    }
}

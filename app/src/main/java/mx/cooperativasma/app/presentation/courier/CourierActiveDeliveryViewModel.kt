package mx.cooperativasma.app.presentation.courier

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.LiveOrder
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourierActiveDeliveryUiState(
    val order: LiveOrder? = null,
    val isLoading: Boolean = true,
    val isDeliveryFinished: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CourierActiveDeliveryViewModel @Inject constructor(
    private val orderLiveRepository: OrderLiveRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(CourierActiveDeliveryUiState())
    val uiState: StateFlow<CourierActiveDeliveryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val courierId = mx.cooperativasma.app.domain.repository.AuthRepository::class.java.name // Placeholder
            
            orderLiveRepository.allLiveOrdersFlow.collect { orders ->
                val currentOrder = orders.find { it.orderId == orderId } 
                _uiState.update { it.copy(order = currentOrder, isLoading = false) }
            }
        }
    }

    fun completeStop(stopId: String) {
        viewModelScope.launch {
            orderLiveRepository.completeRouteStop(orderId, stopId)
        }
    }

    fun startTransitToClient() {
        viewModelScope.launch {
            orderLiveRepository.startTransitToClient(orderId)
        }
    }

    fun finalizeDelivery(pin: String) {
        viewModelScope.launch {
            orderLiveRepository.finalizeDeliveryWithPin(orderId, pin).fold(
                onSuccess = {
                    _uiState.update { it.copy(isDeliveryFinished = true) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.message) }
                }
            )
        }
    }
    
    fun resetDeliveryState() {
        _uiState.update { it.copy(isDeliveryFinished = false) }
    }
}

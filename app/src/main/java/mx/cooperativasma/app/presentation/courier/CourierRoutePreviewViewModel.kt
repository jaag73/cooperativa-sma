package mx.cooperativasma.app.presentation.courier

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.CourierOrderOpportunity
import mx.cooperativasma.app.domain.repository.CourierLogisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CourierRoutePreviewUiState(
    val isLoading: Boolean = true,
    val order: CourierOrderOpportunity? = null,
    val error: String? = null,
    val isAccepted: Boolean = false,
    val isSuggesting: Boolean = false
)

@HiltViewModel
class CourierRoutePreviewViewModel @Inject constructor(
    private val logisticsRepository: CourierLogisticsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(CourierRoutePreviewUiState())
    val uiState: StateFlow<CourierRoutePreviewUiState> = _uiState.asStateFlow()

    init {
        loadOrderPreview()
    }

    private fun loadOrderPreview() {
        viewModelScope.launch {
            try {
                // Recuperar la oportunidad tanto de disponibles como de programados
                val available = logisticsRepository.getAvailableOrdersFlow().firstOrNull() ?: emptyList()
                val scheduled = logisticsRepository.getScheduledOrdersFlow().firstOrNull() ?: emptyList()

                val order = available.find { it.orderId == orderId } ?: scheduled.find { it.orderId == orderId }

                if (order != null) {
                    _uiState.update { it.copy(isLoading = false, order = order) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "El pedido ya no está disponible.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun acceptOrder() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logisticsRepository.acceptOrder(orderId).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isAccepted = true) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
            )
        }
    }

    fun suggestToPeer(peerName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSuggesting = true) }
            logisticsRepository.suggestOrderToPeer(orderId, peerName).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSuggesting = false, isAccepted = true) } 
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSuggesting = false, error = error.message) }
                }
            )
        }
    }
    
    fun resetAcceptedState() {
        _uiState.update { it.copy(isAccepted = false) }
    }
}

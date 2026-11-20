package mx.cooperativasma.app.presentation.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.alert.OrderAlertManager
import mx.cooperativasma.app.domain.notification.OrderNotificationHelper
import mx.cooperativasma.app.domain.model.OrderLifecycleStatus
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.BusinessMenuRepository
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import mx.cooperativasma.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BusinessDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val orderLiveRepository: OrderLiveRepository,
    private val menuRepository: BusinessMenuRepository,
    private val alertManager: OrderAlertManager,
    private val notificationHelper: OrderNotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<BusinessUiState>(BusinessUiState.Loading)
    val uiState: StateFlow<BusinessUiState> = _uiState.asStateFlow()

    private var lastKnownOrderIds = setOf<String>()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val uid = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                val profileResult = userRepository.getUserProfile(uid)
                val profile = profileResult.getOrNull()

                if (profile == null) {
                    _uiState.value = BusinessUiState.Error("Perfil no encontrado")
                    return@launch
                }

                combine(
                    orderLiveRepository.allLiveOrdersFlow.map { list -> list.filter { it.cart.participatingBusinesses.contains(profile.name) } },
                    menuRepository.getProductsFlow(uid)
                ) { orders, menu ->
                    val currentPendingOrderIds = orders.filter { it.status == OrderLifecycleStatus.PENDING_ACCEPTANCE }.map { it.orderId }.toSet()
                    val newPendingOrderIds = currentPendingOrderIds - lastKnownOrderIds
                    
                    if (newPendingOrderIds.isNotEmpty()) {
                        alertManager.startAlertLoop()
                        newPendingOrderIds.forEach { orderId ->
                            notificationHelper.showNewOrderForBusinessNotification(orderId)
                        }
                    } else if (currentPendingOrderIds.isEmpty()) {
                        alertManager.stopAlert()
                    }
                    lastKnownOrderIds = currentPendingOrderIds

                    val currentState = _uiState.value
                    if (currentState is BusinessUiState.Success) {
                        currentState.copy(orders = orders, menu = menu)
                    } else {
                        BusinessUiState.Success(userProfile = profile, orders = orders, menu = menu)
                    }
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = BusinessUiState.Error(e.message ?: "Error al cargar datos")
            }
        }
    }

    fun onEvent(event: BusinessUiEvent) {
        val currentState = _uiState.value as? BusinessUiState.Success ?: return

        when (event) {
            is BusinessUiEvent.OnTabSelected -> {
                _uiState.value = currentState.copy(currentTab = event.tab)
            }
            is BusinessUiEvent.OnOrderFilterSelected -> {
                _uiState.value = currentState.copy(currentOrderFilter = event.filter)
            }
            is BusinessUiEvent.OnMenuSubcategorySelected -> {
                _uiState.value = currentState.copy(currentMenuSubcategory = event.subcategory)
            }
            is BusinessUiEvent.OnAcceptOrder -> {
                alertManager.stopAlert()
                val profileName = currentState.userProfile.name
                viewModelScope.launch {
                    orderLiveRepository.acceptBusinessOrder(event.orderId, profileName)
                }
            }
            is BusinessUiEvent.OnRejectOrder -> {
                alertManager.stopAlert()
                // updateOrderStatus(event.orderId, OrderStatus.CANCELLED)
            }
            is BusinessUiEvent.OnMarkOrderReady -> {
                val profileName = currentState.userProfile.name
                viewModelScope.launch {
                    orderLiveRepository.markBusinessOrderReady(event.orderId, profileName)
                }
            }
            is BusinessUiEvent.OnToggleProductAvailability -> {
                viewModelScope.launch {
                    menuRepository.toggleProductAvailability(event.productId, event.isAvailable)
                }
            }
            is BusinessUiEvent.OnNavigateToEditProduct -> {
                // Se manejaría mediante un callback al NavHost
            }
            BusinessUiEvent.OnStopAlert -> {
                alertManager.stopAlert()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        alertManager.stopAlert()
    }
}

package mx.cooperativasma.app.presentation.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import mx.cooperativasma.app.domain.model.LiveOrder
import mx.cooperativasma.app.domain.notification.OrderNotificationHelper
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientOrderTrackingViewModel @Inject constructor(
    authRepository: AuthRepository,
    orderLiveRepository: OrderLiveRepository,
    notificationHelper: OrderNotificationHelper
) : ViewModel() {

    private var hasNotifiedTransit = false

    val activeOrder: StateFlow<LiveOrder?> =
        orderLiveRepository.getClientActiveOrder(authRepository.getCurrentUserId() ?: "")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            activeOrder.collect { order ->
                if (order != null && order.status == mx.cooperativasma.app.domain.model.OrderLifecycleStatus.IN_TRANSIT_TO_CLIENT && !hasNotifiedTransit) {
                    notificationHelper.showCourierInTransitNotification(order.orderId, order.courierArrivalEtaMinutes)
                    hasNotifiedTransit = true
                }
                if (order == null || order.status == mx.cooperativasma.app.domain.model.OrderLifecycleStatus.DELIVERED) {
                    hasNotifiedTransit = false // reset for next orders
                }
            }
        }
    }
}

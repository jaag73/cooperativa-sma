package com.example.cooperativadeliveryapp.domain.repository

import com.example.cooperativadeliveryapp.domain.model.LiveOrder
import com.example.cooperativadeliveryapp.domain.model.MultiBusinessCart
import com.example.cooperativadeliveryapp.domain.model.PaymentMethod
import com.example.cooperativadeliveryapp.domain.model.UserProfile
import kotlinx.coroutines.flow.StateFlow

interface OrderLiveRepository {
    val allLiveOrdersFlow: StateFlow<List<LiveOrder>>

    suspend fun createOrderFromCart(cart: MultiBusinessCart, clientProfile: UserProfile, paymentMethod: PaymentMethod): LiveOrder
    suspend fun acceptBusinessOrder(orderId: String, businessName: String) // Pasa de PENDING_ACCEPTANCE a PREPARING_AT_BUSINESS
    suspend fun markBusinessOrderReady(orderId: String, businessName: String)
    suspend fun assignCourierToOrder(orderId: String, courierId: String)
    suspend fun completeRouteStop(orderId: String, stopId: String)
    suspend fun startTransitToClient(orderId: String)
    suspend fun finalizeDeliveryWithPin(orderId: String, enteredPin: String): Result<Unit>
    suspend fun cancelOrder(orderId: String): Result<Unit>

    // Auxiliares para vistas
    fun getClientActiveOrder(clientId: String): StateFlow<LiveOrder?>
}

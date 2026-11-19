package com.example.cooperativadeliveryapp.domain.repository

import com.example.cooperativadeliveryapp.domain.model.Order
import com.example.cooperativadeliveryapp.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrdersFlow(businessId: String): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit>
}

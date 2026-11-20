package mx.cooperativasma.app.domain.repository

import mx.cooperativasma.app.domain.model.Order
import mx.cooperativasma.app.domain.model.OrderStatus
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getOrdersFlow(businessId: String): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit>
}

package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.Order
import mx.cooperativasma.app.domain.model.OrderItem
import mx.cooperativasma.app.domain.model.OrderStatus
import mx.cooperativasma.app.domain.model.PaymentMethod
import mx.cooperativasma.app.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor() : OrderRepository {

    private val dummyOrders = mutableListOf(
        Order(
            orderId = "1",
            businessId = "dummy_uid", // O el id simulado
            status = OrderStatus.RECEIVED,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            items = listOf(OrderItem(name = "Tacos al Pastor", quantity = 3, unitPrice = 61.66, specialInstructions = "Sin cebolla")),
            totalAmount = 185.0,
            createdAt = System.currentTimeMillis() - 100000
        ),
        Order(
            orderId = "2",
            businessId = "dummy_uid",
            status = OrderStatus.PREPARING,
            paymentMethod = PaymentMethod.CREDIT_DEBIT_CARD,
            items = listOf(OrderItem(name = "Pizza Grande", quantity = 1, unitPrice = 240.0)),
            totalAmount = 240.0,
            createdAt = System.currentTimeMillis() - 600000
        ),
        Order(
            orderId = "3",
            businessId = "dummy_uid",
            status = OrderStatus.READY_FOR_PICKUP,
            paymentMethod = PaymentMethod.DIRECT_TRANSFER_SPEI,
            items = listOf(OrderItem(name = "Hamburguesa Doble", quantity = 2, unitPrice = 155.0)),
            totalAmount = 310.0,
            createdAt = System.currentTimeMillis() - 1800000
        ),
        Order(
            orderId = "4",
            businessId = "dummy_uid",
            status = OrderStatus.DELIVERED,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            items = listOf(OrderItem(name = "Enchiladas", quantity = 1, unitPrice = 150.0)),
            totalAmount = 150.0,
            createdAt = System.currentTimeMillis() - 86400000
        ),
        Order(
            orderId = "5",
            businessId = "dummy_uid",
            status = OrderStatus.DELIVERED,
            paymentMethod = PaymentMethod.CREDIT_DEBIT_CARD,
            items = listOf(OrderItem(name = "Alitas", quantity = 2, unitPrice = 210.0)),
            totalAmount = 420.0,
            createdAt = System.currentTimeMillis() - 4000000
        ),
        Order(
            orderId = "6",
            businessId = "dummy_uid",
            status = OrderStatus.DELIVERED,
            paymentMethod = PaymentMethod.DIRECT_TRANSFER_SPEI,
            items = listOf(OrderItem(name = "Combo Tacos", quantity = 2, unitPrice = 145.0)),
            totalAmount = 290.0,
            createdAt = System.currentTimeMillis() - 2000000
        )
    )

    private val ordersStateFlow = MutableStateFlow<List<Order>>(dummyOrders.toList())

    override fun getOrdersFlow(businessId: String): Flow<List<Order>> {
        // En un caso real filtraría por businessId, pero como devolvemos datos mock a un id genérico:
        return ordersStateFlow.map { orders ->
            orders.filter { it.businessId == "dummy_uid" || it.businessId == businessId }
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        val index = dummyOrders.indexOfFirst { it.orderId == orderId }
        if (index != -1) {
            val order = dummyOrders[index]
            dummyOrders[index] = order.copy(status = newStatus)
            ordersStateFlow.value = dummyOrders.toList()
            return Result.success(Unit)
        }
        return Result.failure(Exception("Order not found"))
    }
}

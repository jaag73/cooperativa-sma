package mx.cooperativasma.app.domain.model

enum class OrderStatus {
    RECEIVED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}

enum class PaymentMethod {
    CREDIT_DEBIT_CARD, DIRECT_TRANSFER_SPEI, CASH_ON_DELIVERY
}

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val specialInstructions: String? = null
)

data class Order(
    val orderId: String = "",
    val businessId: String = "",
    val clientId: String = "",
    val courierId: String? = null,
    val status: OrderStatus = OrderStatus.RECEIVED,
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_DEBIT_CARD,
    val estimatedPrepTimeMinutes: Int = 20,
    val createdAt: Long = System.currentTimeMillis()
)

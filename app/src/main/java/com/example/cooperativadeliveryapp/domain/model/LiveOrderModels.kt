package com.example.cooperativadeliveryapp.domain.model

enum class OrderLifecycleStatus {
    PENDING_ACCEPTANCE,    // Recibido en negocio con tiempos ya calculados
    PREPARING_AT_BUSINESS, // Negocios preparando el pedido
    READY_FOR_PICKUP,      // Listo para que el repartidor recolecte
    COURIER_ASSIGNED,      // Repartidor en camino a recolectar
    PICKED_UP_AT_BIKE,     // Repartidor confirmó recolección y asegura estiba
    IN_TRANSIT_TO_CLIENT,  // Todas las recolecciones listas, en camino al cliente
    DELIVERED,             // Entregado y validado con PIN
    CANCELLED              // Cancelado por negocio o cliente
}

data class LiveOrder(
    val orderId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientAddress: String = "",
    val clientCoordinates: GeoLocation = GeoLocation(),
    val cart: MultiBusinessCart = MultiBusinessCart(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val deliveryPin: String = "",
    val status: OrderLifecycleStatus = OrderLifecycleStatus.PENDING_ACCEPTANCE,
    val assignedCourierId: String? = null,
    val readyBusinesses: List<String> = emptyList(), // Negocios que ya marcaron "listo"
    val completedStops: List<String> = emptyList(),  // IDs de paradas de ruta completadas
    val courierRouteStops: List<RouteStop> = emptyList(), // Rutas generadas para el repartidor
    val calculatedPrepTimeMinutes: Int = 0,
    val courierArrivalEtaMinutes: Int = 0,
    val recommendedFireDelayMinutes: Int = 0
)

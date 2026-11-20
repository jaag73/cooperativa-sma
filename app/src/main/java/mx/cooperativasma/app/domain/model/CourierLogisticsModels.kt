package mx.cooperativasma.app.domain.model

object CourierVehicleSpecs {
    // Costos operativos reales en Guanajuato (mantenimiento hecho por el propio repartidor)
    const val OIL_AND_SPARK_PLUG_COST = 280.0    // Aceite 4T + bujía cada 2,500 km ($0.112/km)
    const val OIL_CHANGE_INTERVAL_KM = 2500.0
    const val TIRES_SET_COST = 1950.0            // Juego de llantas cada 15,000 km ($0.130/km)
    const val TIRES_INTERVAL_KM = 15000.0
    const val BRAKES_AND_CHAIN_COST = 980.0      // Balatas + kit de arrastre 428H cada 11,000 km ($0.089/km)
    const val BRAKES_CHAIN_INTERVAL_KM = 11000.0

    val MAINTENANCE_RESERVE_PER_KM: Double =
        (OIL_AND_SPARK_PLUG_COST / OIL_CHANGE_INTERVAL_KM) +
        (TIRES_SET_COST / TIRES_INTERVAL_KM) +
        (BRAKES_AND_CHAIN_COST / BRAKES_CHAIN_INTERVAL_KM) // ~$0.331 MXN/km

    const val GAS_PRICE_PER_LITER = 24.50        // Gasolina regular Guanajuato
    const val REAL_KM_PER_LITER = 30.0           // Promedio ciudad con empedrado y topes
    val FUEL_COST_PER_KM: Double = GAS_PRICE_PER_LITER / REAL_KM_PER_LITER // ~$0.817 MXN/km

    val TOTAL_VEHICLE_COST_PER_KM: Double = FUEL_COST_PER_KM + MAINTENANCE_RESERVE_PER_KM // ~$1.15 MXN/km

    // Meta mensual de dignidad: $12,000 netos / 26 días de 6 horas
    const val MONTHLY_NET_GOAL = 12000.0
    const val DAILY_NET_GOAL = 12000.0 / 26.0    // ~$461.54 MXN/día
    const val MULTI_BUSINESS_STOP_BONUS = 15.0   // Bono directo al repartidor por parada adicional
}

data class CourierRestBase(
    val id: String = "base_home",
    val label: String = "Mi Casa", // ej. "Mi Casa", "Casa de Familiar / Amigo"
    val neighborhood: String = "Santas Marías",
    val postalCode: String = "37712",
    val isCurrentActiveBase: Boolean = true
)

data class DeliveryFinancialBreakdown(
    val grossFee: Double = 65.0,
    val estimatedFuelCost: Double = 5.72,     // Km totales * FUEL_COST_PER_KM
    val maintenanceReserve: Double = 2.32,   // Km totales * MAINTENANCE_RESERVE_PER_KM
    val returnTravelCost: Double = 6.50,     // Compensación de retorno a la base de descanso activa
    val multiBusinessBonus: Double = 0.0,    // $15.00 si recolecta en 2 o más negocios
    val netEarnings: Double = 50.46          // Ganancia limpia real en el bolsillo
)

enum class RouteStopType {
    PICKUP_DRY_GOODS, // Recolección de carga seca/robusta (primero en ruta)
    PICKUP_HOT_FOOD,  // Recolección de comida caliente/delicada (al final antes de ir al cliente)
    DROPOFF_CLIENT    // Entrega final en domicilio
}

data class RouteStop(
    val stopId: String = "",
    val stopOrder: Int = 1,
    val stopType: RouteStopType = RouteStopType.PICKUP_DRY_GOODS,
    val locationName: String = "",
    val addressSummary: String = "",
    val itemsSummary: List<String> = emptyList(),
    val cargoPlacementNote: String = "Mochila térmica",
    val isCompleted: Boolean = false
)

data class RouteSegment(
    val title: String, // ej. "Acercamiento desde base", "Ruta entrega", "Retorno a base activa"
    val distanceKm: Double,
    val durationMinutes: Int
)

data class CourierOrderOpportunity(
    val orderId: String = "",
    val isScheduled: Boolean = false,
    val scheduledDateTime: String? = null,
    val originNeighborhood: String = "",
    val clientNeighborhood: String = "",
    val participatingBusinesses: List<String> = emptyList(),
    val segments: List<RouteSegment> = emptyList(),
    val stops: List<RouteStop> = emptyList(),
    val finances: DeliveryFinancialBreakdown = DeliveryFinancialBreakdown(),
    val backpackUnits: Int = 2,
    val rackUnits: Int = 0,
    val hasDelicateItems: Boolean = false,
    val status: OrderStatus = OrderStatus.READY_FOR_PICKUP
)

data class HotspotDemandInfo(
    val zoneName: String = "",
    val pendingOrdersCount: Int = 0,
    val couriersWaitingCount: Int = 0,
    val userQueuePosition: Int? = null // Turno FIFO según hora de llegada al sitio
)

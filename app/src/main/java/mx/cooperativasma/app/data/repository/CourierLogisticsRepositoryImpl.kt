package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.*
import mx.cooperativasma.app.domain.repository.AuthRepository
import mx.cooperativasma.app.domain.repository.CourierLogisticsRepository
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourierLogisticsRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val orderLiveRepository: OrderLiveRepository
) : CourierLogisticsRepository {

    private val _restBasesFlow = MutableStateFlow(
        listOf(
            CourierRestBase(id = "base_1", label = "Mi Casa", neighborhood = "Santas Marías", postalCode = "37712", isCurrentActiveBase = true),
            CourierRestBase(id = "base_2", label = "Casa de mi Tía", neighborhood = "San Antonio", postalCode = "37710", isCurrentActiveBase = false)
        )
    )
    override val restBasesFlow = _restBasesFlow

    private val _ordersFlow = MutableStateFlow(
        listOf(
            CourierOrderOpportunity(
                orderId = "o1",
                originNeighborhood = "Centro",
                clientNeighborhood = "Santas Marías", // Cerca de base_1
                participatingBusinesses = listOf("Tortas El Sazón SMA", "Abarrotes La Esquina"),
                segments = listOf(
                    RouteSegment("Acercamiento", 2.1, 7),
                    RouteSegment("Recolección Abarrotes", 0.5, 3),
                    RouteSegment("Recolección Comida", 1.2, 5),
                    RouteSegment("Entrega", 3.5, 12),
                    RouteSegment("Retorno", 2.0, 6)
                ),
                stops = listOf(
                    RouteStop(stopId = "s1", stopOrder = 1, stopType = RouteStopType.PICKUP_DRY_GOODS, locationName = "Abarrotes La Esquina", addressSummary = "San Antonio Abad #12", itemsSummary = listOf("Refresco 2L", "Papas"), cargoPlacementNote = "Parrilla (Ligas)"),
                    RouteStop(stopId = "s2", stopOrder = 2, stopType = RouteStopType.PICKUP_HOT_FOOD, locationName = "Tortas El Sazón SMA", addressSummary = "Mesones #45", itemsSummary = listOf("Torta Milanesa"), cargoPlacementNote = "Mochila (Caliente)"),
                    RouteStop(stopId = "s3", stopOrder = 3, stopType = RouteStopType.DROPOFF_CLIENT, locationName = "Cliente", addressSummary = "Santas Marías, Calle 1", itemsSummary = listOf("Todo el pedido"), cargoPlacementNote = "Entrega final")
                ),
                finances = DeliveryFinancialBreakdown(grossFee = 75.0, estimatedFuelCost = 7.60, maintenanceReserve = 3.08, returnTravelCost = 8.0, multiBusinessBonus = 15.0, netEarnings = 71.32),
                backpackUnits = 2,
                rackUnits = 3,
                hasDelicateItems = false
            ),
            CourierOrderOpportunity(
                orderId = "o2",
                originNeighborhood = "Salida a Querétaro",
                clientNeighborhood = "San Antonio", // Cerca de base_2
                participatingBusinesses = listOf("Panadería Local"),
                segments = listOf(
                    RouteSegment("Acercamiento", 4.0, 10),
                    RouteSegment("Recolección", 0.0, 3),
                    RouteSegment("Entrega", 2.5, 8),
                    RouteSegment("Retorno", 3.0, 9)
                ),
                stops = listOf(
                    RouteStop(stopId = "s4", stopOrder = 1, stopType = RouteStopType.PICKUP_DRY_GOODS, locationName = "Panadería Local", addressSummary = "Libramiento #123", itemsSummary = listOf("Pastel Imposible"), cargoPlacementNote = "Mochila (Frágil)"),
                    RouteStop(stopId = "s5", stopOrder = 2, stopType = RouteStopType.DROPOFF_CLIENT, locationName = "Cliente", addressSummary = "San Antonio, Calle 2", itemsSummary = listOf("Todo el pedido"), cargoPlacementNote = "Entrega final")
                ),
                finances = DeliveryFinancialBreakdown(grossFee = 55.0, estimatedFuelCost = 7.76, maintenanceReserve = 3.14, returnTravelCost = 7.5, multiBusinessBonus = 0.0, netEarnings = 36.60),
                backpackUnits = 1,
                rackUnits = 0,
                hasDelicateItems = true
            )
        )
    )

    private val _scheduledOrdersFlow = MutableStateFlow(
        listOf(
            CourierOrderOpportunity(
                orderId = "sched_1",
                isScheduled = true,
                scheduledDateTime = "Hoy, 19:30 hrs",
                originNeighborhood = "Independencia",
                clientNeighborhood = "Centro",
                participatingBusinesses = listOf("Pizzería"),
                finances = DeliveryFinancialBreakdown(grossFee = 85.0, estimatedFuelCost = 5.0, maintenanceReserve = 2.0, returnTravelCost = 5.0, multiBusinessBonus = 0.0, netEarnings = 73.0)
            )
        )
    )

    private val _hotspotsFlow = MutableStateFlow(
        listOf(
            HotspotDemandInfo(zoneName = "Zona Centro Histórico", pendingOrdersCount = 12, couriersWaitingCount = 4, userQueuePosition = null),
            HotspotDemandInfo(zoneName = "Corredor Salida a Querétaro", pendingOrdersCount = 5, couriersWaitingCount = 1, userQueuePosition = 2), // El user es el #2 en fila
            HotspotDemandInfo(zoneName = "San Antonio / Independencia", pendingOrdersCount = 2, couriersWaitingCount = 0, userQueuePosition = null)
        )
    )

    override fun switchActiveRestBase(baseId: String) {
        val updated = _restBasesFlow.value.map { it.copy(isCurrentActiveBase = (it.id == baseId)) }
        _restBasesFlow.value = updated
    }

    override fun getAvailableOrdersFlow(): Flow<List<CourierOrderOpportunity>> {
        // En lógica real: ordenar por proximidad a la base activa (neighborhood match)
        return _ordersFlow.map { orders ->
            val activeBase = _restBasesFlow.value.find { it.isCurrentActiveBase }?.neighborhood
            orders.sortedByDescending { it.clientNeighborhood == activeBase }
        }
    }

    override fun getScheduledOrdersFlow(): Flow<List<CourierOrderOpportunity>> = _scheduledOrdersFlow
    override fun getHotspotDemandFlow(): Flow<List<HotspotDemandInfo>> = _hotspotsFlow

    override fun acceptOrder(orderId: String): Result<Unit> {
        val filtered = _ordersFlow.value.filter { it.orderId != orderId }
        _ordersFlow.value = filtered
        
        // Registrarlo centralmente si fuera necesario en la lógica de negocio
        val courierId = authRepository.getCurrentUserId() ?: "courier_1"
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
            orderLiveRepository.assignCourierToOrder(orderId, courierId)
        }

        return Result.success(Unit)
    }

    override fun suggestOrderToPeer(orderId: String, peerName: String): Result<Unit> {
        val filtered = _ordersFlow.value.filter { it.orderId != orderId }
        _ordersFlow.value = filtered
        return Result.success(Unit)
    }
}

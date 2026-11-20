package mx.cooperativasma.app.data.repository

import mx.cooperativasma.app.domain.model.*
import mx.cooperativasma.app.domain.repository.OrderLiveRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import mx.cooperativasma.app.domain.location.LocationTrackerService
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
class OrderLiveRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val locationTrackerService: LocationTrackerService
) : OrderLiveRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    override val allLiveOrdersFlow: StateFlow<List<LiveOrder>> = callbackFlow {
        val listener = firestore.collection("live_orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull { it.toObject(LiveOrder::class.java) } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override suspend fun createOrderFromCart(
        cart: MultiBusinessCart,
        clientProfile: UserProfile,
        paymentMethod: PaymentMethod
    ): LiveOrder {
        val pin = Random.nextInt(1000, 9999).toString()
        val docRef = firestore.collection("live_orders").document()
        val orderId = docRef.id

        val routeStops = mutableListOf<RouteStop>()
        val businesses = cart.items.groupBy { it.businessName }
        var stopCount = 1

        businesses.forEach { (biz, items) ->
            if (items.none { it.physicalSpecs.thermalRequirement == ThermalRequirement.HOT || it.physicalSpecs.thermalRequirement == ThermalRequirement.COLD || it.physicalSpecs.isFragile }) {
                routeStops.add(RouteStop(stopId = "stop_${stopCount}", stopOrder = stopCount, stopType = RouteStopType.PICKUP_DRY_GOODS, locationName = biz, addressSummary = "Ubicación del Negocio", itemsSummary = items.map { "${it.quantity}x ${it.product.name}" }))
                stopCount++
            }
        }

        businesses.forEach { (biz, items) ->
            if (items.any { it.physicalSpecs.thermalRequirement == ThermalRequirement.HOT || it.physicalSpecs.thermalRequirement == ThermalRequirement.COLD || it.physicalSpecs.isFragile }) {
                routeStops.add(RouteStop(stopId = "stop_${stopCount}", stopOrder = stopCount, stopType = RouteStopType.PICKUP_HOT_FOOD, locationName = biz, addressSummary = "Ubicación del Negocio", itemsSummary = items.map { "${it.quantity}x ${it.product.name}" }, cargoPlacementNote = "Mochila (Temperatura/Frágil)"))
                stopCount++
            }
        }

        routeStops.add(RouteStop(stopId = "stop_${stopCount}", stopOrder = stopCount, stopType = RouteStopType.DROPOFF_CLIENT, locationName = clientProfile.name, addressSummary = clientProfile.structuredAddress?.neighborhoodOrCommunity ?: "Domicilio", itemsSummary = listOf("Entrega Total")))

        val maxPrepTime = cart.items.maxOfOrNull { it.product.physicalSpecs.prepTimeMinutes } ?: 15
        val extraTime = (cart.items.size - 1) * 2
        val calculatedPrepTime = maxPrepTime + extraTime
        val arrivalEta = 15 // Mocked ETA for courier
        val fireDelay = maxOf(0, arrivalEta - calculatedPrepTime)

        val newOrder = LiveOrder(
            orderId = orderId,
            clientId = clientProfile.uid,
            clientName = clientProfile.name,
            clientAddress = clientProfile.structuredAddress?.neighborhoodOrCommunity ?: "Sin dirección",
            clientCoordinates = clientProfile.structuredAddress?.coordinates ?: GeoLocation(),
            cart = cart,
            paymentMethod = paymentMethod,
            deliveryPin = pin,
            status = OrderLifecycleStatus.PENDING_ACCEPTANCE,
            courierRouteStops = routeStops,
            calculatedPrepTimeMinutes = calculatedPrepTime,
            courierArrivalEtaMinutes = arrivalEta,
            recommendedFireDelayMinutes = fireDelay
        )

        docRef.set(newOrder).await()
        return newOrder
    }

    override suspend fun acceptBusinessOrder(orderId: String, businessName: String) {
        val docRef = firestore.collection("live_orders").document(orderId)
        firestore.runTransaction { transaction ->
            val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: return@runTransaction
            if (order.status == OrderLifecycleStatus.PENDING_ACCEPTANCE) {
                transaction.update(docRef, "status", OrderLifecycleStatus.PREPARING_AT_BUSINESS)
            }
        }.await()
    }

    override suspend fun markBusinessOrderReady(orderId: String, businessName: String) {
        val docRef = firestore.collection("live_orders").document(orderId)
        firestore.runTransaction { transaction ->
            val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: return@runTransaction
            val updatedReady = (order.readyBusinesses + businessName).distinct()
            transaction.update(docRef, "readyBusinesses", updatedReady)
            
            val allReady = updatedReady.containsAll(order.cart.participatingBusinesses)
            if (allReady && order.status == OrderLifecycleStatus.PREPARING_AT_BUSINESS) {
                transaction.update(docRef, "status", OrderLifecycleStatus.READY_FOR_PICKUP)
            }
        }.await()
    }

    override suspend fun assignCourierToOrder(orderId: String, courierId: String) {
        val docRef = firestore.collection("live_orders").document(orderId)
        firestore.runTransaction { transaction ->
            val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: return@runTransaction
            if (order.assignedCourierId == null && order.status != OrderLifecycleStatus.CANCELLED) {
                transaction.update(docRef, "assignedCourierId", courierId)
                transaction.update(docRef, "status", OrderLifecycleStatus.COURIER_ASSIGNED)
            } else {
                throw Exception("Order already accepted by another courier or canceled")
            }
        }.await()
    }

    override suspend fun completeRouteStop(orderId: String, stopId: String) {
        val docRef = firestore.collection("live_orders").document(orderId)
        firestore.runTransaction { transaction ->
            val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: return@runTransaction
            val newCompleted = (order.completedStops + stopId).distinct()
            transaction.update(docRef, "completedStops", newCompleted)

            val allPickupsDone = order.courierRouteStops.filter { it.stopType != RouteStopType.DROPOFF_CLIENT }.all { newCompleted.contains(it.stopId) }
            if (allPickupsDone && order.status != OrderLifecycleStatus.DELIVERED) {
                transaction.update(docRef, "status", OrderLifecycleStatus.PICKED_UP_AT_BIKE)
            }
        }.await()
    }

    override suspend fun startTransitToClient(orderId: String) {
        val docRef = firestore.collection("live_orders").document(orderId)
        firestore.runTransaction { transaction ->
            val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: return@runTransaction
            if (order.status == OrderLifecycleStatus.PICKED_UP_AT_BIKE) {
                transaction.update(docRef, "status", OrderLifecycleStatus.IN_TRANSIT_TO_CLIENT)
            }
        }.await()
    }

    override suspend fun finalizeDeliveryWithPin(orderId: String, enteredPin: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("live_orders").document(orderId)
            var earned = 0.0
            var courierId = ""
            
            firestore.runTransaction { transaction ->
                val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: throw Exception("Pedido no encontrado")
                
                if (order.deliveryPin != enteredPin.trim()) {
                    throw Exception("PIN incorrecto. Intenta nuevamente.")
                }
                
                transaction.update(docRef, "status", OrderLifecycleStatus.DELIVERED)
                
                courierId = order.assignedCourierId ?: ""
                
                val courierLocation = GeoLocation(20.9144, -100.7452) // Default SMA
                val distance = locationTrackerService.calculateDistanceKm(courierLocation, order.clientCoordinates)
                val baseFee = order.cart.mockDeliveryFee 
                earned = baseFee + (distance * 1.15)
            }.await()
            
            if (courierId.isNotBlank()) {
                val courierRef = firestore.collection("users").document(courierId)
                firestore.runTransaction { transaction ->
                    val user = transaction.get(courierRef).toObject(UserProfile::class.java)
                    if (user != null) {
                        transaction.update(courierRef, "dailyEarnings", user.dailyEarnings + earned)
                    }
                }.await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("live_orders").document(orderId)
            firestore.runTransaction { transaction ->
                val order = transaction.get(docRef).toObject(LiveOrder::class.java) ?: throw Exception("Pedido no encontrado")
                if (order.status == OrderLifecycleStatus.IN_TRANSIT_TO_CLIENT || order.status == OrderLifecycleStatus.DELIVERED) {
                    throw Exception("No se puede cancelar una orden en tránsito o entregada")
                }
                transaction.update(docRef, "status", OrderLifecycleStatus.CANCELLED)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getClientActiveOrder(clientId: String): StateFlow<LiveOrder?> {
        return allLiveOrdersFlow.map { list ->
            list.find { it.clientId == clientId && it.status != OrderLifecycleStatus.DELIVERED }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)
    }
}

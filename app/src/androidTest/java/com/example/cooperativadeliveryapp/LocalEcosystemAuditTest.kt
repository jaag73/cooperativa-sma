package com.example.cooperativadeliveryapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cooperativadeliveryapp.data.repository.BusinessMenuRepositoryImpl
import com.example.cooperativadeliveryapp.data.repository.OrderLiveRepositoryImpl
import com.example.cooperativadeliveryapp.domain.location.LocationTrackerService
import com.example.cooperativadeliveryapp.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.delay

@RunWith(AndroidJUnit4::class)
class LocalEcosystemAuditTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var orderLiveRepo: OrderLiveRepositoryImpl
    private lateinit var menuRepo: BusinessMenuRepositoryImpl
    private lateinit var locationTrackerService: LocationTrackerService

    @Before
    fun setup() {
        firestore = FirebaseFirestore.getInstance()
        try {
            firestore.useEmulator("10.0.2.2", 8080)
            firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(com.google.firebase.firestore.MemoryCacheSettings.newBuilder().build())
                .build()
        } catch (_: Exception) {}

        locationTrackerService = object : LocationTrackerService(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext) {
            override suspend fun getCurrentLocation(): GeoLocation {
                return GeoLocation(20.9144, -100.7452)
            }
            override fun calculateDistanceKm(loc1: GeoLocation, loc2: GeoLocation): Double {
                return 5.0
            }
        }
        orderLiveRepo = OrderLiveRepositoryImpl(firestore, locationTrackerService)
        menuRepo = BusinessMenuRepositoryImpl(firestore)
    }

    @Test
    fun testScenario1_CatalogCreationAndCartIntegration() = runBlocking {
        // 1. Crear producto en negocio
        val product = Product(
            id = "test_product_1",
            businessId = "biz_test",
            name = "Test Torta",
            price = 50.0,
            physicalSpecs = ProductPhysicalSpecs(
                prepTimeMinutes = 12,
                volumeUnits = 2,
                weightCategory = ProductWeightCategory.LIGHT,
                thermalRequirement = ThermalRequirement.HOT,
                isFragile = false,
                cargoPlacement = CargoPlacement.THERMAL_BACKPACK
            )
        )

        val result = menuRepo.saveProduct(product)
        assertTrue(result.isSuccess)

        // 2. Visibilidad en cliente
        delay(1000) 
        val menu = menuRepo.getProductsFlow("biz_test").first()
        assertTrue(menu.any { it.name == "Test Torta" })
        
        // 3. Crear orden multinegocio
        val cartItem = CartItem(
            cartItemId = "item_1",
            businessId = "biz_test",
            businessName = "Torta Shop",
            product = product,
            physicalSpecs = product.physicalSpecs,
            quantity = 1
        )
        val cart = MultiBusinessCart(items = listOf(cartItem))
        val profile = UserProfile(uid = "client_test", name = "Test Client")
        
        val liveOrder = orderLiveRepo.createOrderFromCart(cart, profile, PaymentMethod.CASH_ON_DELIVERY)
        assertNotNull(liveOrder.orderId)
        
        // Verificamos calculo de tiempos
        assertEquals(3, liveOrder.recommendedFireDelayMinutes)
        
        // 4. Verificación de ruta del repartidor
        val stops = liveOrder.courierRouteStops
        assertEquals(2, stops.size)
        assertEquals(RouteStopType.PICKUP_HOT_FOOD, stops[0].stopType)
        assertEquals(RouteStopType.DROPOFF_CLIENT, stops[1].stopType)
    }

    @Test
    fun testScenario2_CapacityLimits_ExceedingBackpack() {
        val product = Product(
            physicalSpecs = ProductPhysicalSpecs(
                volumeUnits = 3, 
                cargoPlacement = CargoPlacement.THERMAL_BACKPACK
            )
        )
        val cartItem = CartItem(
            product = product,
            physicalSpecs = product.physicalSpecs,
            quantity = 3 // 3 x 3 = 9 unidades. Maximo es 8
        )
        val cart = MultiBusinessCart(items = listOf(cartItem), maxBackpackUnits = 8)
        
        assertTrue(cart.isCapacityExceeded)
    }

    @Test
    fun testScenario3_ConcurrencyOnAcceptOrder() = runBlocking {
        // Create order
        val cart = MultiBusinessCart(items = listOf())
        val profile = UserProfile(uid = "client_test", name = "Test Client")
        val order = orderLiveRepo.createOrderFromCart(cart, profile, PaymentMethod.CASH_ON_DELIVERY)
        val orderId = order.orderId

        // PENDING_ACCEPTANCE -> PREPARING -> READY
        orderLiveRepo.acceptBusinessOrder(orderId, "Torta Shop")
        orderLiveRepo.markBusinessOrderReady(orderId, "Torta Shop")
        delay(500)

        // Simulate two couriers accepting at the same time
        orderLiveRepo.assignCourierToOrder(orderId, "courier_1")
        
        var exceptionThrown = false
        try {
            orderLiveRepo.assignCourierToOrder(orderId, "courier_2")
        } catch (e: Exception) {
            exceptionThrown = true
        }
        
        assertTrue("El segundo repartidor debió recibir un error de concurrencia", exceptionThrown)
    }

    @Test
    fun testScenario4_Cancelations() = runBlocking {
        val cart = MultiBusinessCart(items = listOf())
        val profile = UserProfile(uid = "client_test", name = "Test Client")
        val order = orderLiveRepo.createOrderFromCart(cart, profile, PaymentMethod.CASH_ON_DELIVERY)
        
        val cancelResult = orderLiveRepo.cancelOrder(order.orderId)
        assertTrue(cancelResult.isSuccess)

        delay(500)
        val updatedOrder = orderLiveRepo.allLiveOrdersFlow.first().find { it.orderId == order.orderId }!!
        assertEquals(OrderLifecycleStatus.CANCELLED, updatedOrder.status)
    }

    @Test
    fun testScenario5_FinancialClosure() = runBlocking {
        val cartItem = CartItem(
            cartItemId = "item_1",
            businessId = "biz_test",
            businessName = "Torta Shop",
            product = Product(price = 100.0),
            quantity = 1
        )
        val cart = MultiBusinessCart(items = listOf(cartItem), mockDeliveryFee = 45.0)
        val profile = UserProfile(uid = "client_test", name = "Test Client")
        val order = orderLiveRepo.createOrderFromCart(cart, profile, PaymentMethod.CASH_ON_DELIVERY)
        
        orderLiveRepo.assignCourierToOrder(order.orderId, "courier_test")
        
        val result = orderLiveRepo.finalizeDeliveryWithPin(order.orderId, order.deliveryPin)
        assertTrue(result.isSuccess)
    }
}

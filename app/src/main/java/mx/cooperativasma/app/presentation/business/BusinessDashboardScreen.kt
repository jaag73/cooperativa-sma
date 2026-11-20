package mx.cooperativasma.app.presentation.business

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import mx.cooperativasma.app.ui.components.PrimaryActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.cooperativasma.app.domain.model.Order
import mx.cooperativasma.app.domain.model.OrderLifecycleStatus
import mx.cooperativasma.app.domain.model.PaymentMethod
import mx.cooperativasma.app.domain.model.Product
import mx.cooperativasma.app.ui.components.LogisticsBadge
import mx.cooperativasma.app.ui.components.PrimaryActionButton
import mx.cooperativasma.app.ui.theme.AgaveGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDashboardScreen(
    viewModel: BusinessDashboardViewModel = hiltViewModel(),
    onNavigateToEditProduct: (String?) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRoleMenu by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(BusinessUiEvent.OnStopAlert)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        val bizName = if (uiState is BusinessUiState.Success) (uiState as BusinessUiState.Success).userProfile.name else "Mi Comercio Local"
                        Text(bizName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Surface(
                            color = AgaveGreenLight.copy(alpha = 0.15f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "0% Comisión • Cooperativa SMA",
                                color = AgaveGreenLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    var isStoreOpen by remember { mutableStateOf(true) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (isStoreOpen) "Abierto" else "Cerrado", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isStoreOpen,
                            onCheckedChange = { isStoreOpen = it },
                            modifier = Modifier.scale(0.8f)
                        )
                        
                        Box {
                            TextButton(onClick = { showRoleMenu = true }) {
                                Text("Modo: Negocio ▼")
                            }
                            DropdownMenu(
                                expanded = showRoleMenu,
                                onDismissRequest = { showRoleMenu = false }
                            ) {
                                (uiState as? BusinessUiState.Success)?.userProfile?.roles?.forEach { role ->
                                    val roleLabel = when(role) {
                                        "CLIENT" -> "Cliente"
                                        "BUSINESS" -> "Negocio"
                                        "COURIER" -> "Repartidor"
                                        else -> role
                                    }
                                    DropdownMenuItem(
                                        text = { Text(roleLabel) },
                                        onClick = {
                                            showRoleMenu = false
                                            if (role != "BUSINESS") {
                                                // Handle role switch (in a real app call viewModel to update and navigate)
                                                onLogout() 
                                            }
                                        }
                                    )
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("+ Activar otro rol", color = MaterialTheme.colorScheme.primary) },
                                    onClick = { showRoleMenu = false }
                                )
                            }
                        }

                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Cerrar sesión")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState is BusinessUiState.Success) {
                val state = uiState as BusinessUiState.Success
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.ReceiptLong, contentDescription = null) },
                        label = { Text("Pedidos") },
                        selected = state.currentTab == DashboardTab.LIVE_ORDERS,
                        onClick = { viewModel.onEvent(BusinessUiEvent.OnTabSelected(DashboardTab.LIVE_ORDERS)) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.RestaurantMenu, contentDescription = null) },
                        label = { Text("Menú") },
                        selected = state.currentTab == DashboardTab.MENU,
                        onClick = { viewModel.onEvent(BusinessUiEvent.OnTabSelected(DashboardTab.MENU)) }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null) },
                        label = { Text("Finanzas") },
                        selected = state.currentTab == DashboardTab.FINANCES,
                        onClick = { viewModel.onEvent(BusinessUiEvent.OnTabSelected(DashboardTab.FINANCES)) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState is BusinessUiState.Success && (uiState as BusinessUiState.Success).currentTab == DashboardTab.MENU) {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToEditProduct(null) },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("Nuevo Producto") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (val state = uiState) {
                is BusinessUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BusinessUiState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is BusinessUiState.Success -> {
                    when (state.currentTab) {
                        DashboardTab.LIVE_ORDERS -> LiveOrdersTab(state, viewModel)
                        DashboardTab.MENU -> MenuTab(state, viewModel, onNavigateToEditProduct)
                        DashboardTab.FINANCES -> FinancesTab(state)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveOrdersTab(state: BusinessUiState.Success, viewModel: BusinessDashboardViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        
        // Custom Filter names mapping
        val filterNames = mapOf(
            OrderFilter.NEW to "Nuevos",
            OrderFilter.PREPARING to "Preparando",
            OrderFilter.READY to "Listos",
            OrderFilter.HISTORY to "Historial"
        )
        
        PrimaryScrollableTabRow(
            selectedTabIndex = state.currentOrderFilter.ordinal,
            edgePadding = 8.dp
        ) {
            OrderFilter.entries.forEachIndexed { index, filter ->
                Tab(
                    selected = state.currentOrderFilter.ordinal == index,
                    onClick = { viewModel.onEvent(BusinessUiEvent.OnOrderFilterSelected(filter)) },
                    text = { Text(filterNames[filter] ?: filter.name) }
                )
            }
        }
        
        val filteredOrders = state.orders.filter { order ->
            when (state.currentOrderFilter) {
                OrderFilter.NEW -> order.status == OrderLifecycleStatus.PENDING_ACCEPTANCE
                OrderFilter.PREPARING -> order.status == OrderLifecycleStatus.PREPARING_AT_BUSINESS
                OrderFilter.READY -> order.status == OrderLifecycleStatus.READY_FOR_PICKUP
                OrderFilter.HISTORY -> order.status == OrderLifecycleStatus.IN_TRANSIT_TO_CLIENT || order.status == OrderLifecycleStatus.DELIVERED
            }
        }

        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Storefront, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Todo tranquilo en cocina", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Los pedidos de tus vecinos en San Miguel de Allende aparecerán aquí al instante.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.onEvent(BusinessUiEvent.OnTabSelected(DashboardTab.MENU)) }) {
                            Text("Gestionar Catálogo")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredOrders, key = { it.orderId }) { order ->
                    OrderCard(order = order, viewModel = viewModel, bizName = state.userProfile.name)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: mx.cooperativasma.app.domain.model.LiveOrder, viewModel: BusinessDashboardViewModel, bizName: String) {
    val isNew = order.status == OrderLifecycleStatus.PENDING_ACCEPTANCE
    val isPreparing = order.status == OrderLifecycleStatus.PREPARING_AT_BUSINESS
    
    val borderColor = when {
        isNew -> MaterialTheme.colorScheme.error
        isPreparing -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(if (isNew || isPreparing) 2.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Orden #${order.orderId.takeLast(4).uppercase()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                    Text(text = order.paymentMethod.name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filtramos solo los items que pertenecen a este negocio
            val bizItems = order.cart.items.filter { it.businessName == bizName }
            
            bizItems.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${item.quantity}x ${item.product.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                if (item.selectedOptions.isNotEmpty()) {
                    Text(item.selectedOptions.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp))
                }
                if (!item.specialNotes.isNullOrBlank()) {
                    Text(text = "Nota: ${item.specialNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 16.dp, top = 2.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                LogisticsBadge(specs = item.physicalSpecs, modifier = Modifier.padding(start = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            val bizTotal = bizItems.sumOf { it.totalPrice }
            Text(text = "Total Comercio: $${bizTotal}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            if (isNew) {
                val prepTime = order.calculatedPrepTimeMinutes
                val arrivalTime = order.courierArrivalEtaMinutes
                val fireDelay = order.recommendedFireDelayMinutes
                
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⏱️ Preparación estimada: $prepTime min (según menú)", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("🛵 Llegada de repartidor: $arrivalTime min (aprox.)", style = MaterialTheme.typography.bodySmall)
                        if (fireDelay > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("💡 Sugerencia: Inicia cocción en $fireDelay min para que se entregue recién hecho.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrimaryActionButton(
                        text = "Aceptar Pedido",
                        onClick = { viewModel.onEvent(BusinessUiEvent.OnAcceptOrder(order.orderId, prepTime)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { viewModel.onEvent(BusinessUiEvent.OnRejectOrder(order.orderId)) },
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Rechazar")
                    }
                }
            } else if (isPreparing) {
                PrimaryActionButton(
                    text = "Marcar Listo para Recolección",
                    onClick = { viewModel.onEvent(BusinessUiEvent.OnMarkOrderReady(order.orderId)) },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (order.status == OrderLifecycleStatus.READY_FOR_PICKUP) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                    Text("📦 Listo en mostrador — Esperando al repartidor", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            } else {
                Text(text = "Estado actual: ${order.status.name}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuTab(state: BusinessUiState.Success, viewModel: BusinessDashboardViewModel, onNavigateToEditProduct: (String) -> Unit) {
    val subcategories = listOf("Todos") + state.menu.map { it.subcategory }.distinct().sorted()
    
    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryScrollableTabRow(
            selectedTabIndex = subcategories.indexOf(state.currentMenuSubcategory).coerceAtLeast(0),
            edgePadding = 8.dp
        ) {
            subcategories.forEach { subcategory ->
                Tab(
                    selected = state.currentMenuSubcategory == subcategory,
                    onClick = { viewModel.onEvent(BusinessUiEvent.OnMenuSubcategorySelected(subcategory)) },
                    text = { Text(subcategory) }
                )
            }
        }
        
        val filteredMenu = if (state.currentMenuSubcategory == "Todos") {
            state.menu
        } else {
            state.menu.filter { it.subcategory == state.currentMenuSubcategory }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredMenu, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToEditProduct(product.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$${product.price}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            if (product.subcategory.isNotBlank() && product.subcategory != "Todos") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = product.subcategory, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LogisticsBadge(specs = product.physicalSpecs)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Switch(
                                checked = product.isAvailable,
                                onCheckedChange = { isAvailable -> 
                                    viewModel.onEvent(BusinessUiEvent.OnToggleProductAvailability(product.id, isAvailable))
                                }
                            )
                            Text(if (product.isAvailable) "Disponible" else "Agotado", style = MaterialTheme.typography.labelSmall, color = if (product.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancesTab(state: BusinessUiState.Success) {
    val deliveredOrders = state.orders.filter { it.status == OrderLifecycleStatus.DELIVERED }
    
    val totalSales = deliveredOrders.sumOf { it.cart.subtotal }
    val cashSales = deliveredOrders.filter { it.paymentMethod == PaymentMethod.CASH_ON_DELIVERY }.sumOf { it.cart.subtotal }
    val speiSales = deliveredOrders.filter { it.paymentMethod == PaymentMethod.DIRECT_TRANSFER_SPEI }.sumOf { it.cart.subtotal }
    val cardSales = deliveredOrders.filter { it.paymentMethod == PaymentMethod.CREDIT_DEBIT_CARD }.sumOf { it.cart.subtotal }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ventas del Día (Sin Retenciones)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("$$totalSales MXN", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Efectivo:", style = MaterialTheme.typography.bodyMedium)
                    Text("$$cashSales MXN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Transferencia SPEI:", style = MaterialTheme.typography.bodyMedium)
                    Text("$$speiSales MXN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tarjeta:", style = MaterialTheme.typography.bodyMedium)
                    Text("$$cardSales MXN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        state.userProfile.trialInfo?.let { trial ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Membresía Cooperativa (Mes de Prueba)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Inicio: ${trial.trialStartDate}")
                    Text("Fin: ${trial.trialEndDate}")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (trial.isTrialActive) {
                        Text("¡Disfrutando beneficios gratuitos!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

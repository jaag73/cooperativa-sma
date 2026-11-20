package mx.cooperativasma.app.presentation.courier

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.cooperativasma.app.domain.model.CourierOrderOpportunity
import mx.cooperativasma.app.domain.model.CourierVehicleSpecs
import mx.cooperativasma.app.domain.model.HotspotDemandInfo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.TwoWheeler
import mx.cooperativasma.app.ui.theme.AgaveGreenLight
import mx.cooperativasma.app.ui.theme.LogisticsHot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierDashboardScreen(
    viewModel: CourierDashboardViewModel = hiltViewModel(),
    onNavigateToRoutePreview: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRoleMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text("Logística Cooperativa") },
                actions = {
                    var expandedBases by remember { mutableStateOf(false) }
                    
                    Box {
                        TextButton(onClick = { showRoleMenu = true }) {
                            Text("Modo: Repartidor ▼")
                        }
                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            uiState.currentUserProfile?.roles?.forEach { role ->
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
                                        if (role != "COURIER") {
                                            viewModel.switchRole(role)
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

                    Box {
                        TextButton(onClick = { expandedBases = true }) {
                            Text("📍 ${uiState.activeBase?.label ?: "Base"}")
                            Text("▼")
                        }
                        DropdownMenu(
                            expanded = expandedBases,
                            onDismissRequest = { expandedBases = false }
                        ) {
                            uiState.restBases.forEach { base ->
                                DropdownMenuItem(
                                    text = { Text("${base.label} (${base.neighborhood})") },
                                    onClick = {
                                        viewModel.switchActiveBase(base.id)
                                        expandedBases = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.TwoWheeler, contentDescription = null) },
                    label = { Text("Radar") },
                    selected = uiState.currentTab == CourierTab.LIVE_RADAR,
                    onClick = { viewModel.selectTab(CourierTab.LIVE_RADAR) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.AccessTime, contentDescription = null) },
                    label = { Text("Programados") },
                    selected = uiState.currentTab == CourierTab.SCHEDULED,
                    onClick = { viewModel.selectTab(CourierTab.SCHEDULED) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Map, contentDescription = null) },
                    label = { Text("Zonas") },
                    selected = uiState.currentTab == CourierTab.ZONES_FIFO,
                    onClick = { viewModel.selectTab(CourierTab.ZONES_FIFO) }
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            // Cabecera Operativa (Progreso Dignidad y Semáforo)
            Card(
                colors = CardDefaults.cardColors(containerColor = AgaveGreenLight.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Meta Diaria: Cooperativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AgaveGreenLight)
                        Icon(Icons.Rounded.Navigation, contentDescription = null, tint = AgaveGreenLight)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Hoy: $${uiState.todayNetEarnings} / $${"%.2f".format(CourierVehicleSpecs.DAILY_NET_GOAL)} MXN",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { (uiState.todayNetEarnings / CourierVehicleSpecs.DAILY_NET_GOAL).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                        color = AgaveGreenLight
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small) {
                        Text("🚦 Demanda actual: HORA VALLE", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(8.dp, 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }

            when (uiState.currentTab) {
                CourierTab.LIVE_RADAR -> RadarTab(uiState.availableOrders, uiState.activeBase?.neighborhood, onNavigateToRoutePreview)
                CourierTab.SCHEDULED -> ScheduledTab(uiState.scheduledOrders)
                CourierTab.ZONES_FIFO -> HotspotsTab(uiState.hotspots)
            }
        }
    }
}

@Composable
fun RadarTab(orders: List<CourierOrderOpportunity>, activeBaseNeighborhood: String?, onClick: (String) -> Unit) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.TwoWheeler, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Buscando viajes...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Mantente atento a tu zona base.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(orders, key = { it.orderId }) { order ->
            var expanded by remember { mutableStateOf(false) }
            val isNearBase = order.clientNeighborhood == activeBaseNeighborhood

            Card(modifier = Modifier.fillMaxWidth().clickable { onClick(order.orderId) }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(order.participatingBusinesses.joinToString(", "), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("$${order.finances.netEarnings} Libres", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("De: ${order.originNeighborhood} ➡️ Para: ${order.clientNeighborhood}", style = MaterialTheme.typography.bodyMedium)

                    if (isNearBase) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                            Text("🏠 Termina cerca de tu base", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { expanded = !expanded }, modifier = Modifier.padding(0.dp)) {
                        Text(if (expanded) "Ocultar desglose financiero" else "Ver desglose transparente")
                    }

                    AnimatedVisibility(visible = expanded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tarifa Bruta: $${order.finances.grossFee}", style = MaterialTheme.typography.bodySmall)
                            if (order.finances.multiBusinessBonus > 0) {
                                Text("+ Bono Multinegocio: $${order.finances.multiBusinessBonus}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("- Gasolina estimada ($0.82/km): -$${order.finances.estimatedFuelCost}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            Text("- Reserva Mantenimiento ($0.33/km): -$${order.finances.maintenanceReserve}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            Text("+ Compensación Retorno a Base: $${order.finances.returnTravelCost}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Ganancia Neta (Bolsillo): $${order.finances.netEarnings}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun ScheduledTab(orders: List<CourierOrderOpportunity>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(orders, key = { it.orderId }) { order ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⏰ ${order.scheduledDateTime}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("De: ${order.originNeighborhood} ➡️ Para: ${order.clientNeighborhood}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Ganancia Libre: $${order.finances.netEarnings}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun HotspotsTab(hotspots: List<HotspotDemandInfo>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(hotspots, key = { it.zoneName }) { hotspot ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📍 ${hotspot.zoneName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pedidos Listos: ${hotspot.pendingOrdersCount}", style = MaterialTheme.typography.bodyMedium)
                        Text("Repartidores fila: ${hotspot.couriersWaitingCount}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (hotspot.userQueuePosition != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                            Text("Eres el Turno #${hotspot.userQueuePosition} en fila", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

package com.example.cooperativadeliveryapp.presentation.courier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cooperativadeliveryapp.domain.model.RouteStopType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierRoutePreviewScreen(
    viewModel: CourierRoutePreviewViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onOrderAccepted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSuggestDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAccepted) {
        if (uiState.isAccepted) {
            onOrderAccepted()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Previsualización de Ruta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("⬅️")
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.order != null && !uiState.isLoading) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { viewModel.acceptOrder() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aceptar y Comenzar Ruta")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showSuggestDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sugerir a un Colega (A contramano)")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(uiState.error!!, color = MaterialTheme.colorScheme.error) }
        } else if (uiState.order != null) {
            val order = uiState.order!!
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

                // Cabecera Financiera
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ganancia Neta", style = MaterialTheme.typography.titleMedium)
                        Text("$${order.finances.netEarnings} Libres", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Compensación de Retorno (Base): +$${order.finances.returnTravelCost}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (order.hasDelicateItems) {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                        Text("⚠️ Carga Frágil: Uso de mochila térmica es obligatorio.", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tramos y Tiempos
                Text("Tiempos Estimados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                order.segments.forEach { segment ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(segment.title, style = MaterialTheme.typography.bodyMedium)
                        Text("${segment.distanceKm} km (~${segment.durationMinutes} min)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Paradas Secuenciadas
                Text("Secuencia de Paradas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                order.stops.sortedBy { it.stopOrder }.forEach { stop ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            val icon = when (stop.stopType) {
                                RouteStopType.PICKUP_DRY_GOODS -> "📦"
                                RouteStopType.PICKUP_HOT_FOOD -> "♨️"
                                RouteStopType.DROPOFF_CLIENT -> "📍"
                            }
                            Text(icon, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("${stop.stopOrder}. ${stop.locationName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(stop.addressSummary, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Llevar en: ${stop.cargoPlacementNote}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                if (stop.itemsSummary.isNotEmpty()) {
                                    Text(stop.itemsSummary.joinToString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuggestDialog) {
        AlertDialog(
            onDismissRequest = { showSuggestDialog = false },
            title = { Text("Ceder a un colega") },
            text = { Text("Se notificará a los repartidores que se encuentren en la zona de origen del pedido.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.suggestToPeer("Colega Cercano")
                        showSuggestDialog = false
                    }
                ) { Text("Sugerir") }
            },
            dismissButton = {
                TextButton(onClick = { showSuggestDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

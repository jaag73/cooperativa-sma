package mx.cooperativasma.app.presentation.courier

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.cooperativasma.app.ui.components.PrimaryActionButton
import mx.cooperativasma.app.domain.model.RouteStopType
import mx.cooperativasma.app.domain.model.OrderLifecycleStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourierActiveDeliveryScreen(
    viewModel: CourierActiveDeliveryViewModel = hiltViewModel(),
    onDeliveryFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var enteredPin by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isDeliveryFinished) {
        if (uiState.isDeliveryFinished) {
            onDeliveryFinished()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ejecución de Ruta en Vivo") })
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("El pedido ya no está activo.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onDeliveryFinished) {
                        Text("Volver al Radar")
                    }
                }
            }
        } else {
            val order = uiState.order!!
            val currentStop = order.courierRouteStops.firstOrNull { !order.completedStops.contains(it.stopId) }

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentStop == null) {
                    Text("Calculando siguiente parada...", style = MaterialTheme.typography.bodyMedium)
                } else if (uiState.order == null && !uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("El pedido ya no está activo.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDeliveryFinished) {
                    Text("Volver al Radar")
                }
            }
        }
    } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val icon = when (currentStop.stopType) {
                                RouteStopType.PICKUP_DRY_GOODS -> "📦"
                                RouteStopType.PICKUP_HOT_FOOD -> "♨️"
                                RouteStopType.DROPOFF_CLIENT -> "📍"
                            }
                            Text("$icon Parada ${currentStop.stopOrder}: ${currentStop.locationName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(currentStop.addressSummary, style = MaterialTheme.typography.bodyLarge)

                            Spacer(modifier = Modifier.height(16.dp))
                            PrimaryActionButton(
                                text = "Abrir GPS (Maps/Waze)",
                                onClick = {
                                    val uri = Uri.parse("google.navigation:q=${currentStop.locationName}, ${currentStop.addressSummary}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                            if (currentStop.stopType != RouteStopType.DROPOFF_CLIENT) {
                                Text("Recolección:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Acomodo: ${currentStop.cargoPlacementNote}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(8.dp))
                                currentStop.itemsSummary.forEach { item ->
                                    Text("• $item", style = MaterialTheme.typography.bodyMedium)
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                val isBusinessReady = order.readyBusinesses.contains(currentStop.locationName)
                                if (!isBusinessReady) {
                                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.small) {
                                        Text("⚠️ El negocio aún sigue preparando el pedido.", modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                } else if (order.status != OrderLifecycleStatus.PICKED_UP_AT_BIKE) {
                                    PrimaryActionButton(
                                        text = "Confirmar Paquete Recolectado",
                                        onClick = { viewModel.completeStop(currentStop.stopId) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("🏍️ Carga recolectada — Asegura tu estiba en la moto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("Tómate un momento para acomodar la mochila/parrilla y ponerte casco y guantes antes de arrancar.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    PrimaryActionButton(
                                        text = "Iniciar Viaje hacia el Cliente",
                                        onClick = { viewModel.startTransitToClient() },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            } else if (uiState.order == null && !uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("El pedido ya no está activo.")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDeliveryFinished) {
                    Text("Volver al Radar")
                }
            }
        }
    } else {
                                Text("Entrega a Cliente:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "Método de Pago: ${order.paymentMethod.name}\nTotal: $${order.cart.total} MXN",
                                        modifier = Modifier.padding(16.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = { enteredPin = it },
                                    label = { Text("PIN de Entrega (dictado por cliente)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                PrimaryActionButton(
                                    text = "Validar PIN y Finalizar Entrega",
                                    onClick = { viewModel.finalizeDelivery(enteredPin) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = enteredPin.length == 4
                                )

                                if (uiState.error != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

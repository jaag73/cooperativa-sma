package com.example.cooperativadeliveryapp.presentation.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cooperativadeliveryapp.domain.model.CartItem
import com.example.cooperativadeliveryapp.domain.model.PaymentMethod
import com.example.cooperativadeliveryapp.ui.components.CapacityGauge
import com.example.cooperativadeliveryapp.ui.components.PrimaryActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutScreen(
    viewModel: CartCheckoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val cart by viewModel.cartFlow.collectAsState()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Pedido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Text("⬅️") }
                }
            )
        },
        bottomBar = {
            if (cart.items.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text("$${cart.subtotal} MXN", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tarifa Base (Multinegocio)", style = MaterialTheme.typography.bodyMedium)
                            Text("$${cart.mockDeliveryFee} MXN", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("$${cart.total} MXN", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.submitOrder(onOrderSuccess) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !cart.isCapacityExceeded && !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text("Confirmar Pedido Multinegocio")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (cart.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tu carrito está vacío", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Medidor de Capacidad
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Capacidad de la Motocicleta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            CapacityGauge(
                                label = "Mochila Térmica",
                                current = cart.totalBackpackUnits,
                                max = cart.maxBackpackUnits
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            CapacityGauge(
                                label = "Parrilla Trasera",
                                current = cart.totalRackUnits,
                                max = cart.maxRackUnits
                            )

                            if (cart.totalDelicateItems > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = "⚠️ Contiene artículos frágiles. Requerirán cuidado extra.",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            if (cart.isCapacityExceeded) {
                                Spacer(modifier = Modifier.height(16.dp))
                                if (cart.hasFragileOverload) {
                                    Text("⚠️ Por seguridad del producto (pasteles, huevos o vidrio), este pedido excede el límite de carga frágil por viaje.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("⚠️ Límite excedido. Reduce la cantidad para poder transportar tu pedido de forma segura.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (cart.hasHotFood) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡", style = MaterialTheme.typography.headlineSmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Logística Inteligente: Los artículos calientes se recogerán al final de la ruta para que lleguen frescos y calientes a tu domicilio.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                    }
                }

                // Agrupado por negocio
                val itemsByBusiness = cart.items.groupBy { it.businessName }
                itemsByBusiness.forEach { (bizName, items) ->
                    item {
                        Text(bizName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(items, key = { it.cartItemId }) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChange = { newQ -> viewModel.updateQuantity(item.cartItemId, newQ) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Método de Pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val methods = listOf(
                        PaymentMethod.CASH_ON_DELIVERY to "Efectivo al recibir",
                        PaymentMethod.DIRECT_TRANSFER_SPEI to "Transferencia SPEI",
                        PaymentMethod.CREDIT_DEBIT_CARD to "Tarjeta de Crédito/Débito"
                    )

                    methods.forEach { (method, name) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.selectPaymentMethod(method) }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == method,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onQuantityChange: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    if (item.physicalSpecs.fragility == com.example.cooperativadeliveryapp.domain.model.FragilityLevel.DELICATE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("⚠️ Frágil (Mochila obligatoria)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
                if (item.selectedOptions.isNotEmpty()) {
                    Text(item.selectedOptions.joinToString { it.name }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!item.specialNotes.isNullOrBlank()) {
                    Text("Nota: ${item.specialNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                Text("$${item.totalPrice}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(item.quantity - 1) }) { Text("➖") }
                Text("${item.quantity}")
                IconButton(onClick = { onQuantityChange(item.quantity + 1) }) { Text("➕") }
            }
        }
    }
}

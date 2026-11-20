package mx.cooperativasma.app.presentation.client

import androidx.compose.foundation.clickable
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.cooperativasma.app.domain.model.Product
import mx.cooperativasma.app.domain.model.ProductOptionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    viewModel: BusinessDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val cart by viewModel.cartFlow.collectAsState()

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.business?.name ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("⬅️")
                    }
                }
            )
        },
        bottomBar = {
            if (cart.items.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.clickable { onNavigateToCart() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛒", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${cart.items.size} artículos", fontWeight = FontWeight.Bold)
                        }
                        Text("$${cart.subtotal} MXN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

                PrimaryScrollableTabRow(
                    selectedTabIndex = uiState.subcategories.indexOf(uiState.currentSubcategory).coerceAtLeast(0),
                    edgePadding = 16.dp
                ) {
                    uiState.subcategories.forEach { subcat ->
                        Tab(
                            selected = uiState.currentSubcategory == subcat,
                            onClick = { viewModel.setSubcategory(subcat) },
                            text = { Text(subcat) }
                        )
                    }
                }

                val filteredMenu = if (uiState.currentSubcategory == "Todos") {
                    uiState.menu
                } else {
                    uiState.menu.filter { it.subcategory == uiState.currentSubcategory }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredMenu, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedProduct = product },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(product.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$${product.price} MXN", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedProduct != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedProduct = null },
            sheetState = sheetState
        ) {
            ProductConfiguratorSheet(
                product = selectedProduct!!,
                onAddToCart = { quantity, options, notes ->
                    viewModel.addToCart(selectedProduct!!, quantity, options, notes)
                    selectedProduct = null
                },
                onCancel = { selectedProduct = null }
            )
        }
    }
}

@Composable
fun ProductConfiguratorSheet(
    product: Product,
    onAddToCart: (quantity: Int, selectedOptions: List<ProductOptionItem>, specialNotes: String?) -> Unit,
    onCancel: () -> Unit
) {
    var quantity by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    // Simplificación temporal para selección múltiple
    val selectedOptions = remember { mutableStateListOf<ProductOptionItem>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(product.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))

        product.customizations.forEach { group ->
            Text(group.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (group.isRequired) {
                Text("Obligatorio (elige ${group.minSelections})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
            group.options.forEach { option ->
                val isSelected = selectedOptions.contains(option)
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (isSelected) selectedOptions.remove(option) else selectedOptions.add(option)
                    }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(option.name)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (option.extraPrice > 0) {
                            Text("+$${option.extraPrice} ", style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(checked = isSelected, onCheckedChange = null)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Instrucciones especiales") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { if (quantity > 1) quantity-- }) { Text("➖") }
            Text("$quantity", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
            IconButton(onClick = { quantity++ }) { Text("➕") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val unitPrice = product.price + selectedOptions.sumOf { it.extraPrice }
        val total = unitPrice * quantity

        Button(
            onClick = { onAddToCart(quantity, selectedOptions.toList(), notes.ifBlank { null }) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar al Pedido ($${total} MXN)")
        }
        Spacer(modifier = Modifier.height(32.dp)) // Padding for bottom nav / gesture bar
    }
}

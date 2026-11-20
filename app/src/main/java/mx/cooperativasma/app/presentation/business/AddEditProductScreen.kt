package mx.cooperativasma.app.presentation.business

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.cooperativasma.app.domain.model.Product
import mx.cooperativasma.app.domain.model.ProductCustomizationGroup
import mx.cooperativasma.app.domain.model.ProductOptionItem
import mx.cooperativasma.app.domain.model.ThermalRequirement
import mx.cooperativasma.app.domain.model.OptionType
import mx.cooperativasma.app.domain.model.ProductVolumeSize
import mx.cooperativasma.app.domain.model.ProductWeightCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    viewModel: AddEditProductViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val initialProduct by viewModel.product.collectAsState()
    val saveComplete by viewModel.saveComplete.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    LaunchedEffect(saveComplete) {
        if (saveComplete) {
            onNavigateBack()
        }
    }

    var name by remember(initialProduct) { mutableStateOf(initialProduct?.name ?: "") }
    var description by remember(initialProduct) { mutableStateOf(initialProduct?.description ?: "") }
    var price by remember(initialProduct) { mutableStateOf(initialProduct?.price?.takeIf { it > 0.0 }?.toString() ?: "") }
    
    // Category Dropdown State
    var category by remember(initialProduct) { mutableStateOf(initialProduct?.category ?: "Antojitos y Tacos") }
    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("Tortas", "Tacos y Antojitos", "Comida Corrida", "Pizzas y Hamburguesas", "Mariscos", "Vegetariana / Vegana", "Bebidas", "Postres", "Otros")
    
    // Image Picker State
    var imageUrl by remember(initialProduct) { mutableStateOf(initialProduct?.imageUrl ?: "") }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                imageUrl = uri.toString()
            }
        }
    )

    var subcategory by remember(initialProduct) { mutableStateOf(initialProduct?.subcategory ?: "General") }
    
    var customizations by remember(initialProduct) { 
        mutableStateOf(initialProduct?.customizations ?: emptyList()) 
    }

    // Logistics & Transportation State
    var prepTimeMinutes by remember(initialProduct) { mutableStateOf(initialProduct?.physicalSpecs?.prepTimeMinutes?.toString() ?: "10") }
    var volumeSize by remember(initialProduct) { mutableStateOf(initialProduct?.physicalSpecs?.volumeSize ?: ProductVolumeSize.MEDIUM) }
    var expandedVolume by remember { mutableStateOf(false) }
    var weightCategory by remember(initialProduct) { mutableStateOf(initialProduct?.physicalSpecs?.weightCategory ?: ProductWeightCategory.LIGHT) }
    var expandedWeight by remember { mutableStateOf(false) }
    var thermalReq by remember(initialProduct) { mutableStateOf(initialProduct?.physicalSpecs?.thermalRequirement ?: ThermalRequirement.AMBIENT) }
    var expandedThermal by remember { mutableStateOf(false) }
    var isFragile by remember(initialProduct) { mutableStateOf(initialProduct?.physicalSpecs?.isFragile ?: false) }

    var isAvailable by remember(initialProduct) { mutableStateOf(initialProduct?.isAvailable ?: true) }

    val isFormValid = name.isNotBlank() && price.toDoubleOrNull() != null && !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (initialProduct == null) "Nuevo Producto" else "Editar Producto", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        // Image Picker Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clickable(enabled = !isSaving) {
                    imagePickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (imageUrl.isBlank()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📸", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca para agregar foto del platillo", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Aquí en el futuro va la miniatura AsyncImage
                        Text("📷 Imagen Seleccionada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(imageUrl, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca para cambiar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del Platillo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (Ingredientes)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Precio ($ MXN)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { if (!isSaving) expandedCategory = !expandedCategory }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { },
                readOnly = true,
                label = { Text("Categoría Principal") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryEditable, enabled = true)
                    .fillMaxWidth(),
                enabled = !isSaving
            )
            ExposedDropdownMenu(
                expanded = expandedCategory,
                onDismissRequest = { expandedCategory = false }
            ) {
                categories.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            category = selectionOption
                            expandedCategory = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = subcategory,
            onValueChange = { subcategory = it },
            label = { Text("Subcategoría (Ej: Con carne, Refrescos)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Opciones de Preparación y Variantes", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = {
                val group = ProductCustomizationGroup(
                    id = System.currentTimeMillis().toString(),
                    title = "Opciones estándar",
                    type = OptionType.MULTIPLE_CHOICE,
                    options = listOf(
                        ProductOptionItem(id = "1", name = "Sin cebolla", extraPrice = 0.0),
                        ProductOptionItem(id = "2", name = "Salsa aparte", extraPrice = 0.0)
                    )
                )
                customizations = customizations + group
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("Auto-generar (Verduras/Salsas)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = {
                val group = ProductCustomizationGroup(
                    id = System.currentTimeMillis().toString(),
                    title = "Nuevo Grupo",
                    type = OptionType.MULTIPLE_CHOICE
                )
                customizations = customizations + group
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text("+ Agregar Grupo de Opciones")
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        customizations.forEachIndexed { groupIndex, group ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = group.title,
                        onValueChange = { newTitle ->
                            val updated = customizations.toMutableList()
                            updated[groupIndex] = group.copy(title = newTitle)
                            customizations = updated
                        },
                        label = { Text("Título del grupo (ej. Elige Salsa)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Opciones (${group.options.size})",
                        style = MaterialTheme.typography.labelSmall
                    )
                    group.options.forEachIndexed { optIndex, opt ->
                        Text("- ${opt.name} (+$${opt.extraPrice})", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        val newOpt = ProductOptionItem(
                            id = System.currentTimeMillis().toString(),
                            name = "Nueva opción",
                            extraPrice = 0.0
                        )
                        val updated = customizations.toMutableList()
                        updated[groupIndex] = group.copy(options = group.options + newOpt)
                        customizations = updated
                    }) {
                        Text("+ Agregar Opción")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Disponible para ordenar")
            Switch(
                checked = isAvailable,
                onCheckedChange = { isAvailable = it },
                enabled = !isSaving
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Logística y Transporte del Producto", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = prepTimeMinutes,
            onValueChange = { prepTimeMinutes = it },
            label = { Text("Tiempo de preparación (min)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Volume
        ExposedDropdownMenuBox(
            expanded = expandedVolume,
            onExpandedChange = { if (!isSaving) expandedVolume = !expandedVolume }
        ) {
            OutlinedTextField(
                value = volumeSize.label,
                onValueChange = { },
                readOnly = true,
                label = { Text("Volumen / Tamaño") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVolume) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                enabled = !isSaving
            )
            ExposedDropdownMenu(expanded = expandedVolume, onDismissRequest = { expandedVolume = false }) {
                ProductVolumeSize.entries.forEach { size ->
                    DropdownMenuItem(text = { Text(size.label) }, onClick = { volumeSize = size; expandedVolume = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Weight
        ExposedDropdownMenuBox(
            expanded = expandedWeight,
            onExpandedChange = { if (!isSaving) expandedWeight = !expandedWeight }
        ) {
            OutlinedTextField(
                value = weightCategory.label,
                onValueChange = { },
                readOnly = true,
                label = { Text("Peso y Estiba") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedWeight) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                enabled = !isSaving
            )
            ExposedDropdownMenu(expanded = expandedWeight, onDismissRequest = { expandedWeight = false }) {
                ProductWeightCategory.entries.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.label) }, onClick = { weightCategory = cat; expandedWeight = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Thermal
        ExposedDropdownMenuBox(
            expanded = expandedThermal,
            onExpandedChange = { if (!isSaving) expandedThermal = !expandedThermal }
        ) {
            OutlinedTextField(
                value = thermalReq.label,
                onValueChange = { },
                readOnly = true,
                label = { Text("Temperatura") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedThermal) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth(),
                enabled = !isSaving
            )
            ExposedDropdownMenu(expanded = expandedThermal, onDismissRequest = { expandedThermal = false }) {
                mx.cooperativasma.app.domain.model.ThermalRequirement.entries.forEach { req ->
                    DropdownMenuItem(text = { Text(req.label) }, onClick = { thermalReq = req; expandedThermal = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("🥚 Producto Frágil / Delicado\n(No apilar encima)")
            Switch(checked = isFragile, onCheckedChange = { isFragile = it }, enabled = !isSaving)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
            val travelMode = if (weightCategory.requiresRack && !isFragile) "Parrilla (Ligas)" else "Mochila Térmica"
            val timePref = if (thermalReq == mx.cooperativasma.app.domain.model.ThermalRequirement.HOT || thermalReq == mx.cooperativasma.app.domain.model.ThermalRequirement.COLD) "al final de la ruta" else "al principio"
            Text(
                text = "📦 Viajará en $travelMode, ocupa ${volumeSize.capacityUnits} unid., se recoge $timePref.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        if (isSaving) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val newProduct = Product(
                        id = initialProduct?.id ?: "",
                        businessId = initialProduct?.businessId ?: "", 
                        name = name,
                        description = description,
                        price = price.toDoubleOrNull() ?: 0.0,
                        category = category,
                        subcategory = subcategory,
                        isAvailable = isAvailable,
                        imageUrl = imageUrl.ifBlank { null },
                        customizations = customizations,
                        physicalSpecs = mx.cooperativasma.app.domain.model.ProductPhysicalSpecs(
                            prepTimeMinutes = prepTimeMinutes.toIntOrNull() ?: 10,
                            volumeSize = volumeSize,
                            weightCategory = weightCategory,
                            thermalRequirement = thermalReq,
                            isFragile = isFragile
                        )
                    )
                    viewModel.saveProduct(newProduct)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Guardar Producto")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}

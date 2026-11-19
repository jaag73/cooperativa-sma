package com.example.cooperativadeliveryapp.presentation.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cooperativadeliveryapp.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessRegistrationWizardScreen(
    uid: String,
    uiState: AuthUiState,
    onSubmit: (AuthUiEvent.OnCompleteProfileSubmit) -> Unit
) {
    var currentStep by remember { mutableStateOf(1) }

    // Paso 1: Identidad y Giro
    var businessName by remember { mutableStateOf("") }
    var managerName by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf(DEFAULT_BUSINESS_SECTORS.first()) }
    var customSector by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    var slogan by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }

    var expandedSector by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val currentCategories = SECTOR_DEFAULT_CATEGORIES[sector] ?: emptyList()

    LaunchedEffect(sector) {
        category = currentCategories.firstOrNull() ?: ""
    }

    // Paso 2: Dirección
    var postalCode by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    var isCustomNeighborhood by remember { mutableStateOf(false) }
    var customNeighborhoodProposed by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var exteriorNumber by remember { mutableStateOf("") }
    var interiorNumber by remember { mutableStateOf("") }
    var expandedNeighborhood by remember { mutableStateOf(false) }
    var coordinates by remember { mutableStateOf(GeoLocation()) }

    // Paso 3: Titular
    var ownerIdUrl by remember { mutableStateOf("") }
    var ownerSelfieUrl by remember { mutableStateOf("") }

    val idPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { ownerIdUrl = it.toString() }
    }
    val selfiePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { ownerSelfieUrl = it.toString() }
    }

    // Paso 4: Marca Visual
    var logoUrl by remember { mutableStateOf("") }
    var facadeUrl by remember { mutableStateOf("") }

    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { logoUrl = it.toString() }
    }
    val facadePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { facadeUrl = it.toString() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Paso $currentStep de 4", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        LinearProgressIndicator(progress = currentStep / 4f, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

        when (currentStep) {
            1 -> {
                Text("Identidad y Giro Comercial", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Cuéntanos sobre tu comercio para que la comunidad te identifique fácilmente y podamos clasificar tus productos.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Nombre del negocio") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Text)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    label = { Text("Nombre del encargado") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Text)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedSector,
                    onExpandedChange = { expandedSector = !expandedSector }
                ) {
                    OutlinedTextField(
                        value = sector,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Giro Comercial") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSector) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedSector, onDismissRequest = { expandedSector = false }) {
                        DEFAULT_BUSINESS_SECTORS.forEach { selection ->
                            DropdownMenuItem(text = { Text(selection) }, onClick = { sector = selection; expandedSector = false })
                        }
                    }
                }

                AnimatedVisibility(visible = sector == "Otro (sugerir giro)") {
                    OutlinedTextField(
                        value = customSector,
                        onValueChange = { customSector = it },
                        label = { Text("¿Cuál es tu giro?") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Categoría Principal") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        currentCategories.forEach { selection ->
                            DropdownMenuItem(text = { Text(selection) }, onClick = { category = selection; expandedCategory = false })
                        }
                    }
                }

                AnimatedVisibility(visible = category == "Otra (sugerir)" || sector == "Otro (sugerir giro)") {
                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("¿Cuál es tu categoría?") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = slogan,
                    onValueChange = { if (it.length <= 120) slogan = it },
                    label = { Text("Slogan o breve descripción (máx. 120)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rfc,
                    onValueChange = { rfc = it },
                    label = { Text("RFC (Opcional)") },
                    supportingText = { Text("Beneficios al formalizarte ante el SAT") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("A continuación definiremos la ubicación exacta de entrega y recolección.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            2 -> {
                Text("Dirección Estructurada", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Por ahora nuestro servicio opera exclusivamente en San Miguel de Allende y sus comunidades.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = SmaPostalDirectory.SUPPORTED_COUNTRY, onValueChange = {}, label = { Text("País") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = SmaPostalDirectory.SUPPORTED_STATE, onValueChange = {}, label = { Text("Estado") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = SmaPostalDirectory.SUPPORTED_MUNICIPALITY, onValueChange = {}, label = { Text("Municipio / Ciudad") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { 
                        postalCode = it 
                        if (it.length == 5) {
                            val colonias = SmaPostalDirectory.getColoniasForPostalCode(it)
                            if (colonias.size == 1) {
                                neighborhood = colonias.first()
                                isCustomNeighborhood = false
                            } else if (colonias.isEmpty()) {
                                neighborhood = ""
                            }
                        }
                    },
                    label = { Text("Código Postal") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )
                Text(
                    text = "¿No recuerdas tu Código Postal o no coincide con tu recibo? Escribe el nombre de tu colonia o comunidad en el siguiente campo y lo asignaremos automáticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                val neighborhoodsByCp = SmaPostalDirectory.getColoniasForPostalCode(postalCode).sorted()
                val communityMatches = if (neighborhood.length >= 2 && neighborhoodsByCp.isEmpty() && postalCode.isEmpty()) {
                    SmaPostalDirectory.searchCommunities(neighborhood)
                } else emptyList()

                val showDropdown = neighborhoodsByCp.isNotEmpty() || communityMatches.isNotEmpty()

                if (showDropdown && !isCustomNeighborhood) {
                    ExposedDropdownMenuBox(
                        expanded = expandedNeighborhood,
                        onExpandedChange = { expandedNeighborhood = !expandedNeighborhood }
                    ) {
                        OutlinedTextField(
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            label = { Text("Colonia o Comunidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNeighborhood) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedNeighborhood, onDismissRequest = { expandedNeighborhood = false }) {
                            if (neighborhoodsByCp.isNotEmpty()) {
                                neighborhoodsByCp.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection) }, 
                                        onClick = { 
                                            neighborhood = selection
                                            isCustomNeighborhood = false
                                            expandedNeighborhood = false 
                                        }
                                    )
                                }
                            } else {
                                communityMatches.forEach { match ->
                                    DropdownMenuItem(
                                        text = { Text("${match.neighborhoodOrCommunity} (C.P. ${match.postalCode})") }, 
                                        onClick = { 
                                            neighborhood = match.neighborhoodOrCommunity
                                            postalCode = match.postalCode
                                            isCustomNeighborhood = false
                                            expandedNeighborhood = false 
                                        }
                                    )
                                }
                            }
                            DropdownMenuItem(
                                text = { Text("➕ No encuentro mi localidad (escribir manualmente)", fontWeight = FontWeight.Bold) }, 
                                onClick = { 
                                    isCustomNeighborhood = true
                                    neighborhood = ""
                                    expandedNeighborhood = false 
                                }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { 
                            neighborhood = it 
                            if (neighborhood.isEmpty()) isCustomNeighborhood = false
                        },
                        label = { Text("Colonia o Comunidad") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    if (isCustomNeighborhood) {
                        Text(
                            text = "Tu colonia quedará registrada y sujeta a revisión administrativa sin detener tu registro.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Calle") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = exteriorNumber,
                        onValueChange = { exteriorNumber = it },
                        label = { Text("Núm. Exterior") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        value = interiorNumber,
                        onValueChange = { interiorNumber = it },
                        label = { Text("Núm. Int (Opcional)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗺️", style = MaterialTheme.typography.displayMedium)
                        Text("Lat: ${coordinates.latitude}, Lng: ${coordinates.longitude}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { /* TODO: Get real GPS location */ coordinates = GeoLocation() }) {
                            Text("📍 Fijar mi ubicación GPS actual")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("En el siguiente paso validaremos la identidad del titular para mantener la seguridad comunitaria.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            3 -> {
                Text("Verificación del Titular", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Para proteger a la comunidad y evitar fraudes, requerimos validar la identidad oficial de la persona responsable del negocio.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                PhotoPickerCard(
                    title = "Identificación Oficial (INE o Pasaporte)",
                    imageUrl = ownerIdUrl,
                    onClick = { idPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                PhotoPickerCard(
                    title = "Fotografía del Rostro (Selfie)",
                    imageUrl = ownerSelfieUrl,
                    onClick = { selfiePickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        ownerIdUrl = "mock://dev/ine_dummy.jpg"
                        ownerSelfieUrl = "mock://dev/selfie_dummy.jpg"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⚡ Cargar imágenes de prueba (Modo Dev)")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Por último, agregaremos la imagen visual y fachada de tu local para que sea visible en el catálogo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            4 -> {
                Text("Marca Visual y Fachada", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sube el logotipo de tu marca y una fotografía de la fachada exterior de tu local para facilitar la ubicación física a los repartidores.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))

                PhotoPickerCard(
                    title = "Logotipo del negocio",
                    imageUrl = logoUrl,
                    onClick = { logoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                PhotoPickerCard(
                    title = "Fotografía de la fachada exterior",
                    imageUrl = facadeUrl,
                    onClick = { facadePickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                )

                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        logoUrl = "mock://dev/logo_dummy.jpg"
                        facadeUrl = "mock://dev/facade_dummy.jpg"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("⚡ Cargar imágenes de prueba (Modo Dev)")
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("¡Todo listo! Al registrarte, tu primer mes de prueba gratuito comenzará de inmediato sin comisiones de la plataforma.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (currentStep > 1) {
                OutlinedButton(onClick = { currentStep-- }, enabled = uiState !is AuthUiState.Loading) {
                    Text("Regresar")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp)) // Placeholder
            }

            val isStepValid = when (currentStep) {
                1 -> businessName.isNotBlank() && managerName.isNotBlank()
                2 -> postalCode.isNotBlank() && neighborhood.isNotBlank() && street.isNotBlank() && exteriorNumber.isNotBlank()
                3 -> ownerIdUrl.isNotBlank() && ownerSelfieUrl.isNotBlank()
                4 -> logoUrl.isNotBlank() && facadeUrl.isNotBlank()
                else -> false
            }

            if (currentStep < 4) {
                Button(onClick = { currentStep++ }, enabled = isStepValid && uiState !is AuthUiState.Loading) {
                    Text("Siguiente")
                }
            } else {
                Button(
                    onClick = {
                        val taxonomyInfo = BusinessTaxonomy(
                            sector = sector,
                            category = category,
                            customSectorProposed = customSector.ifBlank { null },
                            customCategoryProposed = customCategory.ifBlank { null },
                            isApprovedByAdmin = true
                        )

                        val addressInfo = StructuredAddress(
                            country = SmaPostalDirectory.SUPPORTED_COUNTRY,
                            state = SmaPostalDirectory.SUPPORTED_STATE,
                            city = SmaPostalDirectory.SUPPORTED_MUNICIPALITY,
                            postalCode = postalCode,
                            neighborhoodOrCommunity = neighborhood,
                            isCustomCommunity = isCustomNeighborhood,
                            customCommunityProposed = customNeighborhoodProposed.ifBlank { null },
                            isApprovedByAdmin = !isCustomNeighborhood,
                            street = street,
                            exteriorNumber = exteriorNumber,
                            interiorNumber = interiorNumber.ifBlank { null },
                            coordinates = coordinates
                        )

                        val identityInfo = BusinessIdentityVerification(
                            ownerIdDocumentUrl = ownerIdUrl,
                            ownerSelfieUrl = ownerSelfieUrl,
                            isVerified = false
                        )

                        val brandingInfo = BusinessBranding(
                            slogan = slogan,
                            logoUrl = logoUrl,
                            facadePhotoUrl = facadeUrl
                        )

                        onSubmit(
                            AuthUiEvent.OnCompleteProfileSubmit(
                                uid = uid,
                                role = UserRole.BUSINESS,
                                name = businessName,
                                communityId = neighborhood, // Fallback original
                                managerName = managerName,
                                address = "$street $exteriorNumber", // Fallback original
                                taxonomy = taxonomyInfo,
                                structuredAddress = addressInfo,
                                identityVerification = identityInfo,
                                branding = brandingInfo,
                                rfc = rfc.ifBlank { null }
                            )
                        )
                    },
                    enabled = isStepValid && uiState !is AuthUiState.Loading
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Registrar Micronegocio")
                    }
                }
            }
        }

        if (uiState is AuthUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun PhotoPickerCard(title: String, imageUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (imageUrl.isBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📸", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            } else if (imageUrl.startsWith("mock://")) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ Imagen de prueba cargada", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca para cambiar", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ $title (Cargada)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(imageUrl, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Toca para cambiar", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

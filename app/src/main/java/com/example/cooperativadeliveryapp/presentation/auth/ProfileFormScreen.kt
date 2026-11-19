package com.example.cooperativadeliveryapp.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cooperativadeliveryapp.domain.model.MotorcycleSpecs
import com.example.cooperativadeliveryapp.domain.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileFormScreen(
    uid: String,
    uiState: AuthUiState,
    onSubmit: (AuthUiEvent.OnCompleteProfileSubmit) -> Unit
) {
    var role by remember { mutableStateOf(UserRole.CLIENT) }

    if (role == UserRole.BUSINESS) {
        BusinessRegistrationWizardScreen(
            uid = uid,
            uiState = uiState,
            onSubmit = onSubmit
        )
        return
    }

    // Campos Comunes (Cliente y Repartidor)
    var name by remember { mutableStateOf("") }
    var communityId by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var clabe by remember { mutableStateOf("") }
    var motoBrand by remember { mutableStateOf("") }
    var motoModel by remember { mutableStateOf("") }
    var motoYear by remember { mutableStateOf("") }
    var motoCc by remember { mutableStateOf("") }
    var motoKml by remember { mutableStateOf("") }
    var motoPlate by remember { mutableStateOf("") }
    
    // Bases de Repartidor
    var primaryBaseNeighborhood by remember { mutableStateOf("") }
    var primaryBasePostalCode by remember { mutableStateOf("") }
    var secondaryBaseNeighborhood by remember { mutableStateOf("") }
    var secondaryBasePostalCode by remember { mutableStateOf("") }
    var expandedPrimaryNeighborhood by remember { mutableStateOf(false) }
    var expandedSecondaryNeighborhood by remember { mutableStateOf(false) }

    // Verificaciones de validación simples
    val isFormValid = when (role) {
        UserRole.CLIENT -> name.isNotBlank()
        UserRole.COURIER -> name.isNotBlank() && motoBrand.isNotBlank() && motoModel.isNotBlank() && primaryBaseNeighborhood.isNotBlank() && primaryBasePostalCode.isNotBlank()
        else -> false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Completar Perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        RoleSelectionScreen(
            selectedRole = role,
            onRoleSelected = { role = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campos según rol
        when (role) {
            UserRole.CLIENT -> {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = communityId,
                    onValueChange = { communityId = it },
                    label = { Text("Comunidad/Colonia (Opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¿Deseas pagar en efectivo?",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Para seguridad de los repartidores, el pago en efectivo requiere verificación de identidad posterior (KYC). Podrás hacerlo más adelante.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            UserRole.COURIER -> {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Datos de la Motocicleta", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = motoBrand, onValueChange = { motoBrand = it }, label = { Text("Marca") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = motoModel, onValueChange = { motoModel = it }, label = { Text("Modelo") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = motoYear, onValueChange = { motoYear = it }, label = { Text("Año") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = motoCc, onValueChange = { motoCc = it }, label = { Text("Cilindrada (cc)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = motoKml, onValueChange = { motoKml = it }, label = { Text("Rendimiento (km/L)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = motoPlate, onValueChange = { motoPlate = it }, label = { Text("Placa") }, modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Bases de Descanso", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Base Principal (Tu Casa)", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = primaryBasePostalCode,
                    onValueChange = { primaryBasePostalCode = it },
                    label = { Text("Código Postal") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                val primaryNeighborhoods = com.example.cooperativadeliveryapp.domain.model.SmaPostalDirectory.getColoniasForPostalCode(primaryBasePostalCode).sorted()
                if (primaryNeighborhoods.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedPrimaryNeighborhood,
                        onExpandedChange = { expandedPrimaryNeighborhood = !expandedPrimaryNeighborhood }
                    ) {
                        OutlinedTextField(
                            value = primaryBaseNeighborhood,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Colonia/Comunidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPrimaryNeighborhood) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedPrimaryNeighborhood, onDismissRequest = { expandedPrimaryNeighborhood = false }) {
                            primaryNeighborhoods.forEach { selection ->
                                DropdownMenuItem(text = { Text(selection) }, onClick = { primaryBaseNeighborhood = selection; expandedPrimaryNeighborhood = false })
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = primaryBaseNeighborhood,
                        onValueChange = { primaryBaseNeighborhood = it },
                        label = { Text("Colonia/Comunidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Base Secundaria (Opcional - Casa de familiar/amigo)", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = secondaryBasePostalCode,
                    onValueChange = { secondaryBasePostalCode = it },
                    label = { Text("Código Postal") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                val secondaryNeighborhoods = com.example.cooperativadeliveryapp.domain.model.SmaPostalDirectory.getColoniasForPostalCode(secondaryBasePostalCode).sorted()
                if (secondaryNeighborhoods.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSecondaryNeighborhood,
                        onExpandedChange = { expandedSecondaryNeighborhood = !expandedSecondaryNeighborhood }
                    ) {
                        OutlinedTextField(
                            value = secondaryBaseNeighborhood,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Colonia/Comunidad") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSecondaryNeighborhood) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled = true).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedSecondaryNeighborhood, onDismissRequest = { expandedSecondaryNeighborhood = false }) {
                            secondaryNeighborhoods.forEach { selection ->
                                DropdownMenuItem(text = { Text(selection) }, onClick = { secondaryBaseNeighborhood = selection; expandedSecondaryNeighborhood = false })
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = secondaryBaseNeighborhood,
                        onValueChange = { secondaryBaseNeighborhood = it },
                        label = { Text("Colonia/Comunidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* TODO: Implementar subida de Licencia */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Subir Licencia de Conducir")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* TODO: Implementar subida de Tarjeta Circulación */ }, modifier = Modifier.fillMaxWidth()) {
                    Text("Subir Tarjeta de Circulación")
                }
            }
            else -> {}
        }

        if (role == UserRole.BUSINESS || role == UserRole.COURIER) {
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Datos Fiscales y Bancarios (Opcionales)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Agrega tu RFC para formalizarte y evitar discrepancias fiscales. Agrega tu CLABE para recibir dispersiones bancarias.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = rfc,
                onValueChange = { rfc = it },
                label = { Text("RFC") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = clabe,
                onValueChange = { clabe = it },
                label = { Text("CLABE Interbancaria") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val motoSpecs = if (role == UserRole.COURIER) {
                        MotorcycleSpecs(
                            brand = motoBrand,
                            model = motoModel,
                            year = motoYear.toIntOrNull() ?: 0,
                            displacementCc = motoCc.toIntOrNull() ?: 0,
                            fuelEfficiencyKmPerL = motoKml.toDoubleOrNull() ?: 0.0,
                            plateNumber = motoPlate
                        )
                    } else null

                    val primaryBase = if (role == UserRole.COURIER && primaryBaseNeighborhood.isNotBlank()) {
                        com.example.cooperativadeliveryapp.domain.model.CourierRestBase(
                            id = "base_1",
                            label = "Mi Casa",
                            neighborhood = primaryBaseNeighborhood,
                            postalCode = primaryBasePostalCode,
                            isCurrentActiveBase = true
                        )
                    } else null
                    
                    val secondaryBase = if (role == UserRole.COURIER && secondaryBaseNeighborhood.isNotBlank()) {
                        com.example.cooperativadeliveryapp.domain.model.CourierRestBase(
                            id = "base_2",
                            label = "Base de Respaldo",
                            neighborhood = secondaryBaseNeighborhood,
                            postalCode = secondaryBasePostalCode,
                            isCurrentActiveBase = false
                        )
                    } else null

                    onSubmit(
                        AuthUiEvent.OnCompleteProfileSubmit(
                            uid = uid,
                            role = role,
                            name = name,
                            communityId = communityId.ifBlank { null },
                            motorcycleSpecs = motoSpecs,
                            primaryRestBase = primaryBase,
                            secondaryRestBase = secondaryBase,
                            rfc = rfc.ifBlank { null },
                            clabe = clabe.ifBlank { null }
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text("Guardar Perfil")
            }
            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

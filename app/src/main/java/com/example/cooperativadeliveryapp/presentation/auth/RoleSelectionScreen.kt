package com.example.cooperativadeliveryapp.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cooperativadeliveryapp.domain.model.UserRole

@Composable
fun RoleSelectionScreen(
    selectedRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Selecciona tu rol", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            UserRole.entries.forEach { role ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RadioButton(
                        selected = (role == selectedRole),
                        onClick = { onRoleSelected(role) }
                    )
                    Text(
                        text = when (role) {
                            UserRole.CLIENT -> "Cliente"
                            UserRole.BUSINESS -> "Negocio"
                            UserRole.COURIER -> "Repartidor"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

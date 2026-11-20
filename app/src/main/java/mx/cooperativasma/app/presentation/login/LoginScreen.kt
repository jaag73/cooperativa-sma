package mx.cooperativasma.app.presentation.login

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.cooperativasma.app.domain.model.UserRole
import mx.cooperativasma.app.ui.components.PrimaryActionButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToDashboard: (UserRole) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.loginSuccessProfile) {
        uiState.loginSuccessProfile?.let { profile ->
            Log.d("SMA_AUTH", "Usuario logueado: ${profile.email} con rol activo: ${profile.activeRole}")
            onNavigateToDashboard(UserRole.valueOf(profile.activeRole))
            viewModel.onLoginHandled()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Handshake,
                    contentDescription = "Logo Cooperativa",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Cooperativa SMA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Economía local justa • Vecinos, comercios y repartidores en red",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        ElevatedCard(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (uiState.isRegisterMode) "Crear Cuenta" else "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isRegisterMode) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Nombre Completo o de Negocio") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = uiState.emailOrPhone,
                    onValueChange = { viewModel.onEmailOrPhoneChange(it) },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = viewModel.getPassword(),
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val iconText = if (uiState.isPasswordVisible) "🙈" else "👁️"
                        TextButton(onClick = { viewModel.togglePasswordVisibility() }) {
                            Text(iconText)
                        }
                    },
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                )

                if (!uiState.isRegisterMode) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = { viewModel.sendPasswordResetEmail() },
                            enabled = !uiState.isLoading
                        ) {
                            Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Selecciona tu rol inicial:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { role ->
                            val isSelected = uiState.selectedRole == role
                            val label = when (role) {
                                UserRole.CLIENT -> "Cliente"
                                UserRole.BUSINESS -> "Negocio"
                                UserRole.COURIER -> "Repartidor"
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onRoleSelect(role) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    PrimaryActionButton(
                        text = if (uiState.isRegisterMode) "Crear Cuenta" else "Ingresar",
                        onClick = {
                            if (uiState.isRegisterMode) {
                                viewModel.register()
                            } else {
                                viewModel.login()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.signInWithGoogle(context) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Continuar con Google")
                    }
                }

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val rawError = uiState.errorMessage!!
                    val displayError = if (rawError.contains("127.0.0.1") || rawError.contains("10.0.2.2") || rawError.contains("Network")) {
                        "No se pudo enlazar con el servidor local de la Cooperativa. Verifica que el emulador Firebase esté activo."
                    } else {
                        rawError
                    }
                    
                    Surface(
                        color = if (rawError.startsWith("Correo de recuperación enviado")) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayError,
                            color = if (rawError.startsWith("Correo de recuperación enviado")) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (mx.cooperativasma.app.BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Accesos Rápidos (DEBUG)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleDebugCard(
                    icon = Icons.Rounded.Person,
                    label = "Cliente",
                    onClick = { viewModel.fillDemoCredentials(UserRole.CLIENT) },
                    modifier = Modifier.weight(1f)
                )
                RoleDebugCard(
                    icon = Icons.Rounded.Storefront,
                    label = "Negocio",
                    onClick = { viewModel.fillDemoCredentials(UserRole.BUSINESS) },
                    modifier = Modifier.weight(1f)
                )
                RoleDebugCard(
                    icon = Icons.Rounded.TwoWheeler,
                    label = "Repartidor",
                    onClick = { viewModel.fillDemoCredentials(UserRole.COURIER) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        TextButton(
            onClick = {
                viewModel.toggleRegisterMode()
            }
        ) {
            Text(
                text = if (uiState.isRegisterMode) "¿Ya tienes cuenta? Inicia sesión aquí" else "¿Aún no tienes cuenta? Regístrate aquí",
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (uiState.showGoogleOnboardingDialog) {
        var googleRole by remember { mutableStateOf(UserRole.CLIENT) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissGoogleOnboardingDialog() },
            title = {
                Text(
                    text = "¡Bienvenido a Cooperativa SMA!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selecciona tu rol inicial para comenzar en la red:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    UserRole.entries.forEach { role ->
                        val isSelected = googleRole == role
                        val label = when (role) {
                            UserRole.CLIENT -> "🏠 Cliente / Vecino"
                            UserRole.BUSINESS -> "🏪 Negocio / Comercio"
                            UserRole.COURIER -> "🛵 Repartidor"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { googleRole = role }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeGoogleOnboarding(googleRole)
                    }
                ) {
                    Text("Confirmar y Entrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissGoogleOnboardingDialog() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RoleDebugCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier.height(64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, maxLines = 1)
        }
    }
}

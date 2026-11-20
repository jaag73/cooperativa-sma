package mx.cooperativasma.app.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientKycScreen(
    onSubmitKyc: (idDocumentUrl: String, selfieUrl: String, proofOfAddressUrl: String?) -> Unit,
    onCancel: () -> Unit
) {
    var idDocumentUrl by remember { mutableStateOf("") }
    var selfieUrl by remember { mutableStateOf("") }
    var addressMatchesId by remember { mutableStateOf(true) }
    var proofOfAddressUrl by remember { mutableStateOf("") }

    val isFormValid = idDocumentUrl.isNotBlank() && selfieUrl.isNotBlank() &&
            (addressMatchesId || proofOfAddressUrl.isNotBlank())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Verificación de Identidad (KYC)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para activar el pago en efectivo y garantizar la seguridad de nuestra comunidad, necesitamos verificar tu identidad.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // TODO: En el futuro cambiar por botones que lancen cámara o galería
        OutlinedTextField(
            value = idDocumentUrl,
            onValueChange = { idDocumentUrl = it },
            label = { Text("URL o Ruta de Foto INE (Frente y Reverso)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = selfieUrl,
            onValueChange = { selfieUrl = it },
            label = { Text("URL o Ruta de Selfie Reciente") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = addressMatchesId,
                onCheckedChange = { addressMatchesId = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("¿Tu dirección de entrega principal coincide con tu INE?")
        }

        AnimatedVisibility(visible = !addressMatchesId) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = proofOfAddressUrl,
                    onValueChange = { proofOfAddressUrl = it },
                    label = { Text("URL o Ruta de Comprobante de Domicilio") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onSubmitKyc(
                    idDocumentUrl,
                    selfieUrl,
                    if (addressMatchesId) null else proofOfAddressUrl
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("Enviar para Revisión")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onCancel) {
            Text("Omitir y continuar sin pago en efectivo")
        }
    }
}

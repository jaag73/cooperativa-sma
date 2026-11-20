package mx.cooperativasma.app.presentation.client

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import mx.cooperativasma.app.ui.components.PinTicketCard
import mx.cooperativasma.app.ui.components.StatusStepper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import mx.cooperativasma.app.domain.model.OrderLifecycleStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrderTrackingScreen(
    viewModel: ClientOrderTrackingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val activeOrder by viewModel.activeOrder.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Pedido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("⬅️")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeOrder == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes pedidos activos en curso.")
                }
            } else {
                val order = activeOrder!!

                Text("Estado Actual", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                StatusStepper(
                    currentStatus = order.status,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                PinTicketCard(
                    pin = order.deliveryPin,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

package com.example.cooperativadeliveryapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cooperativadeliveryapp.domain.model.OrderLifecycleStatus

@Composable
fun StatusStepper(
    currentStatus: OrderLifecycleStatus,
    modifier: Modifier = Modifier
) {
    val statuses = listOf(
        OrderLifecycleStatus.PENDING_ACCEPTANCE to "Esperando Confirmación",
        OrderLifecycleStatus.PREPARING_AT_BUSINESS to "Preparando Pedido",
        OrderLifecycleStatus.READY_FOR_PICKUP to "Listo en Mostrador",
        OrderLifecycleStatus.COURIER_ASSIGNED to "Repartidor Asignado",
        OrderLifecycleStatus.PICKED_UP_AT_BIKE to "Repartidor en Tienda",
        OrderLifecycleStatus.IN_TRANSIT_TO_CLIENT to "En Camino",
        OrderLifecycleStatus.DELIVERED to "Entregado"
    )

    val currentIndex = statuses.indexOfFirst { it.first == currentStatus }

    Column(modifier = modifier.fillMaxWidth()) {
        statuses.forEachIndexed { index, (status, label) ->
            val isCompleted = index <= currentIndex
            val isCurrent = index == currentIndex
            val color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isCurrent) MaterialTheme.colorScheme.secondary else color),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted && !isCurrent) {
                        Text("✓", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                    } else if (isCurrent) {
                        Text("•", color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isCurrent) MaterialTheme.colorScheme.secondary else color
                )
            }
            if (index < statuses.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 11.dp)
                        .width(2.dp)
                        .height(24.dp)
                        .background(color)
                )
            }
        }
    }
}

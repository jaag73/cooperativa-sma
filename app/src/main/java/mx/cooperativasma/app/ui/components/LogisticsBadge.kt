package mx.cooperativasma.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mx.cooperativasma.app.domain.model.ProductPhysicalSpecs
import mx.cooperativasma.app.domain.model.ThermalRequirement
import mx.cooperativasma.app.ui.theme.LogisticsAmbient
import mx.cooperativasma.app.ui.theme.LogisticsCold
import mx.cooperativasma.app.ui.theme.LogisticsFragile
import mx.cooperativasma.app.ui.theme.LogisticsHot

@Composable
fun LogisticsBadge(
    specs: ProductPhysicalSpecs,
    modifier: Modifier = Modifier
) {
    val (color, text) = when {
        specs.isFragile -> LogisticsFragile to "🥚 Frágil"
        specs.thermalRequirement == ThermalRequirement.HOT -> LogisticsHot to "🔥 Caliente"
        specs.thermalRequirement == ThermalRequirement.COLD -> LogisticsCold to "❄️ Frío"
        else -> LogisticsAmbient to "📦 Seco"
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SimpleBadge(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

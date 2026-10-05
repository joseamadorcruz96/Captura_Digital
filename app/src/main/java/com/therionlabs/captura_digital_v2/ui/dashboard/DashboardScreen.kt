package com.therionlabs.captura_digital_v2.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.ui.auth.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzul
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzulMedio
import com.therionlabs.captura_digital_v2.ui.auth.FondoClaro

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(uiState = uiState, onBack = onBack)
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(DuocAzul, DuocAzulMedio)))
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
                Text("← Volver", color = Color.White)
            }
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text("Perfil de los prospectos encuestados", color = DuocAmarillo)
        }

        Spacer(Modifier.height(16.dp))

        // Resumen
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard("Encuestas", "${uiState.total}", Modifier.weight(1f))
            SummaryCard("Edad promedio", "${uiState.averageAge} años", Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        // Gráficos
        ChartCard("Género", uiState.byGender, uiState.total, DuocAzul)
        ChartCard("Rango de edad", uiState.byAge, uiState.total, DuocAzulMedio)
        ChartCard("Intereses", uiState.byInterest, uiState.total, DuocAmarillo)

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    data: Map<String, Int>,
    total: Int,
    barColor: Color
) {
    val max = data.values.maxOrNull() ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            Spacer(Modifier.height(8.dp))

            data.forEach { (label, count) ->
                val percent = if (total == 0) 0 else count * 100 / total
                val fraction = if (max == 0) 0f else count.toFloat() / max

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label)
                        Text("$count ($percent%)", color = Color.Gray)
                    }
                    Spacer(Modifier.height(4.dp))

                    // Barra
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE6E9F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}
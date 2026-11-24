package com.therionlabs.captura_digital_v2.ui.dashboard

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.di.rememberAppViewModel
import com.therionlabs.captura_digital_v2.ui.auth.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzul
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzulMedio
import com.therionlabs.captura_digital_v2.ui.auth.FondoClaro
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader

private val pieColors = listOf(
    DuocAzul,
    DuocAmarillo,
    DuocAzulMedio,
    Color(0xFF7FA7D9),
    Color(0xFFB0B7C3)
)

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = rememberAppViewModel { container ->
        if (container != null) {
            DashboardViewModel(
                prospectoRepository = container.prospectoRepository,
                authRepository = container.authRepository,
                indicadoresApi = container.indicadoresApi
            )
        } else {
            DashboardViewModel()
        }
    },
    onBack: () -> Unit,
    onUsuarios: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DashboardScreen(
        uiState = uiState,
        onBack = onBack,
        onUsuarios = onUsuarios,
        onRotarInteres = viewModel::rotarNivelInteres,
        onEliminarProspecto = viewModel::eliminarProspecto,
        onShare = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, viewModel.shareText())
            }
            context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
        }
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onBack: () -> Unit,
    onUsuarios: () -> Unit,
    onRotarInteres: (Prospecto) -> Unit = {},
    onEliminarProspecto: (String) -> Unit = {},
    onShare: () -> Unit
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "dashboard"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        DuocHeader(
            title = "Dashboard",
            subtitle = "Perfil de los prospectos encuestados",
            onBack = onBack
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard("Encuestas", "${uiState.total}", Modifier.weight(1f))
            SummaryCard("Edad promedio", "${uiState.averageAge} años", Modifier.weight(1f))
        }

        if (uiState.uf != null) {
            Spacer(Modifier.height(12.dp))
            IndicadoresCard(uiState)
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onShare,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DuocAmarillo,
                contentColor = DuocAzul
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
        ) {
            Text("Compartir resumen", fontWeight = FontWeight.Bold)
        }

        if (uiState.isAdmin) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onUsuarios,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DuocAzul),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp)
            ) {
                Text("Gestionar usuarios", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        PieChartCard("Género", uiState.byGender, progress)
        PieChartCard("Rango de edad", uiState.byAge, progress)
        ChartCard("Intereses", uiState.byInterest, uiState.total, DuocAmarillo, progress)

        Spacer(Modifier.height(12.dp))
        ProspectosCrudCard(
            prospectos = uiState.prospectos,
            actionMessage = uiState.actionMessage,
            onRotarInteres = onRotarInteres,
            onEliminarProspecto = onEliminarProspecto
        )

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
private fun PieChartCard(title: String, data: Map<String, Int>, progress: Float) {
    val total = data.values.sum()

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
            Spacer(Modifier.height(12.dp))

            if (total == 0) {
                Text("Sin datos", color = Color.Gray)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(130.dp)) {
                            var startAngle = -90f
                            data.values.forEachIndexed { index, count ->
                                val sweep = 360f * count / total * progress
                                drawArc(
                                    color = pieColors[index % pieColors.size],
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = true
                                )
                                startAngle += sweep
                            }
                            drawCircle(color = Color.White, radius = size.minDimension / 4)
                        }
                        Text("$total", fontWeight = FontWeight.Bold, color = DuocAzul)
                    }

                    Spacer(Modifier.width(20.dp))

                    Column {
                        data.entries.forEachIndexed { index, entry ->
                            val percent = entry.value * 100 / total
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(pieColors[index % pieColors.size])
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(entry.key)
                                Spacer(Modifier.width(6.dp))
                                Text("$percent%", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    data: Map<String, Int>,
    total: Int,
    barColor: Color,
    progress: Float
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE6E9F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction * progress)
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
@Composable
private fun IndicadoresCard(uiState: DashboardUiState) {
    val formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Indicadores económicos de hoy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                IndicadorItem("UF", uiState.uf?.let { formato.format(it) }, Modifier.weight(1f))
                IndicadorItem("Dólar", uiState.dolar?.let { formato.format(it) }, Modifier.weight(1f))
                IndicadorItem("UTM", uiState.utm?.let { formato.format(it) }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Fuente: mindicador.cl",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun IndicadorItem(nombre: String, valor: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(nombre, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(valor ?: "-", fontWeight = FontWeight.Bold, color = DuocAzul)
    }
}

@Composable
private fun ProspectosCrudCard(
    prospectos: List<Prospecto>,
    actionMessage: String?,
    onRotarInteres: (Prospecto) -> Unit,
    onEliminarProspecto: (String) -> Unit
) {
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
                text = "Registros recientes (CRUD)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            if (actionMessage != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = actionMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = DuocAzulMedio,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))

            if (prospectos.isEmpty()) {
                Text("No hay prospectos registrados", color = Color.Gray)
            } else {
                prospectos.take(10).forEach { prospecto ->
                    val statusColor = when (prospecto.syncStatus) {
                        SyncStatus.SYNCED -> Color(0xFF2E7D32)
                        SyncStatus.PENDING_SYNC -> Color(0xFFEF6C00)
                        SyncStatus.SYNC_ERROR -> Color(0xFFC62828)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prospecto.nombreCompleto,
                                    fontWeight = FontWeight.Bold,
                                    color = DuocAzul
                                )
                                Text(
                                    text = "${prospecto.rut} · ${prospecto.carreraPrioridad1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                            Text(
                                text = prospecto.syncStatus.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { onRotarInteres(prospecto) }) {
                                Text("Interés: ${prospecto.nivelInteres.displayName}")
                            }
                            TextButton(onClick = { onEliminarProspecto(prospecto.id) }) {
                                Text("Eliminar", color = Color(0xFFC62828))
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}
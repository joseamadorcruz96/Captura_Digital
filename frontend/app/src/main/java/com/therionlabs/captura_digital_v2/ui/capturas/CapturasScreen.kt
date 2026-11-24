package com.therionlabs.captura_digital_v2.ui.capturas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.EstadoCompletitud
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.data.model.SyncStatus
import com.therionlabs.captura_digital_v2.di.rememberAppViewModel
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.formularioModal.FormStep
import com.therionlabs.captura_digital_v2.ui.formularioModal.ProspectoDetalleDialog
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import com.therionlabs.captura_digital_v2.ui.theme.FondoClaro
import com.therionlabs.captura_digital_v2.util.EventoDateUtils

@Composable
fun CapturasRoute(
    viewModel: CapturasViewModel = rememberAppViewModel { container ->
        if (container != null) {
            CapturasViewModel(
                authRepository = container.authRepository,
                prospectoRepository = container.prospectoRepository,
                borradorRepository = container.borradorRepository
            )
        } else {
            CapturasViewModel()
        }
    },
    onNewCapture: () -> Unit,
    onResumeBorrador: (String) -> Unit,
    onEditProspecto: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CapturasScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSyncPendientes = viewModel::sincronizarPendientes,
        onNewCapture = onNewCapture,
        onResumeBorrador = onResumeBorrador,
        onDiscardBorrador = viewModel::descartarBorrador,
        onOpenDetalle = viewModel::abrirDetalle,
        onDismissDetalle = viewModel::cerrarDetalle,
        onEditProspecto = { prospecto ->
            viewModel.cerrarDetalle()
            onEditProspecto(prospecto.id)
        },
        onBack = onBack
    )
}

@Composable
fun CapturasScreen(
    uiState: CapturasUiState,
    onFilterSelected: (CapturasFilter) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSyncPendientes: () -> Unit,
    onNewCapture: () -> Unit,
    onResumeBorrador: (String) -> Unit,
    onDiscardBorrador: (FormularioBorrador) -> Unit,
    onOpenDetalle: (Prospecto) -> Unit,
    onDismissDetalle: () -> Unit,
    onEditProspecto: (Prospecto) -> Unit,
    onBack: () -> Unit
) {
    uiState.selectedProspectoDetail?.let { detail ->
        ProspectoDetalleDialog(
            prospecto = detail,
            onDismiss = onDismissDetalle,
            primaryActionLabel = if (detail.estadoCompletitud == EstadoCompletitud.EXPRESS) {
                "Completar ficha"
            } else {
                "Editar ficha"
            },
            onPrimaryAction = { onEditProspecto(detail) }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        DuocHeader(
            title = if (uiState.isAdmin) "Capturas y Borradores" else "Mis Capturas y Borradores",
            subtitle = "${uiState.borradores.size} borrador(es) · ${uiState.capturas.size} captura(s)",
            onBack = onBack,
            trailingAction = {
                TextButton(
                    onClick = onNewCapture,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+ Nueva", color = DuocAmarillo, fontWeight = FontWeight.Bold)
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Resumen de sincronización: diferencia borradores no sincronizables de capturas válidas
            DuocCard(elevation = 2.dp, contentPadding = 14.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.pendingSyncCount > 0) {
                                "${uiState.pendingSyncCount} captura(s) listas para sincronizar"
                            } else {
                                "Todas tus capturas válidas están sincronizadas"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DuocAzul
                        )
                        Text(
                            text = "Los borradores locales requieren completar sus datos mínimos antes de sincronizarse.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }
                    if (uiState.pendingSyncCount > 0) {
                        Button(
                            onClick = onSyncPendientes,
                            enabled = !uiState.isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = DuocAzul),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(if (uiState.appMode == AppMode.LOCAL_DEMO) "Simular Sync" else "Sincronizar")
                            }
                        }
                    }
                }
                if (!uiState.statusMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = uiState.statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = DuocAzul
                    )
                }
            }

            DuocTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                label = "Buscar por nombre, RUT o carrera",
                placeholder = "Ej: Camila o 20.123.456-5"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CapturasFilter.entries.forEach { item ->
                    val count = when (item) {
                        CapturasFilter.TODOS -> uiState.borradores.size + uiState.capturas.size
                        CapturasFilter.BORRADORES -> uiState.borradores.size
                        CapturasFilter.PENDIENTES -> uiState.pendingSyncCount
                        CapturasFilter.SINCRONIZADOS -> uiState.syncedCount
                    }
                    FilterChip(
                        selected = uiState.filter == item,
                        onClick = { onFilterSelected(item) },
                        label = { Text("${item.label} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DuocAzul,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            val borradoresVisibles = uiState.borradoresFiltrados
            val capturasVisibles = uiState.capturasFiltradas

            if (borradoresVisibles.isEmpty() && capturasVisibles.isEmpty()) {
                DuocCard(elevation = 1.dp, contentPadding = 20.dp) {
                    Text(
                        text = "No hay registros en esta vista",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Inicia una nueva captura o guarda un borrador en terreno para verlo aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(14.dp))
                    DuocButton(text = "Iniciar Nueva Captura", onClick = onNewCapture)
                }
            } else {
                if (borradoresVisibles.isNotEmpty()) {
                    Text(
                        text = "Borradores en progreso (solo local)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    borradoresVisibles.forEach { draft ->
                        BorradorCard(
                            borrador = draft,
                            onRetomar = { onResumeBorrador(draft.id) },
                            onDescartar = { onDiscardBorrador(draft) }
                        )
                    }
                }

                if (capturasVisibles.isNotEmpty()) {
                    Text(
                        text = "Capturas realizadas (sincronizables)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    capturasVisibles.forEach { prospecto ->
                        CapturaRealizadaCard(
                            prospecto = prospecto,
                            onVerDetalle = { onOpenDetalle(prospecto) },
                            onEditar = { onEditProspecto(prospecto) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun BorradorCard(
    borrador: FormularioBorrador,
    onRetomar: () -> Unit,
    onDescartar: () -> Unit
) {
    val stepTitle = runCatching { FormStep.valueOf(borrador.currentStep) }
        .getOrNull()
        ?.let { "Paso ${it.index + 1}/6" }
        ?: "En progreso"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, DuocAmarillo),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DuocAmarillo.copy(alpha = 0.28f)
                ) {
                    Text(
                        text = "Borrador · No sincronizable",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "${if (borrador.modoExpress) "Modo Express" else "Completo · $stepTitle"} · ${EventoDateUtils.formatTime(borrador.updatedAtEpoch)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Text(
                text = borrador.tituloResumen,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            Text(
                text = borrador.resumenSecundario,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRetomar,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retomar")
                }
                TextButton(
                    onClick = onDescartar,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Descartar", color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CapturaRealizadaCard(
    prospecto: Prospecto,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit
) {
    val syncColor = when (prospecto.syncStatus) {
        SyncStatus.SYNCED -> ExitoVerde
        SyncStatus.PENDING_SYNC -> Color(0xFFEF6C00)
        SyncStatus.SYNC_ERROR -> Color(0xFFC62828)
    }

    DuocCard(elevation = 2.dp, contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = syncColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = prospecto.syncStatus.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = syncColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (prospecto.estadoCompletitud == EstadoCompletitud.EXPRESS) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DuocAmarillo.copy(alpha = 0.28f)
                    ) {
                        Text(
                            text = "Express · Incompleto",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DuocAzul,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Text(
                text = EventoDateUtils.formatTime(prospecto.fechaCaptura),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = prospecto.nombreCompleto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DuocAzul
        )
        Text(
            text = "${prospecto.rut} · ${prospecto.sedeInteres}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
        Text(
            text = prospecto.carreraPrioridad1,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = DuocAzul
        )

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onVerDetalle,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Ver ficha")
            }
            OutlinedButton(
                onClick = onEditar,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (prospecto.estadoCompletitud == EstadoCompletitud.EXPRESS) "Completar" else "Editar"
                )
            }
        }
    }
}

@Preview(name = "CapturasScreen - Claro", showBackground = true, showSystemUi = true)
@Composable
fun CapturasScreenPreview() {
    Captura_Digital_v2Theme {
        CapturasScreen(
            uiState = CapturasUiState(
                borradores = listOf(
                    FormularioBorrador(
                        id = "b-1",
                        captadorId = 2,
                        nombres = "Matías",
                        apellidos = "Rojas",
                        rut = "20.123.456-5",
                        currentStep = FormStep.CONTACTO.name
                    )
                ),
                capturas = listOf(
                    Prospecto(
                        id = "p-1",
                        captadorId = 2,
                        rut = "11.111.111-1",
                        nombres = "Camila",
                        apellidos = "Valenzuela",
                        comunaResidencia = "Por completar",
                        celular = "+56987654321",
                        email = "",
                        situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                        establecimientoEducacional = "Por completar",
                        comunaEstablecimiento = "Por completar",
                        jornadaPreferencia = Jornada.DIURNA,
                        sedeInteres = "Viña del Mar",
                        nivelInteres = NivelInteres.MEDIO,
                        perfilMotivacional = PerfilMotivacional.VOCACIONAL,
                        carreraPrioridad1 = "Ingeniería en Informática",
                        estadoCompletitud = EstadoCompletitud.EXPRESS,
                        syncStatus = SyncStatus.PENDING_SYNC
                    )
                )
            ),
            onFilterSelected = {},
            onSearchQueryChange = {},
            onSyncPendientes = {},
            onNewCapture = {},
            onResumeBorrador = {},
            onDiscardBorrador = {},
            onOpenDetalle = {},
            onDismissDetalle = {},
            onEditProspecto = {},
            onBack = {}
        )
    }
}

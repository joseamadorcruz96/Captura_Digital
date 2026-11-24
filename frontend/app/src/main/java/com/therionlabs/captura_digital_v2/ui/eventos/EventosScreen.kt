package com.therionlabs.captura_digital_v2.ui.eventos

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therionlabs.captura_digital_v2.data.model.CatalogoDuoc
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.di.rememberAppViewModel
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import com.therionlabs.captura_digital_v2.ui.theme.FondoClaro
import com.therionlabs.captura_digital_v2.util.EventoDateUtils

@Composable
fun EventosRoute(
    viewModel: EventosViewModel = rememberAppViewModel { container ->
        if (container != null) EventosViewModel(container.eventoRepository) else EventosViewModel()
    },
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EventosScreen(
        uiState = uiState,
        onFiltroChange = viewModel::onFiltroChange,
        onNuevoEvento = viewModel::abrirNuevoEvento,
        onEditarEvento = viewModel::editarEvento,
        onCerrarFormulario = viewModel::cerrarFormulario,
        onNombreChange = viewModel::onNombreChange,
        onTipoChange = viewModel::onTipoChange,
        onSedeChange = viewModel::onSedeChange,
        onUbicacionChange = viewModel::onUbicacionChange,
        onFechaInicioChange = viewModel::onFechaInicioChange,
        onFechaFinChange = viewModel::onFechaFinChange,
        onMetaChange = viewModel::onMetaChange,
        onGuardar = viewModel::guardarEvento,
        onToggleActivo = viewModel::toggleActivo,
        onToggleArchivar = viewModel::toggleArchivar,
        onSeleccionarComoActivo = viewModel::seleccionarComoActivo,
        onBack = onBack
    )
}

@Composable
fun EventosScreen(
    uiState: EventosUiState,
    onFiltroChange: (EventoFilter) -> Unit,
    onNuevoEvento: () -> Unit,
    onEditarEvento: (Evento) -> Unit,
    onCerrarFormulario: () -> Unit,
    onNombreChange: (String) -> Unit,
    onTipoChange: (TipoEvento) -> Unit,
    onSedeChange: (String) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onFechaInicioChange: (String) -> Unit,
    onFechaFinChange: (String) -> Unit,
    onMetaChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onToggleActivo: (Evento) -> Unit,
    onToggleArchivar: (Evento) -> Unit,
    onSeleccionarComoActivo: (Evento) -> Unit,
    onBack: () -> Unit
) {
    if (uiState.mostrarFormulario) {
        EventoFormDialog(
            uiState = uiState,
            onNombreChange = onNombreChange,
            onTipoChange = onTipoChange,
            onSedeChange = onSedeChange,
            onUbicacionChange = onUbicacionChange,
            onFechaInicioChange = onFechaInicioChange,
            onFechaFinChange = onFechaFinChange,
            onMetaChange = onMetaChange,
            onGuardar = onGuardar,
            onDismiss = onCerrarFormulario
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        DuocHeader(
            title = "Eventos y Ferias",
            subtitle = "Administración de ferias, colegios y metas de captura",
            onBack = onBack,
            trailingAction = {
                TextButton(onClick = onNuevoEvento) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = DuocAmarillo)
                    Spacer(Modifier.width(4.dp))
                    Text("Nuevo", color = DuocAmarillo, fontWeight = FontWeight.Bold)
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DuocButton(
                text = "+ Crear nuevo evento o feria",
                onClick = onNuevoEvento
            )

            // Filtros: Activos / Todos / Archivados
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventoFilter.entries.forEach { filtro ->
                    FilterChip(
                        selected = uiState.filtro == filtro,
                        onClick = { onFiltroChange(filtro) },
                        label = { Text(filtro.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DuocAmarillo,
                            selectedLabelColor = DuocAzul
                        )
                    )
                }
            }

            uiState.mensaje?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ExitoVerde.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = msg,
                        color = ExitoVerde,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }

            val eventos = uiState.eventosFiltrados
            Text(
                text = "Mostrando ${eventos.size} evento(s)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )

            if (eventos.isEmpty()) {
                DuocCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay eventos en esta vista.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                eventos.forEach { evento ->
                    EventoAdminCard(
                        evento = evento,
                        esEventoEnUso = evento.id == uiState.activeEventId,
                        onEditar = { onEditarEvento(evento) },
                        onToggleActivo = { onToggleActivo(evento) },
                        onToggleArchivar = { onToggleArchivar(evento) },
                        onSeleccionarComoActivo = { onSeleccionarComoActivo(evento) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EventoAdminCard(
    evento: Evento,
    esEventoEnUso: Boolean,
    onEditar: () -> Unit,
    onToggleActivo: () -> Unit,
    onToggleArchivar: () -> Unit,
    onSeleccionarComoActivo: () -> Unit
) {
    DuocCard(modifier = Modifier.fillMaxWidth(), elevation = 2.dp, contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DuocAmarillo.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = evento.tipo.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                val estadoLabel = when {
                    evento.archivado -> "Archivado"
                    evento.activo -> "Activo"
                    else -> "Inactivo"
                }
                val estadoColor = when {
                    evento.archivado -> Color.Gray
                    evento.activo -> ExitoVerde
                    else -> Color(0xFFB45309)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = estadoColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = estadoLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = estadoColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (esEventoEnUso) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DuocAzul
                    ) {
                        Text(
                            text = "En uso",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar evento", tint = DuocAzul)
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            text = evento.nombre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DuocAzul
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${evento.sedeAsociada} • ${evento.ubicacion}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${EventoDateUtils.formatDateRange(evento.fechaInicio, evento.fechaFin)} • Meta: ${evento.metaProspectos} prospectos",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!evento.archivado) {
                    TextButton(
                        onClick = onToggleActivo,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (evento.activo) "Desactivar" else "Activar",
                            color = DuocAzul,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                TextButton(
                    onClick = onToggleArchivar,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (evento.archivado) "Desarchivar" else "Archivar",
                        color = if (evento.archivado) DuocAzul else Color.DarkGray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (evento.estaDisponibleParaCaptura && !esEventoEnUso) {
                TextButton(
                    onClick = onSeleccionarComoActivo,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Usar ahora", color = ExitoVerde, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventoFormDialog(
    uiState: EventosUiState,
    onNombreChange: (String) -> Unit,
    onTipoChange: (TipoEvento) -> Unit,
    onSedeChange: (String) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onFechaInicioChange: (String) -> Unit,
    onFechaFinChange: (String) -> Unit,
    onMetaChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit
) {
    val editando = uiState.editandoId != null
    var sedesExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (editando) "Editar evento" else "Nuevo evento",
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DuocTextField(
                    value = uiState.nombre,
                    onValueChange = onNombreChange,
                    label = "Nombre del evento *",
                    placeholder = "Ej: Feria Vocacional Viña 2026",
                    errorMessage = uiState.nombreError
                )

                Text("Tipo de evento", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DuocAzul)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TipoEvento.entries.forEach { tipo ->
                        FilterChip(
                            selected = uiState.tipo == tipo,
                            onClick = { onTipoChange(tipo) },
                            label = { Text(tipo.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuocAmarillo,
                                selectedLabelColor = DuocAzul
                            )
                        )
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = sedesExpanded,
                    onExpandedChange = { sedesExpanded = !sedesExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.sedeAsociada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sede asociada *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sedesExpanded) },
                        isError = uiState.sedeError != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = sedesExpanded,
                        onDismissRequest = { sedesExpanded = false }
                    ) {
                        CatalogoDuoc.sedes.forEach { sede ->
                            DropdownMenuItem(
                                text = { Text("${sede.nombre} (${sede.region})") },
                                onClick = {
                                    onSedeChange(sede.nombre)
                                    sedesExpanded = false
                                }
                            )
                        }
                    }
                }
                uiState.sedeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                DuocTextField(
                    value = uiState.ubicacion,
                    onValueChange = onUbicacionChange,
                    label = "Ubicación / Establecimiento *",
                    placeholder = "Ej: Colegio Rubén Castro, Viña del Mar",
                    errorMessage = uiState.ubicacionError
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DuocTextField(
                        value = uiState.fechaInicioText,
                        onValueChange = onFechaInicioChange,
                        label = "Inicio (dd/MM/yyyy)",
                        errorMessage = uiState.fechaInicioError,
                        modifier = Modifier.weight(1f)
                    )
                    DuocTextField(
                        value = uiState.fechaFinText,
                        onValueChange = onFechaFinChange,
                        label = "Término (dd/MM/yyyy)",
                        errorMessage = uiState.fechaFinError,
                        modifier = Modifier.weight(1f)
                    )
                }

                DuocTextField(
                    value = uiState.metaText,
                    onValueChange = onMetaChange,
                    label = "Meta de prospectos *",
                    keyboardType = KeyboardType.Number,
                    errorMessage = uiState.metaError
                )

                uiState.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGuardar,
                enabled = !uiState.cargando,
                colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
            ) {
                Text(if (editando) "Guardar cambios" else "Crear evento")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = DuocAzul)
            }
        }
    )
}

package com.therionlabs.captura_digital_v2.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.Evento
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.ResumenCierreTurno
import com.therionlabs.captura_digital_v2.data.model.TipoEvento
import com.therionlabs.captura_digital_v2.data.model.TurnoEvento
import com.therionlabs.captura_digital_v2.data.model.User
import com.therionlabs.captura_digital_v2.data.model.UserRole
import com.therionlabs.captura_digital_v2.di.rememberAppViewModel
import com.therionlabs.captura_digital_v2.ui.components.ConnectivityBadge
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocOutlinedButton
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import com.therionlabs.captura_digital_v2.ui.theme.FondoClaro
import com.therionlabs.captura_digital_v2.util.EventoDateUtils

@Composable
fun InicioRoute(
    viewModel: InicioViewModel = rememberAppViewModel { container ->
        if (container != null) {
            InicioViewModel(
                authRepository = container.authRepository,
                prospectoRepository = container.prospectoRepository,
                connectivityObserver = container.connectivityObserver,
                eventoRepository = container.eventoRepository
            )
        } else {
            InicioViewModel()
        }
    },
    onNewCapture: () -> Unit,
    onDashboard: () -> Unit,
    onManageEventos: () -> Unit = {},
    onHelp: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.alReanudar()
    }

    InicioScreen(
        uiState = uiState,
        onNewCapture = onNewCapture,
        onDashboard = onDashboard,
        onManageEventos = onManageEventos,
        onHelp = onHelp,
        onSync = viewModel::sincronizarAhora,
        onResetDemoData = viewModel::poblarDatosDemo,
        onSelectEvent = viewModel::seleccionarEvento,
        onStartTurno = viewModel::iniciarTurno,
        onFinishTurno = viewModel::finalizarTurno,
        onDismissResumenTurno = viewModel::cerrarResumenTurno,
        onLogout = {
            viewModel.logout()
            onLogout()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(
    uiState: InicioUiState,
    onNewCapture: () -> Unit,
    onDashboard: () -> Unit,
    onManageEventos: () -> Unit = {},
    onHelp: () -> Unit,
    onSync: () -> Unit = {},
    onResetDemoData: () -> Unit = {},
    onSelectEvent: (String) -> Unit = {},
    onStartTurno: (Int?) -> Unit = {},
    onFinishTurno: () -> Unit = {},
    onDismissResumenTurno: () -> Unit = {},
    onLogout: () -> Unit
) {
    val userName = uiState.user?.name ?: "Usuario"
    val roleName = uiState.user?.role?.displayName ?: "Captura Digital"
    var showEventSelector by remember { mutableStateOf(false) }
    var showCheckInDialog by remember { mutableStateOf(false) }

    if (showEventSelector) {
        ModalBottomSheet(
            onDismissRequest = { showEventSelector = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Seleccionar Evento Activo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Text(
                    text = "Los nuevos cupones capturados se asociarán automáticamente al evento seleccionado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(4.dp))

                if (uiState.availableEvents.isEmpty()) {
                    Text(
                        text = "No hay eventos activos disponibles. Un administrador puede activarlos en Gestión de Eventos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    uiState.availableEvents.forEach { evento ->
                        val selected = evento.id == uiState.activeEventId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectEvent(evento.id)
                                    showEventSelector = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) DuocAzul.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                            border = if (selected) {
                                androidx.compose.foundation.BorderStroke(1.5.dp, DuocAzul)
                            } else null
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = DuocAmarillo.copy(alpha = 0.35f)
                                        ) {
                                            Text(
                                                text = evento.tipo.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DuocAzul,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "Meta: ${evento.metaProspectos}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = evento.nombre,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DuocAzul
                                    )
                                    Text(
                                        text = "${evento.sedeAsociada} • ${evento.ubicacion}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = EventoDateUtils.formatDateRange(evento.fechaInicio, evento.fechaFin),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = DuocAzul
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckInDialog) {
        CheckInTurnoDialog(
            eventName = uiState.activeEventName,
            defaultMeta = uiState.activeEvent?.metaProspectos ?: 20,
            onDismiss = { showCheckInDialog = false },
            onConfirm = { meta ->
                showCheckInDialog = false
                onStartTurno(meta)
            }
        )
    }

    uiState.resumenCierreTurno?.let { resumen ->
        ResumenCierreTurnoDialog(
            resumen = resumen,
            onSyncNow = {
                onDismissResumenTurno()
                onSync()
            },
            onDismiss = onDismissResumenTurno
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
            .verticalScroll(rememberScrollState())
    ) {
        // Encabezado institucional
        DuocHeader(
            title = "¡Hola, $userName!",
            subtitle = roleName,
            showConnectivityStatus = false,
            trailingAction = {
                TextButton(onClick = onLogout) {
                    Text("Salir", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta de Evento Activo, Selector y Estado de Red
            DuocCard(
                elevation = 1.dp,
                contentPadding = 14.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "EVENTO ACTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.appMode == AppMode.LOCAL_DEMO) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DuocAmarillo.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "Demo Local",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DuocAzul,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = uiState.activeEventName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = DuocAzul
                        )
                        uiState.activeEvent?.let { evt ->
                            Text(
                                text = "${evt.tipo.displayName} • ${evt.sedeAsociada}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }

                    ConnectivityBadge(
                        isOnline = uiState.isOnline,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.activeEvent?.ubicacion ?: "Sede asignada",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { showEventSelector = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Cambiar evento",
                            fontWeight = FontWeight.Bold,
                            color = DuocAzul
                        )
                    }
                }
            }

            // Tarjeta de Jornada / Check-in de Turno y Progreso hacia Meta
            TurnoJornadaCard(
                uiState = uiState,
                onStartTurnoClick = { showCheckInDialog = true },
                onFinishTurnoClick = onFinishTurno
            )

            // Tarjeta de Acción Principal (Diferenciada por Rol)
            if (uiState.isCaptador) {
                DuocCard(
                    elevation = 4.dp,
                    contentPadding = 20.dp
                ) {
                    Text(
                        text = "📝 Captura en Terreno",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Abre el cupón oficial de admisión de Duoc UC para registrar los datos del postulante con validación de RUT en vivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(20.dp))
                    DuocButton(
                        text = "Iniciar Nueva Captura",
                        onClick = onNewCapture
                    )
                }
            } else {
                DuocCard(
                    elevation = 4.dp,
                    contentPadding = 20.dp
                ) {
                    Text(
                        text = "📊 Panel Administrativo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Visualiza el consolidado de prospectos, escuelas más solicitadas, promedios de edad y estadísticas del equipo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(20.dp))
                    DuocButton(
                        text = "Ir al Dashboard",
                        onClick = onDashboard
                    )
                }
            }

            // Métricas Rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DuocCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp
                ) {
                    Text(
                        text = "${uiState.totalCapturedToday}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Text(
                        text = if (uiState.isCaptador) "Mis capturas hoy" else "Total registradas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                DuocCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = 14.dp
                ) {
                    val syncColor = when {
                        uiState.syncErrorCount > 0 -> Color(0xFFC62828)
                        uiState.pendingSyncCount == 0 -> ExitoVerde
                        else -> DuocAmarillo
                    }
                    Text(
                        text = if (uiState.pendingSyncCount == 0) "Al día" else "${uiState.pendingSyncCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = syncColor
                    )
                    Text(
                        text = if (uiState.syncErrorCount > 0) "Con error de envío" else "Sincronización",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.syncErrorCount > 0) Color(0xFFC62828) else Color.Gray
                    )
                }
            }

            // Botón de Sincronización Offline-First (Demo Local o Gateway -> ms-formulario)
            DuocCard(elevation = 2.dp, contentPadding = 14.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.appMode == AppMode.LOCAL_DEMO) {
                                "Sincronización Demo Local"
                            } else {
                                "Sincronización Remota"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DuocAzul
                        )
                        Text(
                            text = uiState.syncMessage ?: if (uiState.syncErrorCount > 0) {
                                "${uiState.syncErrorCount} registro(s) con error; toca Sincronizar para reintentar"
                            } else if (uiState.pendingSyncCount > 0) {
                                if (uiState.appMode == AppMode.LOCAL_DEMO) {
                                    "${uiState.pendingSyncCount} registro(s) listos para simular envío local"
                                } else {
                                    "${uiState.pendingSyncCount} registro(s) listos para enviar al servidor"
                                }
                            } else {
                                "Sin registros pendientes por subir"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.syncErrorCount > 0 && uiState.syncMessage == null) {
                                Color(0xFFC62828)
                            } else {
                                Color.DarkGray
                            }
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSync,
                        enabled = !uiState.isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DuocAzul,
                            contentColor = Color.White
                        )
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
                if (uiState.appMode == AppMode.LOCAL_DEMO) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = onResetDemoData,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Poblar / Resetear datos demo (RUTs Módulo 11)",
                            style = MaterialTheme.typography.labelMedium,
                            color = DuocAzul,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Opciones Secundarias
            Text(
                text = "Accesos Rápidos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DuocAzul,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Accesos rápidos según rol
            if (uiState.isCaptador) {
                QuickActionCard(
                    title = "Dashboard de Resultados",
                    description = "Revisa cuántas encuestas llevas y el desglose de intereses",
                    iconText = "📊",
                    onClick = onDashboard
                )
            } else {
                QuickActionCard(
                    title = "Gestión de Eventos y Ferias",
                    description = "Crea, edita, activa o archiva ferias y visitas a colegios",
                    iconText = "🗓️",
                    onClick = onManageEventos
                )
                QuickActionCard(
                    title = "Formulario de Captura",
                    description = "Apoya en terreno ingresando prospectos de forma directa",
                    iconText = "📝",
                    onClick = onNewCapture
                )
            }

            QuickActionCard(
                title = "¿Cómo funciona?",
                description = "Guía paso a paso sobre el flujo de captura y modo offline",
                iconText = "ℹ️",
                onClick = onHelp
            )

            Spacer(Modifier.height(8.dp))

            // Botón de Cerrar Sesión
            DuocOutlinedButton(
                text = "Cerrar sesión",
                onClick = onLogout
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TurnoJornadaCard(
    uiState: InicioUiState,
    onStartTurnoClick: () -> Unit,
    onFinishTurnoClick: () -> Unit
) {
    val turno = uiState.activeTurno
    DuocCard(elevation = 2.dp, contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (turno != null) "TURNO EN CURSO" else "JORNADA / TURNO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (turno != null) ExitoVerde else Color.Gray
                    )
                    if (turno != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ExitoVerde.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "Inicio ${EventoDateUtils.formatTime(turno.inicioEpoch)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExitoVerde,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (turno != null) {
                        "${uiState.turnoCapturedCount} / ${turno.metaTurno} capturas en este turno"
                    } else {
                        "${uiState.eventCapturedCount} / ${uiState.currentGoal} capturas en el evento"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = DuocAzul
                )
            }

            if (turno != null) {
                OutlinedButton(
                    onClick = onFinishTurnoClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DuocAzul),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Finalizar", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onStartTurnoClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DuocAmarillo,
                        contentColor = DuocAzul
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Check-in", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { uiState.progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (uiState.progressFraction >= 1f) ExitoVerde else DuocAzul,
            trackColor = Color(0xFFE2E8F0)
        )
        if (turno != null && turno.eventoId != uiState.activeEventId) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Turno abierto en evento ${turno.eventoId}. Finaliza el turno para cerrar su conteo.",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFB45309)
            )
        }
    }
}

@Composable
private fun CheckInTurnoDialog(
    eventName: String,
    defaultMeta: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var metaText by remember(defaultMeta) { mutableStateOf(defaultMeta.coerceAtLeast(1).toString()) }
    val parsedMeta = metaText.toIntOrNull()
    val metaValida = parsedMeta != null && parsedMeta > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check-in de Turno", fontWeight = FontWeight.Bold, color = DuocAzul) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Vas a iniciar tu jornada de captura en:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Text(
                    text = eventName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                DuocTextField(
                    value = metaText,
                    onValueChange = { metaText = it.filter(Char::isDigit) },
                    label = "Meta de capturas del turno",
                    keyboardType = KeyboardType.Number,
                    errorMessage = if (!metaValida) "Ingresa una meta mayor a 0" else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { parsedMeta?.let(onConfirm) },
                enabled = metaValida,
                colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
            ) {
                Text("Iniciar Turno")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = DuocAzul)
            }
        }
    )
}

@Composable
private fun ResumenCierreTurnoDialog(
    resumen: ResumenCierreTurno,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Resumen de Cierre de Turno", fontWeight = FontWeight.Bold, color = DuocAzul)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = resumen.eventoNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Text(
                    text = "Duración: ${EventoDateUtils.formatDuration(resumen.duracionMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                Text(
                    text = "Capturas logradas: ${resumen.totalCapturados} / ${resumen.metaTurno}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (resumen.totalCapturados >= resumen.metaTurno) ExitoVerde else DuocAzul
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Distribución por nivel de interés",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                NivelInteres.entries.forEach { nivel ->
                    val count = resumen.distribucionInteres[nivel] ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nivel.displayName, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        Text("$count", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = DuocAzul)
                    }
                }
                if (resumen.pendientesSync > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DuocAmarillo.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "Recordatorio: quedan ${resumen.pendientesSync} cupón(es) pendiente(s) de sincronizar.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = DuocAzul,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (resumen.pendientesSync > 0) {
                Button(
                    onClick = onSyncNow,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
                ) {
                    Text("Sincronizar ahora")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DuocAzul)
                ) {
                    Text("Cerrar")
                }
            }
        },
        dismissButton = {
            if (resumen.pendientesSync > 0) {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar", color = DuocAzul)
                }
            }
        }
    )
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    iconText: String,
    onClick: () -> Unit
) {
    DuocCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 2.dp,
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color(0xFFF1F5F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = iconText, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = Color.Gray)
        }
    }
}

@Preview(name = "Pantalla Inicio - Modo Claro", showBackground = true, showSystemUi = true)
@Composable
fun InicioScreenPreview() {
    Captura_Digital_v2Theme {
        InicioScreen(
            uiState = InicioUiState(
                user = User(
                    id = 1,
                    name = "Juan Pérez",
                    email = "juan.perez@duocuc.cl",
                    role = UserRole.CAPTADOR
                ),
                activeEventName = "Feria Vocacional Duoc UC 2026",
                activeEvent = Evento(
                    id = "EVT-DUOC-2026",
                    nombre = "Feria Vocacional Duoc UC 2026",
                    tipo = TipoEvento.FERIA,
                    sedeAsociada = "Viña del Mar",
                    ubicacion = "Duoc UC Sede Viña del Mar",
                    fechaInicio = 1776540000000L,
                    fechaFin = 1776626400000L,
                    metaProspectos = 40
                ),
                activeTurno = TurnoEvento(
                    id = "T-1",
                    eventoId = "EVT-DUOC-2026",
                    captadorId = 1,
                    inicioEpoch = 1776543210000L,
                    metaTurno = 25
                ),
                turnoCapturedCount = 10,
                pendingSyncCount = 2,
                totalCapturedToday = 15,
                isOnline = true
            ),
            onNewCapture = {},
            onDashboard = {},
            onHelp = {},
            onLogout = {}
        )
    }
}

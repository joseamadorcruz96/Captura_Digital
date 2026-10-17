package com.therionlabs.captura_digital_v2.ui.formulario

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.data.model.CatalogoDuoc
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader
import com.therionlabs.captura_digital_v2.ui.components.DuocOutlinedButton
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ErrorRojo
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import com.therionlabs.captura_digital_v2.ui.theme.FondoClaro

@Composable
fun FormularioRoute(
    viewModel: FormularioViewModel = viewModel(),
    captadorId: Int = 2,
    eventoId: String? = "EVT-DUOC-2026",
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FormularioScreen(
        uiState = uiState,
        onConsentimientoChange = viewModel::onConsentimientoChange,
        onRutChange = viewModel::onRutChange,
        onNombresChange = viewModel::onNombresChange,
        onApellidosChange = viewModel::onApellidosChange,
        onGeneroChange = viewModel::onGeneroChange,
        onFechaNacimientoChange = viewModel::onFechaNacimientoChange,
        onComunaResidenciaChange = viewModel::onComunaResidenciaChange,
        onCelularChange = viewModel::onCelularChange,
        onEmailChange = viewModel::onEmailChange,
        onSituacionAcademicaChange = viewModel::onSituacionAcademicaChange,
        onSituacionAcademicaOtroChange = viewModel::onSituacionAcademicaOtroChange,
        onEstablecimientoChange = viewModel::onEstablecimientoChange,
        onComunaEstablecimientoChange = viewModel::onComunaEstablecimientoChange,
        onJornadaChange = viewModel::onJornadaChange,
        onSedeChange = viewModel::onSedeChange,
        onNivelInteresChange = viewModel::onNivelInteresChange,
        onPerfilMotivacionalChange = viewModel::onPerfilMotivacionalChange,
        onEscuelaSeleccionadaChange = viewModel::onEscuelaSeleccionadaChange,
        onCarrera1Change = viewModel::onCarrera1Change,
        onCarrera2Change = viewModel::onCarrera2Change,
        onCarrera3Change = viewModel::onCarrera3Change,
        onGuardar = { viewModel.guardar(captadorId, eventoId) },
        onReset = viewModel::resetFormulario,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioScreen(
    uiState: FormularioUiState,
    onConsentimientoChange: (Boolean) -> Unit,
    onRutChange: (String) -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onGeneroChange: (Genero) -> Unit,
    onFechaNacimientoChange: (String) -> Unit,
    onComunaResidenciaChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSituacionAcademicaChange: (SituacionAcademica) -> Unit,
    onSituacionAcademicaOtroChange: (String) -> Unit,
    onEstablecimientoChange: (String) -> Unit,
    onComunaEstablecimientoChange: (String) -> Unit,
    onJornadaChange: (Jornada) -> Unit,
    onSedeChange: (String) -> Unit,
    onNivelInteresChange: (NivelInteres) -> Unit,
    onPerfilMotivacionalChange: (PerfilMotivacional) -> Unit,
    onEscuelaSeleccionadaChange: (String) -> Unit,
    onCarrera1Change: (String) -> Unit,
    onCarrera2Change: (String) -> Unit,
    onCarrera3Change: (String) -> Unit,
    onGuardar: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro)
    ) {
        DuocHeader(
            title = "Cupón de Admisión",
            subtitle = "Captura digital de prospecto Duoc UC",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SECCIÓN 1: CONSENTIMIENTO INFORMADO
            DuocCard(elevation = 2.dp) {
                Text(
                    text = "1. Consentimiento Legal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = uiState.consentimiento,
                        onCheckedChange = onConsentimientoChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = DuocAzul,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Sí, autorizo el tratamiento de mis datos personales para contacto y admisión por Duoc UC (Ley N° 19.628 / Ley N° 21.719).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
                if (uiState.consentimientoError != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.consentimientoError,
                        color = ErrorRojo,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            // SECCIÓN 2: IDENTIFICACIÓN Y CONTACTO
            DuocCard(elevation = 2.dp) {
                Text(
                    text = "2. Datos Personales y Contacto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(12.dp))

                DuocTextField(
                    value = uiState.rut,
                    onValueChange = onRutChange,
                    label = "RUT del postulante",
                    placeholder = "12.345.678-5",
                    isError = uiState.rutError != null,
                    errorMessage = uiState.rutError
                )
                Spacer(Modifier.height(10.dp))

                DuocTextField(
                    value = uiState.nombres,
                    onValueChange = onNombresChange,
                    label = "Nombres",
                    isError = uiState.nombresError != null,
                    errorMessage = uiState.nombresError
                )
                Spacer(Modifier.height(10.dp))

                DuocTextField(
                    value = uiState.apellidos,
                    onValueChange = onApellidosChange,
                    label = "Apellidos",
                    isError = uiState.apellidosError != null,
                    errorMessage = uiState.apellidosError
                )
                Spacer(Modifier.height(12.dp))

                // Selector de Género
                Text("Género", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Genero.values().forEach { g ->
                        FilterChip(
                            selected = uiState.genero == g,
                            onClick = { onGeneroChange(g) },
                            label = { Text(g.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuocAzul,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))

                DuocTextField(
                    value = uiState.fechaNacimiento,
                    onValueChange = onFechaNacimientoChange,
                    label = "Fecha de Nacimiento (Opcional)",
                    placeholder = "DD/MM/AAAA",
                    keyboardType = KeyboardType.Number
                )
                Spacer(Modifier.height(10.dp))

                DuocTextField(
                    value = uiState.comunaResidencia,
                    onValueChange = onComunaResidenciaChange,
                    label = "Comuna de Residencia",
                    placeholder = "Ej: Viña del Mar, Quilpué",
                    isError = uiState.comunaResidenciaError != null,
                    errorMessage = uiState.comunaResidenciaError
                )
                Spacer(Modifier.height(10.dp))

                DuocTextField(
                    value = uiState.celular,
                    onValueChange = onCelularChange,
                    label = "Celular móvil (+56 9)",
                    placeholder = "8 o 9 dígitos",
                    keyboardType = KeyboardType.Phone,
                    isError = uiState.celularError != null,
                    errorMessage = uiState.celularError
                )
                Spacer(Modifier.height(10.dp))

                DuocTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = "Correo Electrónico",
                    placeholder = "ejemplo@correo.com",
                    keyboardType = KeyboardType.Email,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError
                )
            }

            // SECCIÓN 3: ANTECEDENTES ESCOLARES Y JORNADA
            DuocCard(elevation = 2.dp) {
                Text(
                    text = "3. Antecedentes Escolares y Jornada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(12.dp))

                Text("Situación Académica Actual", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SituacionAcademica.values().forEach { sit ->
                        FilterChip(
                            selected = uiState.situacionAcademica == sit,
                            onClick = { onSituacionAcademicaChange(sit) },
                            label = { Text(sit.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuocAzul,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                if (uiState.situacionAcademica == SituacionAcademica.OTRA) {
                    Spacer(Modifier.height(8.dp))
                    DuocTextField(
                        value = uiState.situacionAcademicaOtro,
                        onValueChange = onSituacionAcademicaOtroChange,
                        label = "Especifique situación académica"
                    )
                }

                Spacer(Modifier.height(10.dp))
                DuocTextField(
                    value = uiState.establecimientoEducacional,
                    onValueChange = onEstablecimientoChange,
                    label = "Establecimiento Educacional",
                    placeholder = "Ej: Colegio Rubén Castro",
                    isError = uiState.establecimientoError != null,
                    errorMessage = uiState.establecimientoError
                )

                Spacer(Modifier.height(10.dp))
                DuocTextField(
                    value = uiState.comunaEstablecimiento,
                    onValueChange = onComunaEstablecimientoChange,
                    label = "Comuna del Colegio (Opcional)",
                    placeholder = "Si es distinta a su comuna"
                )

                Spacer(Modifier.height(12.dp))
                Text("Preferencia de Jornada", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Jornada.values().forEach { j ->
                        FilterChip(
                            selected = uiState.jornada == j,
                            onClick = { onJornadaChange(j) },
                            label = { Text(j.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuocAzul,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // SECCIÓN 4: SEDE Y PERFILAMIENTO
            DuocCard(elevation = 2.dp) {
                Text(
                    text = "4. Sede de Interés y Motivación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(12.dp))

                // Selector de Sede simplificado
                var expandedSede by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedSede,
                    onExpandedChange = { expandedSede = it }
                ) {
                    OutlinedTextField(
                        value = uiState.sedeInteres,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sede Duoc UC de Interés") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSede) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSede,
                        onDismissRequest = { expandedSede = false }
                    ) {
                        CatalogoDuoc.sedes.forEach { sede ->
                            DropdownMenuItem(
                                text = { Text("${sede.nombre} (${sede.region})") },
                                onClick = {
                                    onSedeChange(sede.nombre)
                                    expandedSede = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Nivel de Intención de Matrícula", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NivelInteres.values().forEach { nivel ->
                        FilterChip(
                            selected = uiState.nivelInteres == nivel,
                            onClick = { onNivelInteresChange(nivel) },
                            label = { Text(nivel.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DuocAmarillo,
                                selectedLabelColor = DuocAzul
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Motivación Principal", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PerfilMotivacional.values().forEach { perfil ->
                        val isSelected = uiState.perfilMotivacional == perfil
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFE8EFF9) else Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPerfilMotivacionalChange(perfil) }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) DuocAzul else Color.LightGray,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = perfil.titulo,
                                    fontWeight = FontWeight.Bold,
                                    color = DuocAzul,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = perfil.descripcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            // SECCIÓN 5: MATRIZ DE CARRERAS DE INTERÉS
            DuocCard(elevation = 2.dp) {
                Text(
                    text = "5. Preferencias de Carrera (1 a 3)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Selecciona una Escuela para filtrar el catálogo oficial de carreras.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(Modifier.height(12.dp))

                // Selector de Escuela
                var expandedEscuela by remember { mutableStateOf(false) }
                val escuelaActual = CatalogoDuoc.escuelas.find { it.id == uiState.escuelaSeleccionadaId }

                ExposedDropdownMenuBox(
                    expanded = expandedEscuela,
                    onExpandedChange = { expandedEscuela = it }
                ) {
                    OutlinedTextField(
                        value = escuelaActual?.nombre ?: "Seleccionar Escuela",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filtrar por Escuela Duoc") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscuela) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEscuela,
                        onDismissRequest = { expandedEscuela = false }
                    ) {
                        CatalogoDuoc.escuelas.forEach { esc ->
                            DropdownMenuItem(
                                text = { Text(esc.nombre) },
                                onClick = {
                                    onEscuelaSeleccionadaChange(esc.id)
                                    expandedEscuela = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                val carrerasDisponibles = CatalogoDuoc.getCarrerasByEscuela(uiState.escuelaSeleccionadaId)

                // Carrera Prioridad 1 (Obligatoria)
                Text("Prioridad 1 (Obligatoria) *", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                var expandedC1 by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedC1,
                    onExpandedChange = { expandedC1 = it }
                ) {
                    OutlinedTextField(
                        value = uiState.carreraPrioridad1.ifBlank { "Seleccionar primera opción" },
                        onValueChange = {},
                        readOnly = true,
                        isError = uiState.carrera1Error != null,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedC1) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedC1,
                        onDismissRequest = { expandedC1 = false }
                    ) {
                        carrerasDisponibles.forEach { car ->
                            DropdownMenuItem(
                                text = { Text("${car.nombre} (${car.nivel.displayName})") },
                                onClick = {
                                    onCarrera1Change(car.nombre)
                                    expandedC1 = false
                                }
                            )
                        }
                    }
                }
                if (uiState.carrera1Error != null) {
                    Text(uiState.carrera1Error, color = ErrorRojo, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(12.dp))

                // Carrera Prioridad 2 (Opcional)
                Text("Prioridad 2 (Opcional)", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                var expandedC2 by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedC2,
                    onExpandedChange = { expandedC2 = it }
                ) {
                    OutlinedTextField(
                        value = uiState.carreraPrioridad2.ifBlank { "Seleccionar segunda opción" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedC2) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedC2,
                        onDismissRequest = { expandedC2 = false }
                    ) {
                        carrerasDisponibles.forEach { car ->
                            DropdownMenuItem(
                                text = { Text("${car.nombre} (${car.nivel.displayName})") },
                                onClick = {
                                    onCarrera2Change(car.nombre)
                                    expandedC2 = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Carrera Prioridad 3 (Opcional)
                Text("Prioridad 3 (Opcional)", style = MaterialTheme.typography.labelMedium, color = DuocAzul, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                var expandedC3 by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedC3,
                    onExpandedChange = { expandedC3 = it }
                ) {
                    OutlinedTextField(
                        value = uiState.carreraPrioridad3.ifBlank { "Seleccionar tercera opción" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedC3) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedC3,
                        onDismissRequest = { expandedC3 = false }
                    ) {
                        carrerasDisponibles.forEach { car ->
                            DropdownMenuItem(
                                text = { Text("${car.nombre} (${car.nivel.displayName})") },
                                onClick = {
                                    onCarrera3Change(car.nombre)
                                    expandedC3 = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // BOTÓN GUARDAR
            DuocButton(
                text = "Guardar Prospecto",
                onClick = onGuardar
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN DE GUARDADO EXITOSO
    if (uiState.saveSuccess) {
        AlertDialog(
            onDismissRequest = onReset,
            confirmButton = {
                DuocButton(
                    text = "Capturar Siguiente",
                    onClick = onReset
                )
            },
            dismissButton = {
                TextButton(onClick = onBack) {
                    Text("Volver al Inicio", color = DuocAzul)
                }
            },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = ExitoVerde, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    }
                }
            },
            title = {
                Text(
                    text = "¡Prospecto Registrado!",
                    fontWeight = FontWeight.Bold,
                    color = DuocAzul
                )
            },
            text = {
                Column {
                    Text("Los datos fueron validados y almacenados localmente de forma segura en modo Offline.")
                    Spacer(Modifier.height(8.dp))
                    Text("• Postulante: ${uiState.savedProspectoNombre}", fontWeight = FontWeight.SemiBold)
                    Text("• RUT validado: ${uiState.savedProspectoRut}", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}
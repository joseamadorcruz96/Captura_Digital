package com.therionlabs.captura_digital_v2.ui.formularioModal.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.data.model.CatalogoDuoc
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.formularioModal.StepFormUiState
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul

/**
 * Flujo compacto Fast-Track (Modo Captura Express) para ferias con alta demanda.
 * Captura los campos mínimos esenciales, rellena opcionales con defaults seguros al guardar
 * y marca el cupón como [EstadoCompletitud.EXPRESS] (sincronizable).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressCaptureForm(
    uiState: StepFormUiState,
    onConsentimientoChange: (Boolean) -> Unit,
    onFirmaChange: (String?) -> Unit,
    onScanCedulaClick: () -> Unit,
    onRutChange: (String) -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSedeChange: (String) -> Unit,
    onEscuelaSeleccionadaChange: (String) -> Unit,
    onCarrera1Change: (String) -> Unit,
    onToggleAttachLocation: (Boolean) -> Unit,
    onVerFichaDuplicada: () -> Unit,
    onCargarDuplicadoParaActualizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = DuocAmarillo.copy(alpha = 0.2f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Modo Express: captura rápida con campos esenciales. El cupón quedará marcado como Express / Incompleto para enriquecer después.",
                style = MaterialTheme.typography.bodySmall,
                color = DuocAzul,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp)
            )
        }

        // Bloque 1: Consentimiento + Firma + Identidad + Escáner
        DuocCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            elevation = 2.dp
        ) {
            Text(
                text = "1. Consentimiento e Identificación",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onConsentimientoChange(!uiState.consentimiento) }
            ) {
                Checkbox(
                    checked = uiState.consentimiento,
                    onCheckedChange = onConsentimientoChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Autorizo tratamiento de datos personales (Ley N° 19.628 / 21.719).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (uiState.consentimientoError != null) {
                Text(
                    text = uiState.consentimientoError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
            SignaturePad(
                firmaBase64 = uiState.firmaDigitalBase64,
                firmaTimestampEpoch = uiState.firmaTimestampEpoch,
                onFirmaChange = onFirmaChange,
                errorMessage = uiState.firmaError
            )

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onScanCedulaClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escanear Cédula / QR", fontWeight = FontWeight.Bold)
            }

            if (uiState.scannerFeedbackMessage != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = uiState.scannerFeedbackMessage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))
            DuocTextField(
                value = uiState.rut,
                onValueChange = onRutChange,
                label = "RUT del postulante *",
                placeholder = "12.345.678-5",
                isError = uiState.rutError != null,
                errorMessage = uiState.rutError
            )

            uiState.duplicateProspecto?.let { dup ->
                Spacer(Modifier.height(8.dp))
                DuplicateRutAlertCard(
                    prospecto = dup,
                    onVerFicha = onVerFichaDuplicada,
                    onCargarParaActualizar = onCargarDuplicadoParaActualizar
                )
            }

            Spacer(Modifier.height(10.dp))
            DuocTextField(
                value = uiState.nombres,
                onValueChange = onNombresChange,
                label = "Nombres *",
                isError = uiState.nombresError != null,
                errorMessage = uiState.nombresError
            )
            Spacer(Modifier.height(10.dp))
            DuocTextField(
                value = uiState.apellidos,
                onValueChange = onApellidosChange,
                label = "Apellidos *",
                isError = uiState.apellidosError != null,
                errorMessage = uiState.apellidosError
            )
        }

        // Bloque 2: Contacto + Sede + Carrera 1 + Ubicación
        DuocCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            elevation = 2.dp
        ) {
            Text(
                text = "2. Contacto y Carrera Principal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            DuocTextField(
                value = uiState.celular,
                onValueChange = onCelularChange,
                label = "Celular (+56 9)",
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

            Spacer(Modifier.height(10.dp))
            var expandedSede by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedSede,
                onExpandedChange = { expandedSede = it }
            ) {
                OutlinedTextField(
                    value = uiState.sedeInteres ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Seleccionar sede Duoc UC") },
                    label = { Text("Sede Duoc UC de Interés *") },
                    isError = uiState.sedeError != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSede) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
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
            if (uiState.sedeError != null) {
                Text(
                    text = uiState.sedeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
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
                    label = { Text("Escuela Duoc UC") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEscuela) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
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

            Spacer(Modifier.height(10.dp))
            var expandedCarrera by remember { mutableStateOf(false) }
            val carrerasDisponibles = CatalogoDuoc.getCarrerasByEscuela(uiState.escuelaSeleccionadaId)
            ExposedDropdownMenuBox(
                expanded = expandedCarrera,
                onExpandedChange = { expandedCarrera = it }
            ) {
                OutlinedTextField(
                    value = uiState.carreraPrioridad1.ifBlank { "Seleccionar carrera principal" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Carrera Prioridad 1 *") },
                    isError = uiState.carrera1Error != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCarrera) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                )
                ExposedDropdownMenu(
                    expanded = expandedCarrera,
                    onDismissRequest = { expandedCarrera = false }
                ) {
                    carrerasDisponibles.forEach { car ->
                        DropdownMenuItem(
                            text = { Text("${car.nombre} (${car.nivel.displayName})") },
                            onClick = {
                                onCarrera1Change(car.nombre)
                                expandedCarrera = false
                            }
                        )
                    }
                }
            }
            if (uiState.carrera1Error != null) {
                Text(
                    text = uiState.carrera1Error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Adjuntar ubicación aproximada",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = uiState.locationStatusMessage ?: "Opcional para cobertura en terreno",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.attachLocation,
                    onCheckedChange = onToggleAttachLocation
                )
            }
        }
    }
}

@Composable
fun DuplicateRutAlertCard(
    prospecto: Prospecto,
    onVerFicha: () -> Unit,
    onCargarParaActualizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = DuocAmarillo.copy(alpha = 0.22f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "RUT ya registrado localmente",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            Text(
                text = "${prospecto.nombreCompleto} · ${prospecto.carreraPrioridad1}",
                style = MaterialTheme.typography.bodySmall,
                color = DuocAzul
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onVerFicha,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ver ficha existente", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onCargarParaActualizar,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cargar para actualizar", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

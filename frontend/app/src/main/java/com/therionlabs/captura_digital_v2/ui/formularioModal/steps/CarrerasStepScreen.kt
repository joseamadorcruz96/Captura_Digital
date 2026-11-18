package com.therionlabs.captura_digital_v2.ui.formularioModal.steps

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.data.model.Carrera
import com.therionlabs.captura_digital_v2.data.model.CatalogoDuoc
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Paso 6: Matriz de preferencias de carrera (1 a 3 prioridades).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarrerasStepScreen(
    escuelaSeleccionadaId: String,
    carreraPrioridad1: String,
    carrera1Error: String?,
    carreraPrioridad2: String,
    carreraPrioridad3: String,
    onEscuelaSeleccionadaChange: (String) -> Unit,
    onCarrera1Change: (String) -> Unit,
    onCarrera2Change: (String) -> Unit,
    onCarrera3Change: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DuocCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Text(
            text = "6. Preferencias de Carrera (1 a 3)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Selecciona una Escuela para filtrar el catálogo oficial de carreras.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // Selector de Escuela
        var expandedEscuela by remember { mutableStateOf(false) }
        val escuelaActual = CatalogoDuoc.escuelas.find { it.id == escuelaSeleccionadaId }

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

        Spacer(Modifier.height(12.dp))
        val carrerasDisponibles = CatalogoDuoc.getCarrerasByEscuela(escuelaSeleccionadaId)

        CarreraSelectorDropdown(
            titulo = "Prioridad 1 (Obligatoria) *",
            valorActual = carreraPrioridad1,
            placeholder = "Seleccionar primera opción",
            carrerasDisponibles = carrerasDisponibles,
            errorMessage = carrera1Error,
            onSelected = onCarrera1Change
        )

        Spacer(Modifier.height(12.dp))

        CarreraSelectorDropdown(
            titulo = "Prioridad 2 (Opcional)",
            valorActual = carreraPrioridad2,
            placeholder = "Seleccionar segunda opción",
            carrerasDisponibles = carrerasDisponibles,
            errorMessage = null,
            onSelected = onCarrera2Change
        )

        Spacer(Modifier.height(12.dp))

        CarreraSelectorDropdown(
            titulo = "Prioridad 3 (Opcional)",
            valorActual = carreraPrioridad3,
            placeholder = "Seleccionar tercera opción",
            carrerasDisponibles = carrerasDisponibles,
            errorMessage = null,
            onSelected = onCarrera3Change
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarreraSelectorDropdown(
    titulo: String,
    valorActual: String,
    placeholder: String,
    carrerasDisponibles: List<Carrera>,
    errorMessage: String?,
    onSelected: (String) -> Unit
) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
    Spacer(Modifier.height(4.dp))

    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = valorActual.ifBlank { placeholder },
            onValueChange = {},
            readOnly = true,
            isError = errorMessage != null,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            carrerasDisponibles.forEach { car ->
                DropdownMenuItem(
                    text = { Text("${car.nombre} (${car.nivel.displayName})") },
                    onClick = {
                        onSelected(car.nombre)
                        expanded = false
                    }
                )
            }
        }
    }

    if (errorMessage != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(name = "Paso 6: Carreras (Claro)", showBackground = true)
@Composable
fun CarrerasStepPreview() {
    Captura_Digital_v2Theme {
        Surface {
            CarrerasStepScreen(
                escuelaSeleccionadaId = "INFOR",
                carreraPrioridad1 = "Ingeniería en Informática",
                carrera1Error = null,
                carreraPrioridad2 = "Analista Programador",
                carreraPrioridad3 = "",
                onEscuelaSeleccionadaChange = {},
                onCarrera1Change = {},
                onCarrera2Change = {},
                onCarrera3Change = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Paso 6: Carreras Error (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CarrerasStepDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            CarrerasStepScreen(
                escuelaSeleccionadaId = "INFOR",
                carreraPrioridad1 = "",
                carrera1Error = "Seleccione al menos una carrera como primera preferencia",
                carreraPrioridad2 = "",
                carreraPrioridad3 = "",
                onEscuelaSeleccionadaChange = {},
                onCarrera1Change = {},
                onCarrera2Change = {},
                onCarrera3Change = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

package com.therionlabs.captura_digital_v2.ui.formularioModal.steps

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.data.local.EstablecimientoItem
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.formularioModal.components.AutocompleteTextField
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Paso 4: Antecedentes escolares (con autocompletado asistido) y preferencia de jornada.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AntecedentesStepScreen(
    situacionAcademica: SituacionAcademica,
    situacionAcademicaOtro: String,
    establecimientoEducacional: String,
    establecimientoError: String?,
    comunaEstablecimiento: String,
    jornada: Jornada,
    onSituacionAcademicaChange: (SituacionAcademica) -> Unit,
    onSituacionAcademicaOtroChange: (String) -> Unit,
    onEstablecimientoChange: (String) -> Unit,
    onComunaEstablecimientoChange: (String) -> Unit,
    onJornadaChange: (Jornada) -> Unit,
    modifier: Modifier = Modifier,
    establecimientoSuggestions: List<EstablecimientoItem> = emptyList(),
    onSelectEstablecimientoSuggestion: (EstablecimientoItem) -> Unit = { onEstablecimientoChange(it.nombre) },
    comunaEstablecimientoSuggestions: List<String> = emptyList(),
    onSelectComunaEstablecimientoSuggestion: (String) -> Unit = onComunaEstablecimientoChange
) {
    DuocCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Text(
            text = "4. Antecedentes Escolares y Jornada",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Situación Académica Actual",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SituacionAcademica.entries.forEach { sit ->
                FilterChip(
                    selected = situacionAcademica == sit,
                    onClick = { onSituacionAcademicaChange(sit) },
                    label = { Text(sit.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (situacionAcademica == SituacionAcademica.OTRA) {
            Spacer(Modifier.height(8.dp))
            DuocTextField(
                value = situacionAcademicaOtro,
                onValueChange = onSituacionAcademicaOtroChange,
                label = "Especifique situación académica"
            )
        }

        Spacer(Modifier.height(10.dp))
        AutocompleteTextField(
            value = establecimientoEducacional,
            onValueChange = onEstablecimientoChange,
            suggestions = establecimientoSuggestions,
            itemPrimaryText = { it.nombre },
            itemSecondaryText = { "${it.comuna}${if (it.region.isNotBlank()) " • ${it.region}" else ""}" },
            onSuggestionSelected = onSelectEstablecimientoSuggestion,
            label = "Establecimiento Educacional",
            placeholder = "Ej: Colegio Rubén Castro",
            isError = establecimientoError != null,
            errorMessage = establecimientoError
        )

        Spacer(Modifier.height(10.dp))
        AutocompleteTextField(
            value = comunaEstablecimiento,
            onValueChange = onComunaEstablecimientoChange,
            suggestions = comunaEstablecimientoSuggestions,
            itemPrimaryText = { it },
            onSuggestionSelected = onSelectComunaEstablecimientoSuggestion,
            label = "Comuna del Colegio (Opcional)",
            placeholder = "Si es distinta a su comuna"
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Preferencia de Jornada",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Jornada.entries.forEach { j ->
                FilterChip(
                    selected = jornada == j,
                    onClick = { onJornadaChange(j) },
                    label = { Text(j.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}

@Preview(name = "Paso 4: Antecedentes (Claro)", showBackground = true)
@Composable
fun AntecedentesStepPreview() {
    Captura_Digital_v2Theme {
        Surface {
            AntecedentesStepScreen(
                situacionAcademica = SituacionAcademica.OTRA,
                situacionAcademicaOtro = "Estudiante técnico",
                establecimientoEducacional = "Colegio Rubén Castro",
                establecimientoError = null,
                comunaEstablecimiento = "Viña del Mar",
                jornada = Jornada.DIURNA,
                onSituacionAcademicaChange = {},
                onSituacionAcademicaOtroChange = {},
                onEstablecimientoChange = {},
                onComunaEstablecimientoChange = {},
                onJornadaChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Paso 4: Antecedentes (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AntecedentesStepDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            AntecedentesStepScreen(
                situacionAcademica = SituacionAcademica.CUARTO_MEDIO,
                situacionAcademicaOtro = "",
                establecimientoEducacional = "",
                establecimientoError = "El colegio o liceo de origen es obligatorio",
                comunaEstablecimiento = "",
                jornada = Jornada.VESPERTINA,
                onSituacionAcademicaChange = {},
                onSituacionAcademicaOtroChange = {},
                onEstablecimientoChange = {},
                onComunaEstablecimientoChange = {},
                onJornadaChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

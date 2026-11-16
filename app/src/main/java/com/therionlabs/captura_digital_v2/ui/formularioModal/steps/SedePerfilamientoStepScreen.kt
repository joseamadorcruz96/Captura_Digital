package com.therionlabs.captura_digital_v2.ui.formularioModal.steps

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.therionlabs.captura_digital_v2.data.model.CatalogoDuoc
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Paso 5: Sede Duoc UC de interés, intención de matrícula y perfil motivacional.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SedePerfilamientoStepScreen(
    sedeInteres: String,
    nivelInteres: NivelInteres,
    perfilMotivacional: PerfilMotivacional,
    onSedeChange: (String) -> Unit,
    onNivelInteresChange: (NivelInteres) -> Unit,
    onPerfilMotivacionalChange: (PerfilMotivacional) -> Unit,
    modifier: Modifier = Modifier
) {
    DuocCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Text(
            text = "5. Sede de Interés y Motivación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        var expandedSede by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedSede,
            onExpandedChange = { expandedSede = it }
        ) {
            OutlinedTextField(
                value = sedeInteres,
                onValueChange = {},
                readOnly = true,
                label = { Text("Sede Duoc UC de Interés") },
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

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Nivel de Intención de Matrícula",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NivelInteres.entries.forEach { nivel ->
                FilterChip(
                    selected = nivelInteres == nivel,
                    onClick = { onNivelInteresChange(nivel) },
                    label = { Text(nivel.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Motivación Principal",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            PerfilMotivacional.entries.forEach { perfil ->
                val isSelected = perfilMotivacional == perfil
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPerfilMotivacionalChange(perfil) }
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = perfil.titulo,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = perfil.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Paso 5: Sede y Perfilamiento (Claro)", showBackground = true)
@Composable
fun SedePerfilamientoStepPreview() {
    Captura_Digital_v2Theme {
        Surface {
            SedePerfilamientoStepScreen(
                sedeInteres = "Viña del Mar",
                nivelInteres = NivelInteres.ALTO,
                perfilMotivacional = PerfilMotivacional.VOCACIONAL,
                onSedeChange = {},
                onNivelInteresChange = {},
                onPerfilMotivacionalChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Paso 5: Sede y Perfilamiento (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SedePerfilamientoStepDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            SedePerfilamientoStepScreen(
                sedeInteres = "San Joaquín",
                nivelInteres = NivelInteres.MEDIO,
                perfilMotivacional = PerfilMotivacional.INVERSION,
                onSedeChange = {},
                onNivelInteresChange = {},
                onPerfilMotivacionalChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

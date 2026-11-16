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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Paso 2: Identificación personal del postulante.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DatosPersonalesStepScreen(
    rut: String,
    rutError: String?,
    nombres: String,
    nombresError: String?,
    apellidos: String,
    apellidosError: String?,
    genero: Genero,
    fechaNacimiento: String,
    onRutChange: (String) -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onGeneroChange: (Genero) -> Unit,
    onFechaNacimientoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DuocCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Text(
            text = "2. Datos Personales",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))

        DuocTextField(
            value = rut,
            onValueChange = onRutChange,
            label = "RUT del postulante",
            placeholder = "12.345.678-5",
            isError = rutError != null,
            errorMessage = rutError
        )
        Spacer(Modifier.height(10.dp))

        DuocTextField(
            value = nombres,
            onValueChange = onNombresChange,
            label = "Nombres",
            isError = nombresError != null,
            errorMessage = nombresError
        )
        Spacer(Modifier.height(10.dp))

        DuocTextField(
            value = apellidos,
            onValueChange = onApellidosChange,
            label = "Apellidos",
            isError = apellidosError != null,
            errorMessage = apellidosError
        )
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Género",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Genero.entries.forEach { g ->
                FilterChip(
                    selected = genero == g,
                    onClick = { onGeneroChange(g) },
                    label = { Text(g.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        DuocTextField(
            value = fechaNacimiento,
            onValueChange = onFechaNacimientoChange,
            label = "Fecha de Nacimiento (Opcional)",
            placeholder = "DD/MM/AAAA",
            keyboardType = KeyboardType.Number
        )
    }
}

@Preview(name = "Paso 2: Datos Personales (Claro)", showBackground = true)
@Composable
fun DatosPersonalesStepPreview() {
    Captura_Digital_v2Theme {
        Surface {
            DatosPersonalesStepScreen(
                rut = "20.123.456-5",
                rutError = null,
                nombres = "Camila Andrea",
                nombresError = null,
                apellidos = "",
                apellidosError = "Los apellidos son obligatorios",
                genero = Genero.FEMENINO,
                fechaNacimiento = "15/04/2008",
                onRutChange = {},
                onNombresChange = {},
                onApellidosChange = {},
                onGeneroChange = {},
                onFechaNacimientoChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Paso 2: Datos Personales (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DatosPersonalesStepDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            DatosPersonalesStepScreen(
                rut = "11.111.111-1",
                rutError = null,
                nombres = "Lucas Ignacio",
                nombresError = null,
                apellidos = "González Silva",
                apellidosError = null,
                genero = Genero.MASCULINO,
                fechaNacimiento = "",
                onRutChange = {},
                onNombresChange = {},
                onApellidosChange = {},
                onGeneroChange = {},
                onFechaNacimientoChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

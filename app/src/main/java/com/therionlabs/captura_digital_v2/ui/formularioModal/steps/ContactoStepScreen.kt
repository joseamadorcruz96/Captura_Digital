package com.therionlabs.captura_digital_v2.ui.formularioModal.steps

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Paso 3: Comuna de residencia y canales de contacto del postulante.
 */
@Composable
fun ContactoStepScreen(
    comunaResidencia: String,
    comunaResidenciaError: String?,
    celular: String,
    celularError: String?,
    email: String,
    emailError: String?,
    onComunaResidenciaChange: (String) -> Unit,
    onCelularChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    DuocCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Text(
            text = "3. Contacto y Residencia",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Ingresa la comuna de residencia y al menos un canal de contacto (celular o correo electrónico).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        DuocTextField(
            value = comunaResidencia,
            onValueChange = onComunaResidenciaChange,
            label = "Comuna de Residencia",
            placeholder = "Ej: Viña del Mar, Quilpué",
            isError = comunaResidenciaError != null,
            errorMessage = comunaResidenciaError
        )
        Spacer(Modifier.height(10.dp))

        DuocTextField(
            value = celular,
            onValueChange = onCelularChange,
            label = "Celular móvil (+56 9)",
            placeholder = "8 o 9 dígitos",
            keyboardType = KeyboardType.Phone,
            isError = celularError != null,
            errorMessage = celularError
        )
        Spacer(Modifier.height(10.dp))

        DuocTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Correo Electrónico",
            placeholder = "ejemplo@correo.com",
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError
        )
    }
}

@Preview(name = "Paso 3: Contacto (Claro)", showBackground = true)
@Composable
fun ContactoStepPreview() {
    Captura_Digital_v2Theme {
        Surface {
            ContactoStepScreen(
                comunaResidencia = "Viña del Mar",
                comunaResidenciaError = null,
                celular = "98765432",
                celularError = null,
                email = "correo_invalido",
                emailError = "Formato de correo electrónico inválido",
                onComunaResidenciaChange = {},
                onCelularChange = {},
                onEmailChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(name = "Paso 3: Contacto (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ContactoStepDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            ContactoStepScreen(
                comunaResidencia = "Valparaíso",
                comunaResidenciaError = null,
                celular = "12345678",
                celularError = null,
                email = "lucas@duoc.cl",
                emailError = null,
                onComunaResidenciaChange = {},
                onCelularChange = {},
                onEmailChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

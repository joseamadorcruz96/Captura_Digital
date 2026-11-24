package com.therionlabs.captura_digital_v2.ui.formularioModal.steps

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.formularioModal.components.SignaturePad
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Paso 1: Consentimiento informado (Ley N° 19.628 / Ley N° 21.719) y firma digital táctil.
 */
@Composable
fun ConsentimientoStepScreen(
    consentimiento: Boolean,
    consentimientoError: String?,
    onConsentimientoChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    firmaDigitalBase64: String? = null,
    firmaTimestampEpoch: Long? = null,
    firmaError: String? = null,
    onFirmaChange: (String?) -> Unit = {}
) {
    DuocCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Text(
            text = "1. Consentimiento Legal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Antes de capturar los datos del postulante, confirma su autorización expresa para fines de contacto y admisión.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onConsentimientoChange(!consentimiento) }
                .padding(vertical = 4.dp)
        ) {
            Checkbox(
                checked = consentimiento,
                onCheckedChange = onConsentimientoChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Sí, autorizo el tratamiento de mis datos personales para contacto y admisión por Duoc UC (Ley N° 19.628 / Ley N° 21.719).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (consentimientoError != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = consentimientoError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        SignaturePad(
            firmaBase64 = firmaDigitalBase64,
            firmaTimestampEpoch = firmaTimestampEpoch,
            onFirmaChange = onFirmaChange,
            errorMessage = firmaError
        )
    }
}

@Preview(name = "Paso 1: Consentimiento (Claro)", showBackground = true)
@Composable
fun ConsentimientoStepPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ConsentimientoStepScreen(
                    consentimiento = true,
                    consentimientoError = null,
                    onConsentimientoChange = {}
                )
                ConsentimientoStepScreen(
                    consentimiento = false,
                    consentimientoError = "Debe aceptar el consentimiento para continuar",
                    onConsentimientoChange = {}
                )
            }
        }
    }
}

@Preview(name = "Paso 1: Consentimiento (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConsentimientoStepDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            ConsentimientoStepScreen(
                consentimiento = false,
                consentimientoError = null,
                onConsentimientoChange = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

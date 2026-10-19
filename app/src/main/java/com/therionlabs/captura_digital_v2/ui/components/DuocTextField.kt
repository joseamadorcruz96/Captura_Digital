package com.therionlabs.captura_digital_v2.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ErrorRojo

/**
 * Campo de texto estandarizado Duoc UC.
 * Unifica bordes redondeados (12.dp), colores enfocados y soporte reactivo de errores y contraseñas.
 */
@Composable
fun DuocTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation = when {
        isPassword && !passwordVisible -> PasswordVisualTransformation()
        else -> VisualTransformation.None
    }

    val finalTrailingIcon: (@Composable () -> Unit)? = when {
        trailingIcon != null -> trailingIcon
        isPassword -> {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "👁" else "🔒",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        else -> null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = singleLine,
            enabled = enabled,
            readOnly = readOnly,
            isError = isError || errorMessage != null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DuocAzul,
                focusedLabelColor = DuocAzul,
                cursorColor = DuocAzul,
                errorBorderColor = ErrorRojo,
                errorLabelColor = ErrorRojo
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            trailingIcon = finalTrailingIcon,
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = ErrorRojo,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Preview(name = "DuocTextField - Estados", showBackground = true)
@Composable
fun DuocTextFieldPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Estado vacío con placeholder
                DuocTextField(
                    value = "",
                    onValueChange = {},
                    label = "Nombre completo",
                    placeholder = "Ej: Constanza Silva"
                )

                // Con texto y leadingIcon
                DuocTextField(
                    value = "19.876.543-2",
                    onValueChange = {},
                    label = "RUT",
                    leadingIcon = { Text("🪪") }
                )

                // Campo de contraseña
                DuocTextField(
                    value = "MiClaveSegura2026",
                    onValueChange = {},
                    label = "Contraseña",
                    isPassword = true
                )

                // Estado de error con mensaje explicativo
                DuocTextField(
                    value = "correo_invalido",
                    onValueChange = {},
                    label = "Correo Institucional",
                    isError = true,
                    errorMessage = "Ingresa un correo con dominio @duocuc.cl"
                )

                // Estado deshabilitado
                DuocTextField(
                    value = "SAN-JOAQUIN-01",
                    onValueChange = {},
                    label = "Código de Sede",
                    enabled = false
                )
            }
        }
    }
}

@Preview(name = "DuocTextField - Modo Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DuocTextFieldDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DuocTextField(
                    value = "Juan Pérez",
                    onValueChange = {},
                    label = "Nombre de Usuario"
                )
                DuocTextField(
                    value = "",
                    onValueChange = {},
                    label = "Contraseña",
                    isPassword = true,
                    isError = true,
                    errorMessage = "La contraseña es requerida"
                )
            }
        }
    }
}

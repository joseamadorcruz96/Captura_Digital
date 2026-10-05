package com.therionlabs.captura_digital_v2.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.R

@Composable
fun RecoverRoute(
    viewModel: RecoverViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecoverScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onSendClick = viewModel::send,
        onBack = onBack
    )
}

@Composable
fun RecoverScreen(
    uiState: RecoverUiState,
    onEmailChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBack: () -> Unit
) {
    val fondo = Brush.verticalGradient(
        0.0f to DuocAzul,
        0.45f to DuocAzulMedio,
        0.75f to FondoClaro,
        1.0f to FondoClaro
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(48.dp))

        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(R.drawable.logotherionlabs),
                contentDescription = "Logo",
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Recuperar contraseña",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.sent) {
                    // Mensaje de éxito
                    Text(
                        "¡Revisa tu correo!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Enviamos las instrucciones para restablecer tu contraseña a ${uiState.email}",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                } else {
                    // Formulario
                    Text(
                        "Ingresa el correo con que te registró el administrador y te enviaremos las instrucciones.",
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        isError = uiState.error != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DuocAzul,
                            focusedLabelColor = DuocAzul,
                            cursorColor = DuocAzul
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    uiState.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = onSendClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DuocAmarillo,
                            contentColor = DuocAzul
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Enviar instrucciones", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text(
                "Volver al inicio de sesión",
                color = DuocAzul,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
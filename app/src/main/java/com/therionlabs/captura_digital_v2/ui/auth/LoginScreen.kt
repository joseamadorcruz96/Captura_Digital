package com.therionlabs.captura_digital_v2.ui.auth

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therionlabs.captura_digital_v2.R
import com.therionlabs.captura_digital_v2.ui.components.DuocButton
import com.therionlabs.captura_digital_v2.ui.components.DuocCard
import com.therionlabs.captura_digital_v2.ui.components.DuocTextField

@Composable
fun LoginRoute(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.user) {
        if (uiState.user != null) onLoginSuccess()
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) vibrar(context)
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onForgotPassword = onForgotPassword
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPassword: () -> Unit
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
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Image(
                painter = painterResource(R.drawable.logotherionlabs),
                contentDescription = "Logo",
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Captura Digital",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Inicia sesión para continuar",
            style = MaterialTheme.typography.bodyMedium,
            color = DuocAmarillo
        )

        Spacer(Modifier.height(32.dp))

        DuocCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = 8.dp,
            contentPadding = 20.dp
        ) {
            DuocTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Correo electrónico",
                keyboardType = KeyboardType.Email
            )
            Spacer(Modifier.height(12.dp))

            DuocTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                isPassword = true,
                keyboardType = KeyboardType.Password
            )

            uiState.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            DuocButton(
                text = "Ingresar",
                onClick = onLoginClick
            )
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onForgotPassword) {
            Text(
                "¿Olvidaste tu contraseña?",
                color = DuocAzul,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Las cuentas son creadas por el administrador",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

private fun vibrar(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(300)
    }
}
package com.therionlabs.captura_digital_v2.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = DuocAzul,
    onPrimary = Color.White,
    primaryContainer = DuocAzulMedio,
    onPrimaryContainer = Color.White,
    secondary = DuocAmarillo,
    onSecondary = DuocAzul,
    secondaryContainer = DuocAmarilloClaro,
    onSecondaryContainer = DuocAzul,
    tertiary = DuocAzulMedio,
    onTertiary = Color.White,
    background = FondoClaro,
    onBackground = DuocAzulOscuro,
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE8ECF4),
    onSurfaceVariant = Color.DarkGray,
    error = ErrorRojo,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = DuocAmarillo,
    onPrimary = DuocAzul,
    secondary = DuocAmarillo,
    onSecondary = DuocAzul,
    background = Color(0xFF0F172A),
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    error = ErrorRojo,
    onError = Color.White
)

@Composable
fun Captura_Digital_v2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Para respetar la identidad institucional de Duoc UC, por defecto mantenemos los colores corporativos
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
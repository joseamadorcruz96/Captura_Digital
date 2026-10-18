package com.therionlabs.captura_digital_v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzulMedio

/**
 * Encabezado institucional Duoc UC con degradado azul superior,
 * botón de navegación opcional y soporte para subtítulos.
 */
@Composable
fun DuocHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backText: String = "← Volver",
    trailingAction: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DuocAzul, DuocAzulMedio)))
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        if (onBack != null || trailingAction != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    TextButton(
                        onClick = onBack,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(backText, color = Color.White)
                    }
                } else {
                    Spacer(Modifier.height(1.dp))
                }

                trailingAction?.invoke()
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = DuocAmarillo
            )
        }
    }
}

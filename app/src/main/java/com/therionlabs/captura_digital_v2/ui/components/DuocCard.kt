package com.therionlabs.captura_digital_v2.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul

/**
 * Tarjeta institucional estandarizada para formularios, métricas y contenido.
 */
@Composable
fun DuocCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = Color.White,
    elevation: Dp = 2.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Preview(name = "DuocCard - Claro", showBackground = true)
@Composable
fun DuocCardPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DuocCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Tarjeta Estándar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Tarjeta base con esquinas redondeadas (16.dp) y elevación sutil de 2.dp.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                DuocCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 6.dp,
                    contentPadding = 20.dp
                ) {
                    Text(
                        text = "📊 Métrica Destacada",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DuocAzul
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "48 postulaciones sincronizadas con éxito en la jornada de hoy.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(name = "DuocCard - Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DuocCardDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DuocCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Tarjeta en Modo Oscuro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Vista previa adaptada al esquema de color oscuro institucional.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

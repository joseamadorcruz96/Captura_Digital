package com.therionlabs.captura_digital_v2.ui.formularioModal.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Indicador circular individual por etapa del Stepper.
 * Muestra un círculo interior al 75% con animación de color, ícono dinámico
 * y un badge superior derecho de alerta cuando la etapa tiene errores pendientes.
 */
@Composable
fun StepCircle(
    isComplete: Boolean,
    isCurrent: Boolean,
    hasAlert: Boolean,
    stepIcon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outerColor by animateColorAsState(
        targetValue = when {
            isCurrent -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 250),
        label = "outerCircleColor"
    )

    val innerColor by animateColorAsState(
        targetValue = when {
            isComplete -> MaterialTheme.colorScheme.primary
            isCurrent -> MaterialTheme.colorScheme.secondary
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 250),
        label = "innerCircleColor"
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            isComplete -> MaterialTheme.colorScheme.onPrimary
            isCurrent -> MaterialTheme.colorScheme.onSecondary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 250),
        label = "iconTintColor"
    )

    // Contenedor cuadrado perfecto invisible (sin clip circular) para no cortar el badge de alerta
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Círculo exterior con clip y área táctil circular
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(CircleShape)
                .background(outerColor, shape = CircleShape)
                .then(
                    if (isCurrent) {
                        Modifier.border(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Círculo interior al 75%
            Box(
                modifier = Modifier
                    .fillMaxSize(0.75f)
                    .background(innerColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isComplete) Icons.Default.Check else stepIcon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Badge de alerta posicionado delante del círculo en la esquina superior derecha
        if (hasAlert) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
                    .size(16.dp)
                    .background(MaterialTheme.colorScheme.error, shape = CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    color = MaterialTheme.colorScheme.onError,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}

@Preview(name = "StepCircle - Estados (Claro)", showBackground = true)
@Composable
fun StepCircleStatesPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StepCircle(
                    isComplete = true,
                    isCurrent = false,
                    hasAlert = false,
                    stepIcon = Icons.Default.Info,
                    contentDescription = "Completado",
                    onClick = {},
                    modifier = Modifier.size(56.dp)
                )
                StepCircle(
                    isComplete = false,
                    isCurrent = true,
                    hasAlert = false,
                    stepIcon = Icons.Default.Person,
                    contentDescription = "Activo",
                    onClick = {},
                    modifier = Modifier.size(56.dp)
                )
                StepCircle(
                    isComplete = false,
                    isCurrent = false,
                    hasAlert = true,
                    stepIcon = Icons.Default.Phone,
                    contentDescription = "Con alerta",
                    onClick = {},
                    modifier = Modifier.size(56.dp)
                )
                StepCircle(
                    isComplete = false,
                    isCurrent = false,
                    hasAlert = false,
                    stepIcon = Icons.Default.Star,
                    contentDescription = "Pendiente",
                    onClick = {},
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

@Preview(name = "StepCircle - Estados (Oscuro)", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StepCircleDarkPreview() {
    Captura_Digital_v2Theme {
        Surface {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StepCircle(
                    isComplete = true,
                    isCurrent = false,
                    hasAlert = false,
                    stepIcon = Icons.Default.Info,
                    contentDescription = "Completado",
                    onClick = {},
                    modifier = Modifier.size(56.dp)
                )
                StepCircle(
                    isComplete = false,
                    isCurrent = true,
                    hasAlert = true,
                    stepIcon = Icons.Default.Person,
                    contentDescription = "Activo con alerta",
                    onClick = {},
                    modifier = Modifier.size(56.dp)
                )
            }
        }
    }
}

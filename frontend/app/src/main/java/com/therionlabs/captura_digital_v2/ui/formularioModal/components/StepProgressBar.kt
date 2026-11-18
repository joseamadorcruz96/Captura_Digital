package com.therionlabs.captura_digital_v2.ui.formularioModal.components

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.formularioModal.FormStep
import com.therionlabs.captura_digital_v2.ui.formularioModal.StepStatus
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

/**
 * Barra superior del Stepper con bordes redondeados (50.dp), progreso animado
 * y distribución horizontal de los 6 StepCircle interactivos.
 */
@Composable
fun StepProgressBar(
    currentStep: FormStep,
    stepStatuses: List<StepStatus>,
    progress: Float,
    onStepSelected: (FormStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "stepperProgressAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Contenedor redondeado con los 6 círculos de etapa
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp)),
            shape = RoundedCornerShape(50.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                stepStatuses.forEach { status ->
                    StepCircle(
                        isComplete = status.isComplete,
                        isCurrent = status.step == currentStep,
                        hasAlert = status.hasAlert,
                        stepIcon = iconForStep(status.step),
                        contentDescription = status.step.title,
                        onClick = { onStepSelected(status.step) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Pista de progreso animada proporcional a las etapas completadas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(50.dp))
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }

        // Resumen textual del paso actual y total completado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Paso ${currentStep.index + 1} de ${stepStatuses.size}: ${currentStep.title}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = "${stepStatuses.count { it.isComplete }}/${stepStatuses.size} listos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun iconForStep(step: FormStep): ImageVector {
    return when (step) {
        FormStep.CONSENTIMIENTO -> Icons.Default.Info
        FormStep.DATOS_PERSONALES -> Icons.Default.Person
        FormStep.CONTACTO -> Icons.Default.Phone
        FormStep.ANTECEDENTES -> Icons.Default.Home
        FormStep.SEDE_PERFIL -> Icons.Default.Place
        FormStep.CARRERAS -> Icons.Default.Star
    }
}

@Preview(name = "StepProgressBar - Claro", showBackground = true)
@Composable
fun StepProgressBarPreview() {
    val sampleStatuses = listOf(
        StepStatus(FormStep.CONSENTIMIENTO, isComplete = true, hasAlert = false, isVisited = true),
        StepStatus(FormStep.DATOS_PERSONALES, isComplete = true, hasAlert = false, isVisited = true),
        StepStatus(FormStep.CONTACTO, isComplete = false, hasAlert = true, isVisited = true),
        StepStatus(FormStep.ANTECEDENTES, isComplete = false, hasAlert = false, isVisited = true),
        StepStatus(FormStep.SEDE_PERFIL, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.CARRERAS, isComplete = false, hasAlert = false, isVisited = false)
    )
    Captura_Digital_v2Theme {
        Surface {
            StepProgressBar(
                currentStep = FormStep.ANTECEDENTES,
                stepStatuses = sampleStatuses,
                progress = 2f / 6f,
                onStepSelected = {}
            )
        }
    }
}

@Preview(name = "StepProgressBar - Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StepProgressBarDarkPreview() {
    val sampleStatuses = listOf(
        StepStatus(FormStep.CONSENTIMIENTO, isComplete = true, hasAlert = false, isVisited = true),
        StepStatus(FormStep.DATOS_PERSONALES, isComplete = false, hasAlert = true, isVisited = true),
        StepStatus(FormStep.CONTACTO, isComplete = false, hasAlert = false, isVisited = true),
        StepStatus(FormStep.ANTECEDENTES, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.SEDE_PERFIL, isComplete = false, hasAlert = false, isVisited = false),
        StepStatus(FormStep.CARRERAS, isComplete = false, hasAlert = false, isVisited = false)
    )
    Captura_Digital_v2Theme {
        Surface {
            StepProgressBar(
                currentStep = FormStep.CONTACTO,
                stepStatuses = sampleStatuses,
                progress = 1f / 6f,
                onStepSelected = {}
            )
        }
    }
}

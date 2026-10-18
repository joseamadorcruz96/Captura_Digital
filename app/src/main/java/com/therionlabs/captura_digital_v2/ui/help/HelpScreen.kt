package com.therionlabs.captura_digital_v2.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.auth.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzul
import com.therionlabs.captura_digital_v2.ui.auth.FondoClaro
import com.therionlabs.captura_digital_v2.ui.components.DuocHeader

private data class HelpStep(val title: String, val text: String)

private val steps = listOf(
    HelpStep(
        "Inicia una nueva captura",
        "Desde el inicio, presiona \"Formulario de captura\" para abrir el cupón de admisión."
    ),
    HelpStep(
        "Pide el consentimiento",
        "Antes de registrar datos, el prospecto debe aceptar el uso de su información personal."
    ),
    HelpStep(
        "Datos personales",
        "Ingresa RUT, nombres, apellidos, género, fecha de nacimiento, comuna, celular y correo. El RUT se valida automáticamente."
    ),
    HelpStep(
        "Antecedentes escolares",
        "Indica su situación académica (4° medio, egresado u otra), el establecimiento y su comuna."
    ),
    HelpStep(
        "Preferencias",
        "Selecciona la jornada (diurna o vespertina) y la sede de interés."
    ),
    HelpStep(
        "Interés y motivación",
        "Marca el nivel de interés y el perfil que mejor lo representa."
    ),
    HelpStep(
        "Carreras",
        "Elige hasta 3 carreras en orden de prioridad. La primera es obligatoria."
    ),
    HelpStep(
        "Guarda el cupón",
        "Presiona guardar. Verás una confirmación con el nombre y RUT del prospecto."
    ),
    HelpStep(
        "Revisa tus resultados",
        "En el dashboard verás cuántos prospectos llevas y su perfil: género, edad y carreras de interés."
    )
)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DuocHeader(
                title = "¿Cómo funciona?",
                subtitle = "Pasos para registrar un cupón de admisión",
                onBack = onBack
            )
        }

        itemsIndexed(steps) { index, step ->
            StepCard(number = index + 1, step = step)
        }

        // Consejo sin conexión
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4D6))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sin conexión", fontWeight = FontWeight.Bold, color = DuocAzul)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Puedes registrar cupones sin internet. Quedan guardados en el teléfono como pendientes y se sincronizan cuando vuelvas a tener señal.",
                        color = Color.DarkGray
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepCard(number: Int, step: HelpStep) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = DuocAmarillo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$number", fontWeight = FontWeight.Bold, color = DuocAzul)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(step.title, fontWeight = FontWeight.Bold, color = DuocAzul)
                Text(step.text, color = Color.DarkGray)
            }
        }
    }
}
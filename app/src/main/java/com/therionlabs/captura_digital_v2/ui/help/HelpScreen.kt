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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.auth.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzul
import com.therionlabs.captura_digital_v2.ui.auth.DuocAzulMedio
import com.therionlabs.captura_digital_v2.ui.auth.FondoClaro

private data class HelpStep(val title: String, val text: String)

private val steps = listOf(
    HelpStep("Inicia sesión", "Usa el correo y la contraseña que te entregó el administrador."),
    HelpStep("Abre el formulario", "Desde el menú ☰ elige \"Nueva encuesta\"."),
    HelpStep("Completa los datos", "Registra la información del prospecto: nombre, edad, género e intereses."),
    HelpStep("Guarda la encuesta", "Presiona \"Guardar\". Verás una confirmación al terminar."),
    HelpStep("Revisa tus resultados", "En el dashboard verás cuántas encuestas llevas y el resumen de tus prospectos.")
)

@Composable
fun HelpScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoClaro),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Encabezado
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DuocAzul, DuocAzulMedio)))
                    .statusBarsPadding()
                    .padding(24.dp)
            ) {
                Text(
                    "¿Cómo funciona?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Sigue estos pasos para registrar una encuesta",
                    color = DuocAmarillo
                )
            }
        }

        // Pasos
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
                        "Puedes trabajar sin internet. Las encuestas se guardan en el teléfono y se envían solas cuando vuelvas a tener señal.",
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
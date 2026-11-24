package com.therionlabs.captura_digital_v2.ui.formularioModal.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.ui.theme.ExitoVerde
import java.io.ByteArrayOutputStream

/**
 * Pad táctil de firma digital basado en Compose Canvas sin dependencias externas.
 * Convierte el trazo a PNG comprimido en Base64 compacto y permite limpiar o previsualizar firmas guardadas.
 */
@Composable
fun SignaturePad(
    firmaBase64: String?,
    firmaTimestampEpoch: Long?,
    onFirmaChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null
) {
    val isPreview = LocalInspectionMode.current
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(400, 150)) }

    LaunchedEffect(firmaBase64) {
        if (firmaBase64.isNullOrBlank() && strokes.isNotEmpty()) {
            strokes.clear()
            currentStroke = emptyList()
        }
    }

    val decodedPreviewBitmap = remember(firmaBase64, isPreview) {
        if (isPreview || firmaBase64.isNullOrBlank()) null
        else decodeBase64Bitmap(firmaBase64)
    }

    val hasLiveStrokes = strokes.isNotEmpty() || currentStroke.isNotEmpty()
    val hasSignature = hasLiveStrokes || !firmaBase64.isNullOrBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Firma del Postulante",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (hasSignature) {
                        val hora = firmaTimestampEpoch?.let {
                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(java.util.Date(it))
                        }
                        if (hora != null) "Firma registrada ($hora)" else "Firma digital registrada"
                    } else {
                        "Dibuja la firma con el dedo dentro del recuadro"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasSignature) ExitoVerde else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (hasSignature) {
                TextButton(
                    onClick = {
                        strokes.clear()
                        currentStroke = emptyList()
                        onFirmaChange(null)
                    }
                ) {
                    Text("Limpiar firma", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFAFBFD))
                .border(
                    width = if (errorMessage != null) 1.5.dp else 1.dp,
                    color = when {
                        errorMessage != null -> MaterialTheme.colorScheme.error
                        hasSignature -> ExitoVerde
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke = currentStroke + change.position
                        },
                        onDragEnd = {
                            if (currentStroke.size >= 2) {
                                strokes.add(currentStroke)
                                currentStroke = emptyList()
                                val encoded = encodeStrokesToBase64(
                                    strokes = strokes.toList(),
                                    width = canvasSize.width.coerceAtLeast(1),
                                    height = canvasSize.height.coerceAtLeast(1)
                                )
                                onFirmaChange(encoded)
                            } else {
                                currentStroke = emptyList()
                            }
                        },
                        onDragCancel = {
                            currentStroke = emptyList()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (!hasLiveStrokes && decodedPreviewBitmap != null) {
                Image(
                    bitmap = decodedPreviewBitmap.asImageBitmap(),
                    contentDescription = "Firma digital guardada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeStyle = Stroke(
                    width = 5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
                (strokes + listOfNotNull(currentStroke.takeIf { it.isNotEmpty() })).forEach { pts ->
                    if (pts.size >= 2) {
                        val path = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 1 until pts.size) {
                                lineTo(pts[i].x, pts[i].y)
                            }
                        }
                        drawPath(path = path, color = DuocAzul, style = strokeStyle)
                    }
                }
            }

            if (!hasSignature) {
                Text(
                    text = "Firme aquí",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun encodeStrokesToBase64(
    strokes: List<List<Offset>>,
    width: Int,
    height: Int
): String? {
    if (strokes.isEmpty()) return null
    return runCatching {
        val targetW = 360
        val targetH = 140
        val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.TRANSPARENT)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(0, 43, 92)
            strokeWidth = 4f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val scaleX = targetW.toFloat() / width.toFloat()
        val scaleY = targetH.toFloat() / height.toFloat()

        strokes.forEach { pts ->
            if (pts.size >= 2) {
                val path = android.graphics.Path()
                path.moveTo(pts.first().x * scaleX, pts.first().y * scaleY)
                for (i in 1 until pts.size) {
                    path.lineTo(pts[i].x * scaleX, pts[i].y * scaleY)
                }
                canvas.drawPath(path, paint)
            }
        }

        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, output)
        bitmap.recycle()
        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()
}

private fun decodeBase64Bitmap(base64: String): Bitmap? {
    return runCatching {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()
}

@Preview(name = "SignaturePad - Vacío", showBackground = true)
@Composable
fun SignaturePadPreview() {
    Captura_Digital_v2Theme {
        Surface(modifier = Modifier.padding(16.dp)) {
            SignaturePad(
                firmaBase64 = null,
                firmaTimestampEpoch = null,
                onFirmaChange = {}
            )
        }
    }
}

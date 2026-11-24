package com.therionlabs.captura_digital_v2.ui.formularioModal.components

import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.therionlabs.captura_digital_v2.ui.theme.DuocAmarillo
import com.therionlabs.captura_digital_v2.ui.theme.DuocAzul
import com.therionlabs.captura_digital_v2.util.CedulaChileParser
import com.therionlabs.captura_digital_v2.util.CedulaScanResult
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * ModalBottomSheet con CameraX + ML Kit Barcode Scanning (QR y PDF417) para leer
 * la Cédula de Identidad chilena y extraer RUT y nombres vía [CedulaChileParser].
 */
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CedulaScannerSheet(
    onCedulaDetected: (CedulaScanResult) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var statusText by remember { mutableStateOf("Apunta al código QR o PDF417 de la cédula...") }
    val detectedFlag = remember { AtomicBoolean(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_PDF417)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            runCatching { scanner.close() }
            cameraExecutor.shutdown()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Escanear Cédula de Identidad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DuocAzul
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, DuocAmarillo, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val provider = runCatching { cameraProviderFuture.get() }.getOrNull()
                            if (provider == null) {
                                statusText = "No se pudo inicializar la cámara en este dispositivo."
                                return@addListener
                            }

                            val preview = CameraPreview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            @Suppress("DEPRECATION")
                            val analysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                processBarcodeProxy(
                                    imageProxy = imageProxy,
                                    scanner = scanner,
                                    detectedFlag = detectedFlag,
                                    onParsed = { result ->
                                        ContextCompat.getMainExecutor(ctx).execute {
                                            onCedulaDetected(result)
                                        }
                                    },
                                    onNonCedulaDetected = {
                                        ContextCompat.getMainExecutor(ctx).execute {
                                            statusText = "Código detectado sin RUT válido. Enfoca el QR/PDF417 del carnet."
                                        }
                                    }
                                )
                            }

                            runCatching {
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    analysis
                                )
                            }.onFailure {
                                statusText = "Cámara trasera no disponible en este dispositivo."
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cancelar y escribir manualmente", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processBarcodeProxy(
    imageProxy: androidx.camera.core.ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    detectedFlag: AtomicBoolean,
    onParsed: (CedulaScanResult) -> Unit,
    onNonCedulaDetected: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null || detectedFlag.get()) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            if (detectedFlag.get()) return@addOnSuccessListener
            for (barcode in barcodes) {
                val raw = barcode.rawValue ?: continue
                val parsed = CedulaChileParser.parse(raw)
                if (parsed != null && detectedFlag.compareAndSet(false, true)) {
                    onParsed(parsed)
                    return@addOnSuccessListener
                }
            }
            if (barcodes.isNotEmpty()) {
                onNonCedulaDetected()
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

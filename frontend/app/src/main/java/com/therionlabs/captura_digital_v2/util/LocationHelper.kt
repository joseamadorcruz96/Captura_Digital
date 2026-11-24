package com.therionlabs.captura_digital_v2.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.math.roundToLong

sealed class LocationFetchResult {
    data class Success(val latitud: Double, val longitud: Double) : LocationFetchResult()
    data object PermissionDenied : LocationFetchResult()
    data object ProviderDisabled : LocationFetchResult()
    data class Unavailable(val reason: String) : LocationFetchResult()
}

/**
 * Helper nativo basado en [LocationManager] para adjuntar coordenadas aproximadas de captura
 * (redondeadas a 4 decimales ~11 m) bajo permiso explícito, sin bloquear nunca el guardado.
 */
object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    @SuppressLint("MissingPermission")
    suspend fun fetchApproximateLocation(
        context: Context,
        timeoutMs: Long = 5_000L
    ): LocationFetchResult {
        if (!hasLocationPermission(context)) {
            return LocationFetchResult.PermissionDenied
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return LocationFetchResult.Unavailable("Servicio de ubicación no disponible en este dispositivo")

        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            return LocationFetchResult.ProviderDisabled
        }

        // 1. Intentar última ubicación reciente conocida para respuesta instantánea en feria
        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        val bestLastKnown = providers
            .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

        if (bestLastKnown != null && System.currentTimeMillis() - bestLastKnown.time <= 10 * 60 * 1000L) {
            return LocationFetchResult.Success(
                latitud = roundCoord(bestLastKnown.latitude),
                longitud = roundCoord(bestLastKnown.longitude)
            )
        }

        // 2. Solicitar una lectura actual con timeout seguro
        val targetProvider = when {
            runCatching { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false) ->
                LocationManager.NETWORK_PROVIDER
            runCatching { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false) ->
                LocationManager.GPS_PROVIDER
            else -> null
        }

        if (targetProvider != null) {
            val freshLocation = withTimeoutOrNull(timeoutMs) {
                requestSingleLocation(locationManager, targetProvider, ContextCompat.getMainExecutor(context))
            }
            if (freshLocation != null) {
                return LocationFetchResult.Success(
                    latitud = roundCoord(freshLocation.latitude),
                    longitud = roundCoord(freshLocation.longitude)
                )
            }
        }

        // 3. Fallback a última ubicación conocida aunque sea más antigua
        if (bestLastKnown != null) {
            return LocationFetchResult.Success(
                latitud = roundCoord(bestLastKnown.latitude),
                longitud = roundCoord(bestLastKnown.longitude)
            )
        }

        return LocationFetchResult.Unavailable("No se pudo obtener señal de ubicación; se guardará sin coordenadas")
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocation(
        locationManager: LocationManager,
        provider: String,
        executor: Executor
    ): Location? = suspendCancellableCoroutine { cont ->
        val cancellationSignal = androidx.core.os.CancellationSignal()
        cont.invokeOnCancellation { cancellationSignal.cancel() }

        runCatching {
            LocationManagerCompat.getCurrentLocation(
                locationManager,
                provider,
                cancellationSignal,
                executor
            ) { location ->
                if (cont.isActive) {
                    cont.resume(location)
                }
            }
        }.onFailure {
            if (cont.isActive) cont.resume(null)
        }
    }

    fun roundCoord(value: Double): Double = (value * 10_000.0).roundToLong() / 10_000.0
}

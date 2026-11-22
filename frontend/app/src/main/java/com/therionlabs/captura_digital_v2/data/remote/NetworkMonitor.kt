package com.therionlabs.captura_digital_v2.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Contrato observable del estado de conectividad a Internet del dispositivo.
 */
interface ConnectivityObserver {
    val isOnline: StateFlow<Boolean>
    fun isCurrentlyOnline(): Boolean = isOnline.value
}

/**
 * Monitor global reactivo de conectividad basado en ConnectivityManager.NetworkCallback.
 * Se inicializa una sola vez en Application.onCreate() sin polling ni consumo en reposo.
 */
class NetworkMonitor private constructor(
    context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val validatedNetworks = ConcurrentHashMap.newKeySet<Network>()
    private val _isOnline = MutableStateFlow(checkCurrentConnectivity())
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val caps = connectivityManager.getNetworkCapabilities(network)
            if (isValidatedInternet(caps)) {
                validatedNetworks.add(network)
                _isOnline.value = true
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            if (isValidatedInternet(networkCapabilities)) {
                validatedNetworks.add(network)
            } else {
                validatedNetworks.remove(network)
            }
            _isOnline.value = validatedNetworks.isNotEmpty()
        }

        override fun onLost(network: Network) {
            validatedNetworks.remove(network)
            _isOnline.value = validatedNetworks.isNotEmpty() || checkCurrentConnectivity()
        }

        override fun onUnavailable() {
            validatedNetworks.clear()
            _isOnline.value = false
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        runCatching {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    override fun isCurrentlyOnline(): Boolean = _isOnline.value

    private fun checkCurrentConnectivity(): Boolean {
        val active = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(active)
        return isValidatedInternet(caps)
    }

    private fun isValidatedInternet(caps: NetworkCapabilities?): Boolean {
        if (caps == null) return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        @Volatile
        private var INSTANCE: NetworkMonitor? = null

        private val fallbackObserver = object : ConnectivityObserver {
            private val state = MutableStateFlow(true)
            override val isOnline: StateFlow<Boolean> = state.asStateFlow()
        }

        fun init(context: Context): NetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkMonitor(context).also { INSTANCE = it }
            }
        }

        /**
         * Acceso global al observador de red; en Previews o tests JVM sin Application devuelve un fallback seguro.
         */
        fun observer(): ConnectivityObserver = INSTANCE ?: fallbackObserver

        fun isInternetAvailable(): Boolean = observer().isCurrentlyOnline()
    }
}

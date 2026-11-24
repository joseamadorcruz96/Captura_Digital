package com.therionlabs.captura_digital_v2.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.therionlabs.captura_digital_v2.CapturaDigitalApp

/**
 * Crea u obtiene un ViewModel inyectando dependencias desde [AppContainer] cuando la aplicación
 * está inicializada, manteniendo compatibilidad con @Preview y tests sin Android Context.
 */
@Composable
inline fun <reified VM : ViewModel> rememberAppViewModel(
    crossinline creator: (AppContainer?) -> VM
): VM {
    val container = CapturaDigitalApp.containerOrNull()
    return viewModel(
        factory = viewModelFactory {
            initializer { creator(container) }
        }
    )
}

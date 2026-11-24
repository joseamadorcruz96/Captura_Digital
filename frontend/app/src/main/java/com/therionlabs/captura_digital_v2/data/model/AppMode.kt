package com.therionlabs.captura_digital_v2.data.model

/**
 * Modo de operación de la aplicación bajo estrategia App-First.
 */
enum class AppMode(val displayName: String, val description: String) {
    LOCAL_DEMO(
        displayName = "Demo Local",
        description = "Opera 100% en local con datos de prueba sin depender del servidor"
    ),
    REMOTE_GATEWAY(
        displayName = "Servidor Remoto",
        description = "Conecta con API Gateway y microservicios (JWT + Batch Sync)"
    )
}

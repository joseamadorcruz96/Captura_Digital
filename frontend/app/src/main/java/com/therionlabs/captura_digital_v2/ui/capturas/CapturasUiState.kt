package com.therionlabs.captura_digital_v2.ui.capturas

import com.therionlabs.captura_digital_v2.data.model.AppMode
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import com.therionlabs.captura_digital_v2.data.model.Prospecto
import com.therionlabs.captura_digital_v2.data.model.SyncStatus

enum class CapturasFilter(val label: String) {
    TODOS("Todos"),
    BORRADORES("Borradores"),
    PENDIENTES("Pendientes"),
    SINCRONIZADOS("Sincronizados")
}

data class CapturasUiState(
    val filter: CapturasFilter = CapturasFilter.TODOS,
    val searchQuery: String = "",
    val appMode: AppMode = AppMode.LOCAL_DEMO,
    val isAdmin: Boolean = false,
    val borradores: List<FormularioBorrador> = emptyList(),
    val capturas: List<Prospecto> = emptyList(),
    val selectedProspectoDetail: Prospecto? = null,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
) {
    private val normalizedQuery: String
        get() = searchQuery.trim().lowercase()

    val borradoresFiltrados: List<FormularioBorrador>
        get() {
            if (filter != CapturasFilter.TODOS && filter != CapturasFilter.BORRADORES) return emptyList()
            val q = normalizedQuery
            if (q.isBlank()) return borradores
            return borradores.filter {
                it.tituloResumen.lowercase().contains(q) ||
                    it.rut.lowercase().contains(q) ||
                    it.carreraPrioridad1.lowercase().contains(q)
            }
        }

    val capturasFiltradas: List<Prospecto>
        get() {
            val byFilter = when (filter) {
                CapturasFilter.TODOS -> capturas
                CapturasFilter.BORRADORES -> emptyList()
                CapturasFilter.PENDIENTES -> capturas.filter { it.syncStatus != SyncStatus.SYNCED }
                CapturasFilter.SINCRONIZADOS -> capturas.filter { it.syncStatus == SyncStatus.SYNCED }
            }
            val q = normalizedQuery
            if (q.isBlank()) return byFilter
            return byFilter.filter {
                it.nombreCompleto.lowercase().contains(q) ||
                    it.rut.lowercase().contains(q) ||
                    it.carreraPrioridad1.lowercase().contains(q)
            }
        }

    val pendingSyncCount: Int
        get() = capturas.count { it.syncStatus != SyncStatus.SYNCED }

    val syncedCount: Int
        get() = capturas.count { it.syncStatus == SyncStatus.SYNCED }
}

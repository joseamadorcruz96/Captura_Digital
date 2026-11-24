package com.therionlabs.captura_digital_v2.data.repository

import com.therionlabs.captura_digital_v2.data.local.dao.BorradorDao
import com.therionlabs.captura_digital_v2.data.model.FormularioBorrador
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Contrato para persistencia local de borradores de captura en progreso.
 * Los borradores son estrictamente locales (no sincronizables) hasta convertirse en [Prospecto].
 */
interface IBorradorRepository {
    fun observeByCaptador(captadorId: Int): Flow<List<FormularioBorrador>>
    fun observeAll(): Flow<List<FormularioBorrador>>
    suspend fun getById(id: String): FormularioBorrador?
    suspend fun getLatestByCaptador(captadorId: Int): FormularioBorrador?
    suspend fun guardarBorrador(borrador: FormularioBorrador): Boolean
    suspend fun eliminarBorrador(id: String): Boolean
}

class BorradorRepository(
    private val dao: BorradorDao? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : IBorradorRepository {

    private val memoryFlow = MutableStateFlow<List<FormularioBorrador>>(emptyList())

    init {
        if (dao != null) {
            coroutineScope.launch {
                dao.observeAll().collect { items ->
                    memoryFlow.value = items
                }
            }
        }
    }

    override fun observeByCaptador(captadorId: Int): Flow<List<FormularioBorrador>> {
        return dao?.observeByCaptador(captadorId)
            ?: memoryFlow.map { list ->
                list.filter { it.captadorId == captadorId }.sortedByDescending { it.updatedAtEpoch }
            }
    }

    override fun observeAll(): Flow<List<FormularioBorrador>> {
        return dao?.observeAll()
            ?: memoryFlow.map { list -> list.sortedByDescending { it.updatedAtEpoch } }
    }

    override suspend fun getById(id: String): FormularioBorrador? {
        return dao?.getById(id) ?: memoryFlow.value.firstOrNull { it.id == id }
    }

    override suspend fun getLatestByCaptador(captadorId: Int): FormularioBorrador? {
        return dao?.getLatestByCaptador(captadorId)
            ?: memoryFlow.value
                .filter { it.captadorId == captadorId }
                .maxByOrNull { it.updatedAtEpoch }
    }

    override suspend fun guardarBorrador(borrador: FormularioBorrador): Boolean {
        if (!borrador.hasMinimumDraftData()) {
            return false
        }
        val normalized = borrador.copy(updatedAtEpoch = borrador.updatedAtEpoch)
        val current = memoryFlow.value.toMutableList()
        val idx = current.indexOfFirst { it.id == normalized.id }
        if (idx != -1) {
            current[idx] = normalized
        } else {
            current.add(0, normalized)
        }
        memoryFlow.value = current.sortedByDescending { it.updatedAtEpoch }
        dao?.insertOrUpdate(normalized)
        return true
    }

    override suspend fun eliminarBorrador(id: String): Boolean {
        memoryFlow.value = memoryFlow.value.filterNot { it.id == id }
        dao?.deleteById(id)
        return true
    }
}

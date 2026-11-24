package com.therionlabs.captura_digital_v2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Borrador local de formulario de captura en progreso.
 * Permite pausar y retomar múltiples capturas por captador (exigiendo mínimo 1 campo con datos).
 * Los borradores viven exclusivamente en almacenamiento local y nunca se sincronizan con el backend.
 */
@Entity(
    tableName = "formulario_borradores",
    indices = [
        Index(value = ["captadorId"]),
        Index(value = ["updatedAtEpoch"])
    ]
)
data class FormularioBorrador(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val captadorId: Int,
    val eventoId: String? = null,
    val modoExpress: Boolean = false,
    val currentStep: String = "CONSENTIMIENTO",

    // Paso 1: Consentimiento + firma
    val consentimiento: Boolean = false,
    val firmaDigitalBase64: String? = null,
    val firmaTimestampEpoch: Long? = null,

    // Paso 2: Datos personales
    val rut: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val genero: Genero = Genero.NO_INDICA,
    val fechaNacimiento: String = "",

    // Paso 3: Contacto y residencia
    val comunaResidencia: String = "",
    val celular: String = "",
    val email: String = "",

    // Paso 4: Antecedentes escolares
    val situacionAcademica: SituacionAcademica = SituacionAcademica.CUARTO_MEDIO,
    val situacionAcademicaOtro: String = "",
    val establecimientoEducacional: String = "",
    val comunaEstablecimiento: String = "",
    val jornada: Jornada = Jornada.DIURNA,

    // Paso 5: Sede y perfilamiento
    val sedeInteres: String? = null,
    val nivelInteres: NivelInteres? = null,
    val perfilMotivacional: PerfilMotivacional? = null,

    // Paso 6: Carreras
    val escuelaSeleccionadaId: String = "INFOR",
    val carreraPrioridad1: String = "",
    val carreraPrioridad2: String = "",
    val carreraPrioridad3: String = "",

    // Geolocalización opcional y referencia si edita prospecto existente
    val latitud: Double? = null,
    val longitud: Double? = null,
    val editingProspectoId: String? = null,
    val updatedAtEpoch: Long = System.currentTimeMillis()
) {
    /**
     * Regla de negocio: un borrador debe contener al menos 1 campo ingresado (ej: nombre o RUT)
     * para evitar almacenar registros completamente vacíos en Room.
     */
    fun hasMinimumDraftData(): Boolean {
        return rut.isNotBlank() ||
            nombres.isNotBlank() ||
            apellidos.isNotBlank() ||
            fechaNacimiento.isNotBlank() ||
            comunaResidencia.isNotBlank() ||
            celular.isNotBlank() ||
            email.isNotBlank() ||
            situacionAcademicaOtro.isNotBlank() ||
            establecimientoEducacional.isNotBlank() ||
            comunaEstablecimiento.isNotBlank() ||
            !sedeInteres.isNullOrBlank() ||
            carreraPrioridad1.isNotBlank() ||
            carreraPrioridad2.isNotBlank() ||
            carreraPrioridad3.isNotBlank() ||
            !firmaDigitalBase64.isNullOrBlank()
    }

    val tituloResumen: String
        get() {
            val fullName = "$nombres $apellidos".trim()
            return when {
                fullName.isNotBlank() -> fullName
                rut.isNotBlank() -> "RUT $rut"
                email.isNotBlank() -> email
                celular.isNotBlank() -> "+569 $celular"
                establecimientoEducacional.isNotBlank() -> establecimientoEducacional
                comunaResidencia.isNotBlank() -> comunaResidencia
                else -> "Borrador sin nombre"
            }
        }

    val resumenSecundario: String
        get() {
            val partes = buildList {
                if (rut.isNotBlank() && tituloResumen != "RUT $rut") add(rut)
                if (carreraPrioridad1.isNotBlank()) add(carreraPrioridad1)
                else if (!sedeInteres.isNullOrBlank()) add("Sede $sedeInteres")
                else if (establecimientoEducacional.isNotBlank() && tituloResumen != establecimientoEducacional) {
                    add(establecimientoEducacional)
                } else if (comunaResidencia.isNotBlank() && tituloResumen != comunaResidencia) {
                    add(comunaResidencia)
                }
            }
            return if (partes.isEmpty()) "Captura pausada · Solo local" else partes.joinToString(" · ")
        }
}

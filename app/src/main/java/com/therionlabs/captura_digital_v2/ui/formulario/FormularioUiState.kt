package com.therionlabs.captura_digital_v2.ui.formulario

import com.therionlabs.captura_digital_v2.data.model.Genero
import com.therionlabs.captura_digital_v2.data.model.Jornada
import com.therionlabs.captura_digital_v2.data.model.NivelInteres
import com.therionlabs.captura_digital_v2.data.model.PerfilMotivacional
import com.therionlabs.captura_digital_v2.data.model.SituacionAcademica

data class FormularioUiState(
    // Consentimiento Legal (Ley N° 19.628 / 21.719)
    val consentimiento: Boolean = false,
    val consentimientoError: String? = null,

    // Datos Personales
    val rut: String = "",
    val rutError: String? = null,
    val nombres: String = "",
    val nombresError: String? = null,
    val apellidos: String = "",
    val apellidosError: String? = null,
    val genero: Genero = Genero.NO_INDICA,
    val fechaNacimiento: String = "",
    val comunaResidencia: String = "",
    val comunaResidenciaError: String? = null,
    val celular: String = "",
    val celularError: String? = null,
    val email: String = "",
    val emailError: String? = null,

    // Antecedentes Escolares
    val situacionAcademica: SituacionAcademica = SituacionAcademica.CUARTO_MEDIO,
    val situacionAcademicaOtro: String = "",
    val establecimientoEducacional: String = "",
    val establecimientoError: String? = null,
    val comunaEstablecimiento: String = "",

    // Preferencias Institucionales
    val jornada: Jornada = Jornada.DIURNA,
    val sedeInteres: String = "Viña del Mar",

    // Calificación y Motivación
    val nivelInteres: NivelInteres = NivelInteres.ALTO,
    val perfilMotivacional: PerfilMotivacional = PerfilMotivacional.VOCACIONAL,

    // Selección de Carreras (1 a 3 prioridades)
    val escuelaSeleccionadaId: String = "INFOR",
    val carreraPrioridad1: String = "",
    val carrera1Error: String? = null,
    val carreraPrioridad2: String = "",
    val carreraPrioridad3: String = "",

    // Control de UI
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val savedProspectoNombre: String = "",
    val savedProspectoRut: String = ""
)

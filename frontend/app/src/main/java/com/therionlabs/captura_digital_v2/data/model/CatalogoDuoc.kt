package com.therionlabs.captura_digital_v2.data.model

/**
 * Catálogo académico oficial de Duoc UC según especificación del cupón físico.
 * Incluye las 10 Escuelas, sedes por región y sus carreras técnicas y profesionales.
 */

enum class NivelAcademico(val displayName: String) {
    PROFESIONAL("Profesional"),
    TECNICO("Técnico Nivel Superior")
}

data class Escuela(
    val id: String,
    val nombre: String
)

data class Carrera(
    val id: String,
    val escuelaId: String,
    val nombre: String,
    val nivel: NivelAcademico
)

data class Sede(
    val id: String,
    val nombre: String,
    val region: String
)

object CatalogoDuoc {

    val escuelas: List<Escuela> = listOf(
        Escuela("ADMIN", "Escuela de Administración y Negocios"),
        Escuela("COMUN", "Escuela de Comunicación"),
        Escuela("CONST", "Escuela de Construcción"),
        Escuela("DISEN", "Escuela de Diseño"),
        Escuela("GASTR", "Escuela de Gastronomía"),
        Escuela("INFOR", "Escuela de Informática y Telecomunicaciones"),
        Escuela("SALUD", "Escuela de Salud y Bienestar"),
        Escuela("INGEN", "Escuela de Ingeniería y Recursos Naturales"),
        Escuela("TURIS", "Escuela de Turismo y Hospitalidad"),
        Escuela("ONLIN", "Duoc Online")
    )

    val carreras: List<Carrera> = listOf(
        // Administración y Negocios
        Carrera("ADM_01", "ADMIN", "Auditoría", NivelAcademico.PROFESIONAL),
        Carrera("ADM_02", "ADMIN", "Ingeniería en Administración Mención Finanzas", NivelAcademico.PROFESIONAL),
        Carrera("ADM_03", "ADMIN", "Ingeniería en Administración Mención Gestión de Personas", NivelAcademico.PROFESIONAL),
        Carrera("ADM_04", "ADMIN", "Ingeniería en Administración Mención Innovación y Emprendimiento", NivelAcademico.PROFESIONAL),
        Carrera("ADM_05", "ADMIN", "Ingeniería en Comercio Exterior", NivelAcademico.PROFESIONAL),
        Carrera("ADM_06", "ADMIN", "Ingeniería en Gestión Logística", NivelAcademico.PROFESIONAL),
        Carrera("ADM_07", "ADMIN", "Ingeniería en Marketing Digital", NivelAcademico.PROFESIONAL),
        Carrera("ADM_08", "ADMIN", "Comercio Exterior", NivelAcademico.TECNICO),
        Carrera("ADM_09", "ADMIN", "Contabilidad General Mención Legislación Tributaria", NivelAcademico.TECNICO),
        Carrera("ADM_10", "ADMIN", "Técnico en Administración", NivelAcademico.TECNICO),
        Carrera("ADM_11", "ADMIN", "Técnico en Gestión Logística", NivelAcademico.TECNICO),

        // Comunicación
        Carrera("COM_01", "COMUN", "Animación Digital", NivelAcademico.PROFESIONAL),
        Carrera("COM_02", "COMUN", "Comunicación Audiovisual", NivelAcademico.PROFESIONAL),
        Carrera("COM_03", "COMUN", "Ingeniería en Sonido", NivelAcademico.PROFESIONAL),
        Carrera("COM_04", "COMUN", "Publicidad", NivelAcademico.PROFESIONAL),
        Carrera("COM_05", "COMUN", "Relaciones Públicas y Comunicación Organizacional", NivelAcademico.PROFESIONAL),
        Carrera("COM_06", "COMUN", "Técnico Audiovisual", NivelAcademico.TECNICO),
        Carrera("COM_07", "COMUN", "Tecnología en Sonido e Iluminación", NivelAcademico.TECNICO),

        // Construcción
        Carrera("CON_01", "CONST", "Ingeniería en Construcción", NivelAcademico.PROFESIONAL),
        Carrera("CON_02", "CONST", "Ingeniería en Prevención de Riesgos", NivelAcademico.PROFESIONAL),
        Carrera("CON_03", "CONST", "Técnico Modelador Proyectista", NivelAcademico.TECNICO),
        Carrera("CON_04", "CONST", "Restauración de Bienes Patrimoniales", NivelAcademico.TECNICO),
        Carrera("CON_05", "CONST", "Técnico en Construcción", NivelAcademico.TECNICO),
        Carrera("CON_06", "CONST", "Técnico en Prevención de Riesgos", NivelAcademico.TECNICO),
        Carrera("CON_07", "CONST", "Técnico Topógrafo Geomático", NivelAcademico.TECNICO),

        // Diseño
        Carrera("DIS_01", "DISEN", "Diseño de Ambientes", NivelAcademico.PROFESIONAL),
        Carrera("DIS_02", "DISEN", "Diseño de Vestuario", NivelAcademico.PROFESIONAL),
        Carrera("DIS_03", "DISEN", "Diseño Gráfico", NivelAcademico.PROFESIONAL),
        Carrera("DIS_04", "DISEN", "Diseño Industrial e Innovación en Productos", NivelAcademico.PROFESIONAL),
        Carrera("DIS_05", "DISEN", "Ilustración para Contextos Globales", NivelAcademico.PROFESIONAL),

        // Gastronomía
        Carrera("GAS_01", "GASTR", "Gastronomía Internacional", NivelAcademico.PROFESIONAL),
        Carrera("GAS_02", "GASTR", "Gastronomía", NivelAcademico.TECNICO),

        // Informática y Telecomunicaciones
        Carrera("INF_01", "INFOR", "Ingeniería en Informática", NivelAcademico.PROFESIONAL),
        Carrera("INF_02", "INFOR", "Ingeniería en Infraestructura Tecnológica", NivelAcademico.PROFESIONAL),
        Carrera("INF_03", "INFOR", "Ingeniería en Redes y Telecomunicaciones", NivelAcademico.PROFESIONAL),
        Carrera("INF_04", "INFOR", "Analista Programador", NivelAcademico.TECNICO),
        Carrera("INF_05", "INFOR", "Técnico en Redes y Telecomunicaciones", NivelAcademico.TECNICO),

        // Salud y Bienestar
        Carrera("SAL_01", "SALUD", "Informática Biomédica", NivelAcademico.PROFESIONAL),
        Carrera("SAL_02", "SALUD", "Preparador Físico", NivelAcademico.TECNICO),
        Carrera("SAL_03", "SALUD", "Técnico de Enfermería", NivelAcademico.TECNICO),
        Carrera("SAL_04", "SALUD", "Técnico en Laboratorio Clínico, Banco de Sangre e Imagenología", NivelAcademico.TECNICO),
        Carrera("SAL_05", "SALUD", "Técnico en Odontología", NivelAcademico.TECNICO),
        Carrera("SAL_06", "SALUD", "Técnico en Química y Farmacia", NivelAcademico.TECNICO),

        // Ingeniería y Recursos Naturales
        Carrera("ING_01", "INGEN", "Ingeniería Agrícola", NivelAcademico.PROFESIONAL),
        Carrera("ING_02", "INGEN", "Ingeniería en Electricidad y Automatización Industrial", NivelAcademico.PROFESIONAL),
        Carrera("ING_03", "INGEN", "Ingeniería en Mantenimiento Industrial", NivelAcademico.PROFESIONAL),
        Carrera("ING_04", "INGEN", "Ingeniería en Maquinaria y Vehículos Pesados", NivelAcademico.PROFESIONAL),
        Carrera("ING_05", "INGEN", "Ingeniería en Mecánica Automotriz y Autotrónica", NivelAcademico.PROFESIONAL),
        Carrera("ING_06", "INGEN", "Ingeniería en Recursos Naturales y Sostenibilidad", NivelAcademico.PROFESIONAL),
        Carrera("ING_07", "INGEN", "Técnico en Calidad de Alimentos", NivelAcademico.TECNICO),
        Carrera("ING_08", "INGEN", "Técnico en Desabolladura y Pintura Automotriz", NivelAcademico.TECNICO),
        Carrera("ING_09", "INGEN", "Técnico en Electricidad y Automatización Industrial", NivelAcademico.TECNICO),
        Carrera("ING_10", "INGEN", "Técnico en Electricidad y Energías Renovables", NivelAcademico.TECNICO),
        Carrera("ING_11", "INGEN", "Técnico en Geología", NivelAcademico.TECNICO),
        Carrera("ING_12", "INGEN", "Técnico en Mantenimiento Industrial", NivelAcademico.TECNICO),
        Carrera("ING_13", "INGEN", "Técnico en Maquinaria y Vehículos Pesados", NivelAcademico.TECNICO),
        Carrera("ING_14", "INGEN", "Técnico en Mecánica Automotriz y Autotrónica", NivelAcademico.TECNICO),
        Carrera("ING_15", "INGEN", "Técnico en Operación y Supervisión de Procesos Mineros", NivelAcademico.TECNICO),
        Carrera("ING_16", "INGEN", "Técnico Veterinario y Pecuario", NivelAcademico.TECNICO),

        // Turismo y Hospitalidad
        Carrera("TUR_01", "TURIS", "Administración en Turismo y Hospitalidad", NivelAcademico.PROFESIONAL),
        Carrera("TUR_02", "TURIS", "Técnico en Turismo y Hospitalidad", NivelAcademico.TECNICO),

        // Duoc Online
        Carrera("ONL_01", "ONLIN", "Administración Pública", NivelAcademico.PROFESIONAL),
        Carrera("ONL_02", "ONLIN", "Auditoría y Análisis de Negocios", NivelAcademico.PROFESIONAL),
        Carrera("ONL_03", "ONLIN", "Ingeniería en Administración de Seguridad Privada", NivelAcademico.PROFESIONAL),
        Carrera("ONL_04", "ONLIN", "Ingeniería en Control de Gestión", NivelAcademico.PROFESIONAL),
        Carrera("ONL_05", "ONLIN", "Ingeniería en Gestión de Personas", NivelAcademico.PROFESIONAL),
        Carrera("ONL_06", "ONLIN", "Ingeniería en Marketing Digital", NivelAcademico.PROFESIONAL),
        Carrera("ONL_07", "ONLIN", "Relaciones Públicas y Comunicación Organizacional", NivelAcademico.PROFESIONAL),
        Carrera("ONL_08", "ONLIN", "Ingeniería en Prevención de Riesgos", NivelAcademico.PROFESIONAL),
        Carrera("ONL_09", "ONLIN", "Ingeniería en Ciberseguridad", NivelAcademico.PROFESIONAL),
        Carrera("ONL_10", "ONLIN", "Ingeniería en Ciencia de Datos", NivelAcademico.PROFESIONAL),
        Carrera("ONL_11", "ONLIN", "Ingeniería en Desarrollo de Software", NivelAcademico.PROFESIONAL),
        Carrera("ONL_12", "ONLIN", "Ingeniería en Automatización y Control Industrial", NivelAcademico.PROFESIONAL),
        Carrera("ONL_13", "ONLIN", "Ingeniería en Minas", NivelAcademico.PROFESIONAL),
        Carrera("ONL_14", "ONLIN", "Ingeniería Industrial", NivelAcademico.PROFESIONAL),
        Carrera("ONL_15", "ONLIN", "Administración de Empresas", NivelAcademico.TECNICO),
        Carrera("ONL_16", "ONLIN", "Contabilidad Tributaria", NivelAcademico.TECNICO),
        Carrera("ONL_17", "ONLIN", "Técnico en Operaciones Logísticas", NivelAcademico.TECNICO),
        Carrera("ONL_18", "ONLIN", "Técnico en Trabajo Social", NivelAcademico.TECNICO),
        Carrera("ONL_19", "ONLIN", "Técnico en Prevención de Riesgos Laborales", NivelAcademico.TECNICO),
        Carrera("ONL_20", "ONLIN", "Desarrollo y Diseño Web", NivelAcademico.TECNICO),
        Carrera("ONL_21", "ONLIN", "Analista Programador Computacional", NivelAcademico.TECNICO),
        Carrera("ONL_22", "ONLIN", "Desarrollo de Aplicaciones", NivelAcademico.TECNICO),
        Carrera("ONL_23", "ONLIN", "Técnico en Control y Monitoreo Remoto de Procesos Mineros", NivelAcademico.TECNICO)
    )

    val sedes: List<Sede> = listOf(
        // Región Metropolitana
        Sede("RM_01", "Alameda (Santiago Centro)", "Región Metropolitana"),
        Sede("RM_02", "Antonio Varas (Providencia)", "Región Metropolitana"),
        Sede("RM_03", "Maipú", "Región Metropolitana"),
        Sede("RM_04", "Melipilla", "Región Metropolitana"),
        Sede("RM_05", "Padre Alonso de Ovalle (Santiago Centro)", "Región Metropolitana"),
        Sede("RM_06", "Plaza Norte (Huechuraba)", "Región Metropolitana"),
        Sede("RM_07", "Plaza Oeste (Cerrillos)", "Región Metropolitana"),
        Sede("RM_08", "Plaza Vespucio (La Florida)", "Región Metropolitana"),
        Sede("RM_09", "Puente Alto", "Región Metropolitana"),
        Sede("RM_10", "San Bernardo", "Región Metropolitana"),
        Sede("RM_11", "San Carlos de Apoquindo (Las Condes)", "Región Metropolitana"),
        Sede("RM_12", "San Joaquín", "Región Metropolitana"),

        // Región de Valparaíso
        Sede("VAL_01", "Valparaíso", "Región de Valparaíso"),
        Sede("VAL_02", "Viña del Mar", "Región de Valparaíso"),

        // Región del Biobío
        Sede("BIO_01", "Concepción", "Región del Biobío"),
        Sede("BIO_02", "Campus Arauco", "Región del Biobío"),
        Sede("BIO_03", "Campus Nacimiento", "Región del Biobío"),

        // Región de la Araucanía
        Sede("ARA_01", "Campus Villarrica", "Región de la Araucanía"),

        // Región de los Lagos
        Sede("LAG_01", "Puerto Montt", "Región de los Lagos")
    )

    fun getCarrerasByEscuela(escuelaId: String): List<Carrera> =
        carreras.filter { it.escuelaId == escuelaId }
}

package com.therionlabs.captura_digital_v2.data.local.mapper

import com.therionlabs.captura_digital_v2.data.local.entity.ProspectoEntity
import com.therionlabs.captura_digital_v2.data.model.Prospecto

/**
 * Funciones de extensión de mapeo bidireccional entre la entidad de persistencia
 * (ProspectoEntity) y el modelo de dominio de negocio (Prospecto).
 * Mantiene la capa de dominio completamente agnóstica de SQLite y Room.
 */

fun ProspectoEntity.toDomain(): Prospecto = Prospecto(
    id = id,
    captadorId = captadorId,
    eventoId = eventoId,
    fechaCaptura = fechaCaptura,
    syncStatus = syncStatus,
    consentimientoDatos = consentimientoDatos,
    rut = rut,
    nombres = nombres,
    apellidos = apellidos,
    genero = genero,
    fechaNacimiento = fechaNacimiento,
    comunaResidencia = comunaResidencia,
    celular = celular,
    email = email,
    situacionAcademica = situacionAcademica,
    situacionAcademicaOtro = situacionAcademicaOtro,
    establecimientoEducacional = establecimientoEducacional,
    comunaEstablecimiento = comunaEstablecimiento,
    jornadaPreferencia = jornadaPreferencia,
    sedeInteres = sedeInteres,
    nivelInteres = nivelInteres,
    perfilMotivacional = perfilMotivacional,
    carreraPrioridad1 = carreraPrioridad1,
    carreraPrioridad2 = carreraPrioridad2,
    carreraPrioridad3 = carreraPrioridad3
)

fun Prospecto.toEntity(): ProspectoEntity = ProspectoEntity(
    id = id,
    captadorId = captadorId,
    eventoId = eventoId,
    fechaCaptura = fechaCaptura,
    syncStatus = syncStatus,
    consentimientoDatos = consentimientoDatos,
    rut = rut,
    nombres = nombres,
    apellidos = apellidos,
    genero = genero,
    fechaNacimiento = fechaNacimiento,
    comunaResidencia = comunaResidencia,
    celular = celular,
    email = email,
    situacionAcademica = situacionAcademica,
    situacionAcademicaOtro = situacionAcademicaOtro,
    establecimientoEducacional = establecimientoEducacional,
    comunaEstablecimiento = comunaEstablecimiento,
    jornadaPreferencia = jornadaPreferencia,
    sedeInteres = sedeInteres,
    nivelInteres = nivelInteres,
    perfilMotivacional = perfilMotivacional,
    carreraPrioridad1 = carreraPrioridad1,
    carreraPrioridad2 = carreraPrioridad2,
    carreraPrioridad3 = carreraPrioridad3
)

fun List<ProspectoEntity>.toDomainList(): List<Prospecto> = map { it.toDomain() }

fun List<Prospecto>.toEntityList(): List<ProspectoEntity> = map { it.toEntity() }

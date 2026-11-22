package com.therionlabs.msformulario.mapper;

import com.therionlabs.msformulario.dto.CarreraPreferenciaDto;
import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.EventoResponseDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoResponseDto;
import com.therionlabs.msformulario.model.CarreraPreferencia;
import com.therionlabs.msformulario.model.Evento;
import com.therionlabs.msformulario.model.Prospecto;
import com.therionlabs.msformulario.model.enums.Genero;
import com.therionlabs.msformulario.model.enums.Jornada;
import com.therionlabs.msformulario.model.enums.NivelInteres;
import com.therionlabs.msformulario.model.enums.PerfilMotivacional;
import com.therionlabs.msformulario.model.enums.SituacionAcademica;
import com.therionlabs.msformulario.util.RutValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProspectoMapper {

    public Prospecto toNewEntity(ProspectoRequestDto dto) {
        Prospecto entity = new Prospecto();
        if (dto.clientUuid() != null && !dto.clientUuid().isBlank()) {
            entity.setClientUuid(dto.clientUuid().trim());
        }
        applyFields(entity, dto);
        return entity;
    }

    public void updateEntity(Prospecto entity, ProspectoRequestDto dto) {
        applyFields(entity, dto);
    }

    private void applyFields(Prospecto entity, ProspectoRequestDto dto) {
        entity.setCaptadorId(dto.captadorId());
        entity.setEventoId(dto.eventoId());
        entity.setTimestampEpoch(dto.timestampEpoch() != null ? dto.timestampEpoch() : System.currentTimeMillis());
        entity.setConsentimientoDatos(dto.consentimientoDatos());
        entity.setRut(RutValidator.normalize(dto.rut()));
        entity.setNombres(dto.nombres().trim());
        entity.setApellidos(dto.apellidos().trim());
        entity.setGenero(parseEnum(Genero.class, dto.genero(), "genero"));
        entity.setFechaNacimiento(dto.fechaNacimiento());
        entity.setComunaResidencia(dto.comunaResidencia().trim());
        entity.setCelular(dto.celular() != null ? dto.celular().trim() : "");
        entity.setEmail(dto.email() != null ? dto.email().trim().toLowerCase() : "");
        entity.setSituacionAcademica(parseEnum(SituacionAcademica.class, dto.situacionAcademica(), "situacionAcademica"));
        entity.setSituacionAcademicaOtro(dto.situacionAcademicaOtro());
        entity.setEstablecimientoEducacional(dto.establecimientoEducacional().trim());
        entity.setComunaEstablecimiento(dto.comunaEstablecimiento().trim());
        entity.setJornadaPreferencia(parseEnum(Jornada.class, dto.jornadaPreferencia(), "jornadaPreferencia"));
        entity.setSedeInteres(dto.sedeInteres().trim());
        entity.setNivelInteres(parseEnum(NivelInteres.class, dto.nivelInteres(), "nivelInteres"));
        entity.setPerfilMotivacional(parseEnum(PerfilMotivacional.class, dto.perfilMotivacional(), "perfilMotivacional"));

        List<CarreraPreferencia> carreras = dto.carreras().stream()
                .map(c -> new CarreraPreferencia(c.prioridad(), c.nombreCarrera().trim()))
                .toList();
        entity.setCarreras(carreras);
    }

    public ProspectoResponseDto toResponseDto(Prospecto entity) {
        List<CarreraPreferenciaDto> carrerasDto = entity.getCarreras().stream()
                .map(c -> new CarreraPreferenciaDto(c.getPrioridad(), c.getNombreCarrera()))
                .toList();

        return new ProspectoResponseDto(
                entity.getClientUuid(),
                entity.getCaptadorId(),
                entity.getEventoId(),
                entity.getTimestampEpoch(),
                entity.getRut(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getGenero().name(),
                entity.getFechaNacimiento(),
                entity.getComunaResidencia(),
                entity.getCelular(),
                entity.getEmail(),
                entity.isConsentimientoDatos(),
                entity.getSituacionAcademica().name(),
                entity.getSituacionAcademicaOtro(),
                entity.getEstablecimientoEducacional(),
                entity.getComunaEstablecimiento(),
                entity.getJornadaPreferencia().name(),
                entity.getSedeInteres(),
                entity.getNivelInteres().name(),
                entity.getPerfilMotivacional().name(),
                carrerasDto,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public Evento toEventoEntity(EventoRequestDto dto) {
        Evento evento = new Evento();
        if (dto.id() != null && !dto.id().isBlank()) {
            evento.setId(dto.id().trim());
        }
        updateEventoEntity(evento, dto);
        return evento;
    }

    public void updateEventoEntity(Evento evento, EventoRequestDto dto) {
        evento.setNombre(dto.nombre().trim());
        evento.setEstablecimiento(dto.establecimiento().trim());
        evento.setComuna(dto.comuna().trim());
        evento.setSedeResponsable(dto.sedeResponsable().trim());
        evento.setFechaEvento(dto.fechaEvento());
        if (dto.activo() != null) {
            evento.setActivo(dto.activo());
        }
    }

    public EventoResponseDto toEventoResponseDto(Evento evento) {
        return new EventoResponseDto(
                evento.getId(),
                evento.getNombre(),
                evento.getEstablecimiento(),
                evento.getComuna(),
                evento.getSedeResponsable(),
                evento.getFechaEvento(),
                evento.isActivo(),
                evento.getCreatedAt()
        );
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String rawValue, String fieldName) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("El campo '" + fieldName + "' es obligatorio");
        }
        try {
            return Enum.valueOf(enumType, rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Valor inválido para '" + fieldName + "': " + rawValue);
        }
    }
}

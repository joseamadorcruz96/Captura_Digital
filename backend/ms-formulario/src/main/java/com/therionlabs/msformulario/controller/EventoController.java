package com.therionlabs.msformulario.controller;

import com.therionlabs.msformulario.dto.EventoRequestDto;
import com.therionlabs.msformulario.dto.EventoResponseDto;
import com.therionlabs.msformulario.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Gestión de ferias y colegios donde operan los captadores")
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Opcionalmente filtra solo eventos activos")
    public List<EventoResponseDto> listar(@RequestParam(required = false) Boolean activo) {
        return eventoService.listar(activo);
    }

    @PostMapping
    @Operation(summary = "Crear evento")
    public ResponseEntity<EventoResponseDto> crear(@Valid @RequestBody EventoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventoService.crear(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar evento")
    public EventoResponseDto actualizar(
            @PathVariable String id,
            @Valid @RequestBody EventoRequestDto request
    ) {
        return eventoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar evento")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        eventoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

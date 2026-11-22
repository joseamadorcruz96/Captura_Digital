package com.therionlabs.msformulario.controller;

import com.therionlabs.msformulario.dto.BatchSyncRequestDto;
import com.therionlabs.msformulario.dto.BatchSyncResponseDto;
import com.therionlabs.msformulario.dto.DashboardResumenDto;
import com.therionlabs.msformulario.dto.ProspectoRequestDto;
import com.therionlabs.msformulario.dto.ProspectoResponseDto;
import com.therionlabs.msformulario.service.ProspectoService;
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
@RequestMapping("/api/v1/prospectos")
@RequiredArgsConstructor
@Tag(name = "Prospectos", description = "CRUD, métricas y sincronización Offline-First de formularios")
public class ProspectoController {

    private final ProspectoService prospectoService;

    @GetMapping
    @Operation(summary = "Listar o filtrar prospectos")
    public List<ProspectoResponseDto> listar(
            @RequestParam(required = false) Integer captadorId,
            @RequestParam(required = false) String eventoId,
            @RequestParam(required = false) String rut,
            @RequestParam(required = false) String sede
    ) {
        return prospectoService.listar(captadorId, eventoId, rut, sede);
    }

    @GetMapping("/resumen")
    @Operation(summary = "Métricas agregadas para Dashboard")
    public DashboardResumenDto resumen(@RequestParam(required = false) String eventoId) {
        return prospectoService.obtenerResumen(eventoId);
    }

    @GetMapping("/{clientUuid}")
    @Operation(summary = "Obtener prospecto por clientUuid")
    public ProspectoResponseDto obtener(@PathVariable String clientUuid) {
        return prospectoService.obtener(clientUuid);
    }

    @PostMapping
    @Operation(summary = "Crear prospecto unitario")
    public ResponseEntity<ProspectoResponseDto> crear(@Valid @RequestBody ProspectoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prospectoService.crear(request));
    }

    @PutMapping("/{clientUuid}")
    @Operation(summary = "Actualizar prospecto completo")
    public ProspectoResponseDto actualizar(
            @PathVariable String clientUuid,
            @Valid @RequestBody ProspectoRequestDto request
    ) {
        return prospectoService.actualizar(clientUuid, request);
    }

    @DeleteMapping("/{clientUuid}")
    @Operation(summary = "Eliminar prospecto")
    public ResponseEntity<Void> eliminar(@PathVariable String clientUuid) {
        prospectoService.eliminar(clientUuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronización por lote Offline-First (upsert por RUT y resultados parciales)")
    public BatchSyncResponseDto sincronizarLote(@Valid @RequestBody BatchSyncRequestDto request) {
        return prospectoService.sincronizarLote(request);
    }
}

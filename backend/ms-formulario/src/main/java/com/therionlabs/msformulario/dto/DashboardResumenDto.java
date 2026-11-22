package com.therionlabs.msformulario.dto;

import java.util.Map;

public record DashboardResumenDto(
        long totalProspectos,
        Map<String, Long> conteoPorSede,
        Map<String, Long> conteoPorNivelInteres,
        Map<String, Long> conteoPorJornada
) {}

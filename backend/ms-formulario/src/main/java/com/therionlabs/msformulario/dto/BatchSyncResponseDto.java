package com.therionlabs.msformulario.dto;

import java.util.List;
import java.util.Map;

public record BatchSyncResponseDto(
        int totalProcesados,
        List<String> exitososUuids,
        List<String> fallidosUuids,
        String mensaje,
        Map<String, String> erroresPorUuid
) {}

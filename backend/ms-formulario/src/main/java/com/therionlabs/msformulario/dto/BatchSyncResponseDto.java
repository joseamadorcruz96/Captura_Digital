package com.therionlabs.msformulario.dto;

import java.util.List;

public record BatchSyncResponseDto(
        int totalProcesados,
        List<String> exitososUuids,
        List<String> fallidosUuids,
        String mensaje
) {}

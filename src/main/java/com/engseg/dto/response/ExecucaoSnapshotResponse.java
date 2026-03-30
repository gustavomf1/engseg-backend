package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExecucaoSnapshotResponse(
        UUID id,
        String descricaoExecucao,
        LocalDateTime dataSubmissao,
        String status,
        String comentarioRevisao,
        List<EvidenciaResponse> evidencias
) {}

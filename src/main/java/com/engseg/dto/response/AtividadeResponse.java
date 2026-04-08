package com.engseg.dto.response;

import java.util.List;
import java.util.UUID;

public record AtividadeResponse(
        UUID id,
        String titulo,
        String descricao,
        Integer ordem,
        String status,
        String motivoRejeicao,
        String descricaoExecucao,
        String statusExecucao,
        String motivoRejeicaoExecucao,
        List<EvidenciaResponse> evidencias
) {}

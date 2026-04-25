package com.engseg.dto.response;

import com.engseg.entity.StatusTratativaDesvio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TrativaDesvioResponse(
        UUID id,
        String titulo,
        String descricao,
        List<EvidenciaInfo> evidencias,
        StatusTratativaDesvio status,
        String motivoReprovacao,
        Integer numero,
        Integer rodada,
        LocalDateTime dtCriacao
) {
    public record EvidenciaInfo(UUID id, String nome, String url) {}
}

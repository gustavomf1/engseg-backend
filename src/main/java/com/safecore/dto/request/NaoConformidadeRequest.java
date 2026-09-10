package com.safecore.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record NaoConformidadeRequest(
        @NotNull UUID estabelecimentoId,
        @NotBlank String titulo,
        @NotNull UUID localizacaoId,
        String descricao,
        @Min(1) @Max(5) Integer severidade,
        @Min(1) @Max(4) Integer probabilidade,
        UUID responsavelTrativaId,
        UUID responsavelNcId,
        boolean regraDeOuro,
        List<UUID> normaIds,
        boolean reincidencia,
        UUID ncAnteriorId,
        List<String> emailsManuais,
        List<String> emailsPadraoExcluidos,
        @NotNull UUID empresaContratadaId
) {}

package com.engseg.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public record NaoConformidadeRequest(
        @NotNull UUID estabelecimentoId,
        @NotBlank String titulo,
        UUID localizacaoId,
        @NotBlank String descricao,
        @Min(1) @Max(5) int severidade,
        @Min(1) @Max(4) int probabilidade,
        UUID engResponsavelConstrutoraId,
        UUID engResponsavelVerificacaoId,
        boolean regraDeOuro,
        List<UUID> normaIds,
        boolean reincidencia,
        UUID ncAnteriorId,
        List<String> emailsManuais,
        List<String> emailsPadraoExcluidos
) {}

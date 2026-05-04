package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record DesvioRequest(
        @NotNull UUID estabelecimentoId,
        @NotBlank String titulo,
        UUID localizacaoId,
        @NotBlank String descricao,
        @NotBlank String orientacaoRealizada,
        boolean regraDeOuro,
        @NotNull UUID responsavelDesvioId,
        @NotNull UUID responsavelTratativaId,
        List<String> emailsManuais,
        List<String> emailsPadraoExcluidos,
        UUID empresaContratadaId
) {}

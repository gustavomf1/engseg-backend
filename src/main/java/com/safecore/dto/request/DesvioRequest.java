package com.safecore.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record DesvioRequest(
        @NotNull UUID estabelecimentoId,
        @NotBlank String titulo,
        @NotNull UUID localizacaoId,
        String descricao,
        String orientacaoRealizada,
        boolean regraDeOuro,
        UUID responsavelDesvioId,
        UUID responsavelTratativaId,
        List<String> emailsManuais,
        List<String> emailsPadraoExcluidos,
        @NotNull UUID empresaContratadaId
) {}

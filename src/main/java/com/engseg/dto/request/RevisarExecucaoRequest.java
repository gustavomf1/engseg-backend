package com.engseg.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RevisarExecucaoRequest(
        @NotEmpty List<@Valid DecisaoExecucao> decisoes,
        String comentario,
        List<String> emailsManuais
) {
    public record DecisaoExecucao(
            @NotNull UUID atividadeId,
            @NotNull String status,
            String motivo
    ) {}
}

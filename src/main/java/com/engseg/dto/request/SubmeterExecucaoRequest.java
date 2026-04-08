package com.engseg.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SubmeterExecucaoRequest(
        @NotEmpty List<@Valid AtividadeExecucao> atividades
) {
    public record AtividadeExecucao(
            @NotNull UUID atividadeId,
            String descricaoExecucao
    ) {}
}

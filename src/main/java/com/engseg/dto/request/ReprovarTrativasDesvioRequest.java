package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ReprovarTrativasDesvioRequest(
        @NotNull @NotEmpty List<ItemReprovacao> itens,
        List<String> emailsManuais
) {
    public record ItemReprovacao(
            @NotNull UUID trativaId,
            @NotBlank String motivo
    ) {}
}

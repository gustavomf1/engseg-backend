package com.engseg.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RejeitarAtividadesRequest(
        @NotEmpty List<@Valid ItemRejeicao> atividades,
        String comentarioGeral
) {
    public record ItemRejeicao(
            @NotNull UUID atividadeId,
            @NotBlank String motivo
    ) {}
}

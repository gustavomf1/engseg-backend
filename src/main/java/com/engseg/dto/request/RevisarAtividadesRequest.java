package com.engseg.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RevisarAtividadesRequest(
        @NotEmpty List<@Valid DecisaoAtividade> decisoes,
        String comentario
) {
    public record DecisaoAtividade(
            @NotNull UUID atividadeId,
            @NotNull String status,   // "APROVADA" ou "REJEITADA"
            String motivo             // obrigatório quando REJEITADA
    ) {}
}

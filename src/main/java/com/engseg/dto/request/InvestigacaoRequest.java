package com.engseg.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InvestigacaoRequest(
        @NotEmpty @Size(min = 1, max = 5) List<@Valid PorqueItem> porques,
        @NotBlank String causaRaiz,
        @NotEmpty List<@Valid AtividadeItem> atividades
) {
    public record PorqueItem(
            @NotBlank String pergunta,
            @NotBlank String resposta
    ) {}

    public record AtividadeItem(
            @NotBlank String titulo,
            @NotBlank String descricao
    ) {}
}

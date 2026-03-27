package com.engseg.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record InvestigacaoRequest(
        @NotBlank String porqueUm,
        @NotBlank String porqueUmResposta,
        @NotBlank String porqueDois,
        @NotBlank String porqueDoisResposta,
        @NotBlank String porqueTres,
        @NotBlank String porqueTresResposta,
        @NotBlank String porqueQuatro,
        @NotBlank String porqueQuatroResposta,
        @NotBlank String porqueCinco,
        @NotBlank String porqueCincoResposta,
        @NotBlank String causaRaiz,
        @NotEmpty List<@NotBlank String> atividades
) {}

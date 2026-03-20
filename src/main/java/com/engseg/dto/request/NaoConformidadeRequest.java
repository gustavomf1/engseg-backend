package com.engseg.dto.request;

import com.engseg.entity.NivelSeveridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NaoConformidadeRequest(
        @NotNull UUID estabelecimentoId,
        @NotBlank String titulo,
        UUID localizacaoId,
        @NotBlank String descricao,
        @NotBlank String nrRelacionada,
        @NotNull NivelSeveridade nivelSeveridade,
        UUID engResponsavelConstrutoraId,
        UUID engResponsavelVerificacaoId,
        boolean regraDeOuro
) {}

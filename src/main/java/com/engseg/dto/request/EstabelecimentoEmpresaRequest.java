package com.engseg.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EstabelecimentoEmpresaRequest(
        @NotNull UUID empresaId
) {}

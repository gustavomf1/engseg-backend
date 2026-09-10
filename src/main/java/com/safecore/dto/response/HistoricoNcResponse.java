package com.safecore.dto.response;

import com.safecore.entity.StatusNaoConformidade;
import com.safecore.entity.TipoAcaoHistorico;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoNcResponse(
        UUID id,
        TipoAcaoHistorico acao,
        String usuarioNome,
        String comentario,
        StatusNaoConformidade statusAnterior,
        StatusNaoConformidade statusAtual,
        LocalDateTime dataAcao
) {}

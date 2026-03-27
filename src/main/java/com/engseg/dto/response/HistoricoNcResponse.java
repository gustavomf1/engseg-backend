package com.engseg.dto.response;

import com.engseg.entity.StatusNaoConformidade;
import com.engseg.entity.TipoAcaoHistorico;
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

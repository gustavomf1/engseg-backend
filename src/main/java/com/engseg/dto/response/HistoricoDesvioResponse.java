package com.engseg.dto.response;

import com.engseg.entity.StatusDesvio;
import com.engseg.entity.TipoAcaoHistoricoDesvio;
import java.time.LocalDateTime;
import java.util.UUID;

public record HistoricoDesvioResponse(
        UUID id,
        TipoAcaoHistoricoDesvio tipo,
        String usuarioNome,
        String comentario,
        StatusDesvio statusAnterior,
        StatusDesvio statusAtual,
        String snapshotObservacao,
        UUID snapshotEvidenciaId,
        LocalDateTime dataAcao
) {}

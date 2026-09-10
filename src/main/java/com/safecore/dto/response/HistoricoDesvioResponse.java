package com.safecore.dto.response;

import com.safecore.entity.StatusDesvio;
import com.safecore.entity.TipoAcaoHistoricoDesvio;
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

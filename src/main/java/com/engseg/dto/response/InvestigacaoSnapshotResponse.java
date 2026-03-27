package com.engseg.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvestigacaoSnapshotResponse(
        UUID id,
        String porqueUm,
        String porqueUmResposta,
        String porqueDois,
        String porqueDoisResposta,
        String porqueTres,
        String porqueTresResposta,
        String porqueQuatro,
        String porqueQuatroResposta,
        String porqueCinco,
        String porqueCincoResposta,
        String causaRaiz,
        List<String> atividades,
        LocalDateTime dataSubmissao,
        String status,
        String comentarioRevisao
) {}

package com.engseg.util;

import com.engseg.entity.NivelRisco;

public final class MatrizRisco {

    // Índice [severidade 1-5][probabilidade 1-4]
    // Posição 0 não é usada em nenhum eixo.
    private static final NivelRisco[][] MATRIZ = {
        {},  // severidade 0 — não usada
        { null, NivelRisco.BAIXO,    NivelRisco.BAIXO,    NivelRisco.BAIXO,    NivelRisco.MODERADO }, // S=1
        { null, NivelRisco.BAIXO,    NivelRisco.BAIXO,    NivelRisco.MODERADO, NivelRisco.ALTO     }, // S=2
        { null, NivelRisco.BAIXO,    NivelRisco.MODERADO, NivelRisco.ALTO,     NivelRisco.CRITICO  }, // S=3
        { null, NivelRisco.BAIXO,    NivelRisco.MODERADO, NivelRisco.ALTO,     NivelRisco.CRITICO  }, // S=4
        { null, NivelRisco.MODERADO, NivelRisco.ALTO,     NivelRisco.ALTO,     NivelRisco.CRITICO  }, // S=5
    };

    private MatrizRisco() {}

    public static NivelRisco calcular(int severidade, int probabilidade) {
        if (severidade < 1 || severidade > 5) {
            throw new IllegalArgumentException("Severidade deve ser entre 1 e 5, recebido: " + severidade);
        }
        if (probabilidade < 1 || probabilidade > 4) {
            throw new IllegalArgumentException("Probabilidade deve ser entre 1 e 4, recebido: " + probabilidade);
        }
        return MATRIZ[severidade][probabilidade];
    }
}

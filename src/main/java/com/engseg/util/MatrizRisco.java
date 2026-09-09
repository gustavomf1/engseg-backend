package com.engseg.util;

import com.engseg.entity.NivelRisco;

public final class MatrizRisco {

    private static final NivelRisco[][] MATRIZ = {
        {},
        { null, NivelRisco.BAIXO,    NivelRisco.BAIXO,    NivelRisco.BAIXO,    NivelRisco.MODERADO },
        { null, NivelRisco.BAIXO,    NivelRisco.BAIXO,    NivelRisco.MODERADO, NivelRisco.ALTO     },
        { null, NivelRisco.BAIXO,    NivelRisco.MODERADO, NivelRisco.ALTO,     NivelRisco.CRITICO  },
        { null, NivelRisco.BAIXO,    NivelRisco.MODERADO, NivelRisco.ALTO,     NivelRisco.CRITICO  },
        { null, NivelRisco.MODERADO, NivelRisco.ALTO,     NivelRisco.ALTO,     NivelRisco.CRITICO  },
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

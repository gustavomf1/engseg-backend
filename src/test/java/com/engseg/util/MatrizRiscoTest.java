package com.engseg.util;

import com.engseg.entity.NivelRisco;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MatrizRiscoTest {

    @Test
    void severidade1_probabilidade1_deveSerBaixo() {
        assertThat(MatrizRisco.calcular(1, 1)).isEqualTo(NivelRisco.BAIXO);
    }

    @Test
    void severidade1_probabilidade4_deveSerModerado() {
        assertThat(MatrizRisco.calcular(1, 4)).isEqualTo(NivelRisco.MODERADO);
    }

    @Test
    void severidade2_probabilidade4_deveSerAlto() {
        assertThat(MatrizRisco.calcular(2, 4)).isEqualTo(NivelRisco.ALTO);
    }

    @Test
    void severidade3_probabilidade4_deveSerCritico() {
        assertThat(MatrizRisco.calcular(3, 4)).isEqualTo(NivelRisco.CRITICO);
    }

    @Test
    void severidade5_probabilidade1_deveSerModerado() {
        assertThat(MatrizRisco.calcular(5, 1)).isEqualTo(NivelRisco.MODERADO);
    }

    @Test
    void severidade5_probabilidade4_deveSerCritico() {
        assertThat(MatrizRisco.calcular(5, 4)).isEqualTo(NivelRisco.CRITICO);
    }

    @Test
    void severidade4_probabilidade2_deveSerModerado() {
        assertThat(MatrizRisco.calcular(4, 2)).isEqualTo(NivelRisco.MODERADO);
    }

    @Test
    void severidade3_probabilidade3_deveSerAlto() {
        assertThat(MatrizRisco.calcular(3, 3)).isEqualTo(NivelRisco.ALTO);
    }

    @Test
    void severidadeInvalida_deveLancarExcecao() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> MatrizRisco.calcular(0, 1)
        );
    }

    @Test
    void probabilidadeInvalida_deveLancarExcecao() {
        org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> MatrizRisco.calcular(1, 5)
        );
    }
}

package com.engseg.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void admin_defaultIsFalse() {
        var usuario = Usuario.builder()
                .nome("Teste")
                .email("teste@teste.com")
                .senha("senha")
                .perfil(PerfilUsuario.ENGENHEIRO)
                .build();
        assertThat(usuario.isAdmin()).isFalse();
    }

    @Test
    void admin_canBeSetToTrue() {
        var usuario = Usuario.builder()
                .nome("Admin")
                .email("admin@teste.com")
                .senha("senha")
                .perfil(PerfilUsuario.ENGENHEIRO)
                .admin(true)
                .build();
        assertThat(usuario.isAdmin()).isTrue();
    }
}

package com.safecore.security;

import com.safecore.entity.Empresa;
import com.safecore.entity.PerfilUsuario;
import com.safecore.entity.Usuario;
import com.safecore.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    private Usuario buildUsuario(boolean admin) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Engenheiro")
                .email("eng@teste.com")
                .senha("$2a$10$hash")
                .perfil(PerfilUsuario.ENGENHEIRO)
                .empresa(Empresa.builder().id(UUID.randomUUID()).build())
                .admin(admin)
                .build();
    }

    @Test
    void loadUser_nonAdmin_hasOnlyEngenheiroRole() {
        when(usuarioRepository.findByEmail("eng@teste.com"))
                .thenReturn(Optional.of(buildUsuario(false)));

        var details = service.loadUserByUsername("eng@teste.com");

        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly("ROLE_ENGENHEIRO");
    }

    @Test
    void loadUser_admin_hasEngenheiroAndAdminRoles() {
        when(usuarioRepository.findByEmail("eng@teste.com"))
                .thenReturn(Optional.of(buildUsuario(true)));

        var details = service.loadUserByUsername("eng@teste.com");

        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ENGENHEIRO", "ROLE_ADMIN");
    }
}

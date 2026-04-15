package com.engseg.service;

import com.engseg.dto.request.LoginRequest;
import com.engseg.entity.Empresa;
import com.engseg.entity.PerfilUsuario;
import com.engseg.entity.Usuario;
import com.engseg.repository.UsuarioRepository;
import com.engseg.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UsuarioRepository usuarioRepository;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    private Usuario buildUsuario(boolean admin) {
        return Usuario.builder()
                .id(UUID.randomUUID())
                .nome("Eng Admin")
                .email("admin@eng.com")
                .senha("hash")
                .perfil(PerfilUsuario.ENGENHEIRO)
                .empresa(Empresa.builder().id(UUID.randomUUID()).build())
                .admin(admin)
                .build();
    }

    @Test
    void login_adminUser_responseContainsIsAdminTrue() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByEmail("admin@eng.com")).thenReturn(Optional.of(buildUsuario(true)));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("admin@eng.com", "senha"));

        assertThat(response.isAdmin()).isTrue();
    }

    @Test
    void login_nonAdminUser_responseContainsIsAdminFalse() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(usuarioRepository.findByEmail("admin@eng.com")).thenReturn(Optional.of(buildUsuario(false)));
        when(jwtService.generateToken(any(), any())).thenReturn("jwt-token");

        var response = authService.login(new LoginRequest("admin@eng.com", "senha"));

        assertThat(response.isAdmin()).isFalse();
    }
}

package com.engseg.service;

import com.engseg.dto.request.CriarUsuarioDiretoRequest;
import com.engseg.entity.Empresa;
import com.engseg.entity.PerfilUsuario;
import com.engseg.entity.Usuario;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceDiretoTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UsuarioService usuarioService;

    @Test
    void criarDireto_isAdminTrueComPerfilTecnico_throwsBadRequest() {
        var empresaId = UUID.randomUUID();
        when(empresaRepository.findById(empresaId))
                .thenReturn(Optional.of(Empresa.builder().id(empresaId).build()));

        var request = new CriarUsuarioDiretoRequest(
                "João", "joao@teste.com", "senha123",
                PerfilUsuario.TECNICO, empresaId, true
        );

        assertThatThrownBy(() -> usuarioService.criarDireto(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("400");
    }

    @Test
    void criarDireto_isAdminTrueComPerfilEngenheiro_salvaCampoAdmin() {
        var empresaId = UUID.randomUUID();
        var empresa = Empresa.builder()
                .id(empresaId)
                .razaoSocial("Empresa Teste")
                .cnpj("00000000000100")
                .build();
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode("senha123")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new CriarUsuarioDiretoRequest(
                "Admin", "admin@eng.com", "senha123",
                PerfilUsuario.ENGENHEIRO, empresaId, true
        );

        // does not throw — creates admin user
        usuarioService.criarDireto(request);
    }
}

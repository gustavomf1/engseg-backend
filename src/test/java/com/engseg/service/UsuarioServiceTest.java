package com.engseg.service;

import com.engseg.dto.request.UsuarioRequest;
import com.engseg.entity.*;
import com.engseg.exception.BusinessException;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks
    UsuarioService service;

    private final UUID empresaId = UUID.randomUUID();

    private void autenticarComo(String role) {
        var auth = new UsernamePasswordAuthenticationToken(
                "caller@engseg.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private UsuarioRequest requestComPerfil(PerfilUsuario perfil) {
        return new UsuarioRequest("Nome Teste", "novo@engseg.com", "senha123", perfil, empresaId, null);
    }

    private Empresa empresaMock() {
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.setRazaoSocial("EngSeg");
        empresa.setCnpj("00.000.000/0001-00");
        return empresa;
    }

    private Usuario usuarioSalvoMock(PerfilUsuario perfil, Empresa empresa) {
        Usuario saved = new Usuario();
        saved.setId(UUID.randomUUID());
        saved.setNome("Nome Teste");
        saved.setEmail("novo@engseg.com");
        saved.setPerfil(perfil);
        saved.setEmpresa(empresa);
        saved.setAtivo(true);
        return saved;
    }

    // ─── create: bloqueio de escalada de privilégio ────────────────────────────

    @Test
    void create_engenheiroNaoPodeCriarOutroEngenheiro() {
        autenticarComo("ENGENHEIRO");
        // Empresa lookup acontece antes da verificação de privilégio
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresaMock()));

        assertThatThrownBy(() -> service.create(requestComPerfil(PerfilUsuario.ENGENHEIRO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Engenheiro não pode criar outro usuário com perfil ENGENHEIRO");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void create_tecnicoPodeCriarEngenheiro() {
        autenticarComo("TECNICO");
        Empresa empresa = empresaMock();
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any())).thenReturn(usuarioSalvoMock(PerfilUsuario.ENGENHEIRO, empresa));

        // Não deve lançar BusinessException
        service.create(requestComPerfil(PerfilUsuario.ENGENHEIRO));

        verify(usuarioRepository).save(any());
    }

    @Test
    void create_engenheiroPodeCriarTecnico() {
        autenticarComo("ENGENHEIRO");
        Empresa empresa = empresaMock();
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(usuarioRepository.save(any())).thenReturn(usuarioSalvoMock(PerfilUsuario.TECNICO, empresa));

        // Criar TECNICO é permitido para ENGENHEIRO
        service.create(requestComPerfil(PerfilUsuario.TECNICO));

        verify(usuarioRepository).save(any());
    }

    @Test
    void create_semSenha_lancaIllegalArgumentException() {
        autenticarComo("ENGENHEIRO");
        // Empresa lookup ocorre antes da verificação de senha
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresaMock()));

        UsuarioRequest req = new UsuarioRequest("Nome", "email@test.com", null, PerfilUsuario.TECNICO, empresaId, null);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Senha é obrigatória");

        verify(usuarioRepository, never()).save(any());
    }

    // ─── update: bloqueio de promoção de privilégio ───────────────────────────

    @Test
    void update_engenheiroNaoPodePromoverUsuarioParaEngenheiro() {
        autenticarComo("ENGENHEIRO");

        UUID userId = UUID.randomUUID();
        Empresa empresa = empresaMock();

        Usuario existente = new Usuario();
        existente.setId(userId);
        existente.setPerfil(PerfilUsuario.TECNICO); // era TECNICO, tentando promover para ENGENHEIRO
        existente.setEmpresa(empresa);

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(existente));
        // Empresa lookup ocorre antes da verificação de privilégio no update
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));

        assertThatThrownBy(() -> service.update(userId, requestComPerfil(PerfilUsuario.ENGENHEIRO)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Engenheiro não pode promover usuário para perfil ENGENHEIRO");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void update_engenheiroNaoPrecisaAlterarProprioEngenheiroParaEngenheiro() {
        // Engenheiro editando OUTRO engenheiro que já é ENGENHEIRO → não deve lançar
        // (regra: só bloqueia PROMOVER de outro perfil para ENGENHEIRO)
        autenticarComo("ENGENHEIRO");

        UUID userId = UUID.randomUUID();
        Empresa empresa = empresaMock();

        Usuario existente = new Usuario();
        existente.setId(userId);
        existente.setPerfil(PerfilUsuario.ENGENHEIRO); // já é ENGENHEIRO
        existente.setEmpresa(empresa);
        existente.setNome("Eng Antigo");
        existente.setEmail("eng@test.com");

        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(existente));
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(usuarioRepository.save(any())).thenReturn(existente);

        // Editar dados de quem já é ENGENHEIRO → permitido
        service.update(userId, requestComPerfil(PerfilUsuario.ENGENHEIRO));

        verify(usuarioRepository).save(any());
    }
}

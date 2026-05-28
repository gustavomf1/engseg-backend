package com.engseg.service;

import com.engseg.dto.request.NormaRequest;
import com.engseg.entity.Norma;
import com.engseg.entity.Usuario;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.repository.NormaRepository;
import com.engseg.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NormaServiceTest {

    @Mock NormaRepository normaRepository;
    @Mock NaoConformidadeRepository naoConformidadeRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock SecurityHelper securityHelper;
    @Mock ClaudeService claudeService;

    @InjectMocks
    NormaService service;

    @Test
    void create_populaCamposDeAuditoriaEContadoresNoResponse() {
        UUID normaId = UUID.randomUUID();
        Usuario usuario = usuario(UUID.randomUUID(), "Admin");

        when(securityHelper.getUsuarioLogado()).thenReturn(usuario);
        when(normaRepository.save(any(Norma.class))).thenAnswer(inv -> {
            Norma norma = inv.getArgument(0);
            norma.setId(normaId);
            return norma;
        });
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(naoConformidadeRepository.countByNormaId(normaId)).thenReturn(3L);
        when(naoConformidadeRepository.countAtivasByNormaId(normaId)).thenReturn(2L);

        var response = service.create(new NormaRequest("NR Teste", "Descricao", "Conteudo"));

        ArgumentCaptor<Norma> captor = ArgumentCaptor.forClass(Norma.class);
        verify(normaRepository).save(captor.capture());
        Norma saved = captor.getValue();

        assertThat(saved.getCriadoEm()).isNotNull();
        assertThat(saved.getAtualizadoEm()).isEqualTo(saved.getCriadoEm());
        assertThat(saved.getCriadoPorId()).isEqualTo(usuario.getId());
        assertThat(saved.getAtualizadoPorId()).isEqualTo(usuario.getId());
        assertThat(response.criadoPorNome()).isEqualTo("Admin");
        assertThat(response.atualizadoPorNome()).isEqualTo("Admin");
        assertThat(response.totalOcorrencias()).isEqualTo(3);
        assertThat(response.totalNcsAtivas()).isEqualTo(2);
    }

    @Test
    void update_preservaCriacaoEAtualizaAuditoriaDeEdicao() {
        UUID normaId = UUID.randomUUID();
        Usuario criador = usuario(UUID.randomUUID(), "Criador");
        Usuario editor = usuario(UUID.randomUUID(), "Editor");
        LocalDateTime criadoEm = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime atualizadoEmOriginal = LocalDateTime.of(2026, 5, 2, 10, 0);

        Norma norma = Norma.builder()
                .id(normaId)
                .titulo("Antigo")
                .descricao("Antiga")
                .conteudo("Antigo")
                .criadoEm(criadoEm)
                .criadoPorId(criador.getId())
                .atualizadoEm(atualizadoEmOriginal)
                .atualizadoPorId(criador.getId())
                .build();

        when(normaRepository.findById(normaId)).thenReturn(Optional.of(norma));
        when(securityHelper.getUsuarioLogado()).thenReturn(editor);
        when(normaRepository.save(any(Norma.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findById(criador.getId())).thenReturn(Optional.of(criador));
        when(usuarioRepository.findById(editor.getId())).thenReturn(Optional.of(editor));

        var response = service.update(normaId, new NormaRequest("Novo", "Nova", "Novo conteudo"));

        assertThat(norma.getTitulo()).isEqualTo("Novo");
        assertThat(norma.getCriadoEm()).isEqualTo(criadoEm);
        assertThat(norma.getCriadoPorId()).isEqualTo(criador.getId());
        assertThat(norma.getAtualizadoEm()).isAfter(atualizadoEmOriginal);
        assertThat(norma.getAtualizadoPorId()).isEqualTo(editor.getId());
        assertThat(response.criadoPorNome()).isEqualTo("Criador");
        assertThat(response.atualizadoPorNome()).isEqualTo("Editor");
    }

    private Usuario usuario(UUID id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        return usuario;
    }
}

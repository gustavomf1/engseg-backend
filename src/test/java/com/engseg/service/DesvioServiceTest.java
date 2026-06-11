package com.engseg.service;

import com.engseg.dto.response.DesvioResponse;
import com.engseg.entity.*;
import com.engseg.exception.BusinessException;
import com.engseg.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DesvioServiceTest {

    @Mock DesvioRepository desvioRepository;
    @Mock EstabelecimentoRepository estabelecimentoRepository;
    @Mock LocalizacaoRepository localizacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock EvidenciaRepository evidenciaRepository;
    @Mock HistoricoDesvioRepository historicoDesvioRepository;
    @Mock TrativaDesvioRepository trativaDesvioRepository;
    @Mock EmpresaRepository empresaRepository;
    @Mock S3StorageService s3StorageService;
    @Mock SecurityHelper securityHelper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    DesvioService service;

    private final UUID desvioId = UUID.randomUUID();

    @BeforeEach
    void setupSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken("test@engseg.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(usuarioRepository.findByEmail("test@engseg.com")).thenReturn(Optional.empty());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private Desvio buildDesvio(StatusDesvio status) {
        Estabelecimento est = new Estabelecimento();
        est.setId(UUID.randomUUID());
        est.setNome("Estabelecimento Teste");

        Desvio desvio = new Desvio();
        desvio.setId(desvioId);
        desvio.setEstabelecimento(est);
        desvio.setTitulo("Desvio Teste");
        desvio.setDescricao("Descrição");
        desvio.setOrientacaoRealizada("Orientação");
        desvio.setStatus(status);
        desvio.setDataRegistro(LocalDateTime.now());
        desvio.setRegraDeOuro(false);
        desvio.setHistorico(new ArrayList<>());
        desvio.setTratativas(new ArrayList<>());
        return desvio;
    }

    private void mockToResponseDeps(Desvio desvio) {
        when(trativaDesvioRepository.findByDesvioIdOrderByNumeroAsc(any())).thenReturn(List.of());
    }

    // ─── findAll (EXTERNO) ──────────────────────────────────────────────────────

    @Test
    void findAll_quandoExterno_retornaApenasDesviosOndeEhResponsavelTratativa() {
        UUID estId = UUID.randomUUID();
        Usuario externo = Usuario.builder().id(UUID.randomUUID()).perfil(PerfilUsuario.EXTERNO).build();
        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();

        Desvio desvioDoExterno = buildDesvio(StatusDesvio.AGUARDANDO_TRATATIVA);
        desvioDoExterno.setId(UUID.randomUUID());
        desvioDoExterno.getEstabelecimento().setId(estId);
        desvioDoExterno.setResponsavelTratativa(externo);

        Desvio desvioDeOutroResponsavel = buildDesvio(StatusDesvio.AGUARDANDO_TRATATIVA);
        desvioDeOutroResponsavel.setId(UUID.randomUUID());
        desvioDeOutroResponsavel.getEstabelecimento().setId(estId);
        desvioDeOutroResponsavel.setResponsavelTratativa(outroUsuario);

        Desvio desvioSemResponsavel = buildDesvio(StatusDesvio.ABERTO);
        desvioSemResponsavel.setId(UUID.randomUUID());
        desvioSemResponsavel.getEstabelecimento().setId(estId);

        when(securityHelper.isExterno()).thenReturn(true);
        when(securityHelper.getEstabelecimentosDoExterno()).thenReturn(List.of(estId));
        when(securityHelper.getUsuarioLogado()).thenReturn(externo);
        when(desvioRepository.findByEstabelecimentoIdIn(List.of(estId)))
                .thenReturn(List.of(desvioDoExterno, desvioDeOutroResponsavel, desvioSemResponsavel));
        mockToResponseDeps(desvioDoExterno);

        List<DesvioResponse> result = service.findAll(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(desvioDoExterno.getId());
    }

    @Test
    void findAll_quandoExternoSemEstabelecimentosPermitidos_retornaListaVazia() {
        when(securityHelper.isExterno()).thenReturn(true);
        when(securityHelper.getEstabelecimentosDoExterno()).thenReturn(List.of());

        List<DesvioResponse> result = service.findAll(null, null);

        assertThat(result).isEmpty();
        verify(desvioRepository, never()).findByEstabelecimentoIdIn(any());
    }

    // ─── findById (EXTERNO) ─────────────────────────────────────────────────────

    @Test
    void findById_quandoExternoNaoEhResponsavelTratativa_lancaBusinessException() {
        Usuario externo = Usuario.builder().id(UUID.randomUUID()).build();
        Usuario outroUsuario = Usuario.builder().id(UUID.randomUUID()).build();

        Desvio desvio = buildDesvio(StatusDesvio.AGUARDANDO_TRATATIVA);
        desvio.setResponsavelTratativa(outroUsuario);
        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));

        when(securityHelper.isExterno()).thenReturn(true);
        when(securityHelper.getUsuarioLogado()).thenReturn(externo);

        assertThatThrownBy(() -> service.findById(desvioId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Acesso negado");
    }

    @Test
    void findById_quandoExternoEhResponsavelTratativa_retornaDesvio() {
        Usuario externo = Usuario.builder().id(UUID.randomUUID()).perfil(PerfilUsuario.EXTERNO).build();

        Desvio desvio = buildDesvio(StatusDesvio.AGUARDANDO_TRATATIVA);
        desvio.setResponsavelTratativa(externo);
        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));
        mockToResponseDeps(desvio);

        when(securityHelper.isExterno()).thenReturn(true);
        when(securityHelper.getUsuarioLogado()).thenReturn(externo);

        DesvioResponse result = service.findById(desvioId);

        assertThat(result.id()).isEqualTo(desvioId);
    }
}

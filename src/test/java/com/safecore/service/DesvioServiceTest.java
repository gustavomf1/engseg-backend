package com.safecore.service;

import com.safecore.dto.request.AprovarDesvioRequest;
import com.safecore.dto.request.DesvioRequest;
import com.safecore.dto.request.ReprovarTrativasDesvioRequest;
import com.safecore.dto.response.DesvioResponse;
import com.safecore.entity.*;
import com.safecore.exception.BusinessException;
import com.safecore.exception.CamposObrigatoriosException;
import com.safecore.repository.*;
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
import org.mockito.ArgumentCaptor;

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

    @Test
    void aprovar_quandoUsuarioNaoEhResponsavelDesvio_lancaBusinessExceptionMesmoSendoAdmin() {
        Usuario admin = Usuario.builder().id(UUID.randomUUID()).admin(true).build();
        Usuario responsavelDesvio = Usuario.builder().id(UUID.randomUUID()).build();

        Desvio desvio = buildDesvio(StatusDesvio.AGUARDANDO_APROVACAO);
        desvio.setResponsavelDesvio(responsavelDesvio);
        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));
        when(securityHelper.getUsuarioLogado()).thenReturn(admin);

        assertThatThrownBy(() -> service.aprovar(desvioId, new AprovarDesvioRequest(null, List.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Apenas o responsável pelo desvio pode aprovar");
    }

    @Test
    void aprovar_quandoResponsavelDesvio_aprovaComSucesso() {
        Usuario responsavelDesvio = Usuario.builder().id(UUID.randomUUID()).build();

        Desvio desvio = buildDesvio(StatusDesvio.AGUARDANDO_APROVACAO);
        desvio.setResponsavelDesvio(responsavelDesvio);
        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));
        when(securityHelper.getUsuarioLogado()).thenReturn(responsavelDesvio);
        when(trativaDesvioRepository.findByDesvioIdAndStatus(any(), any())).thenReturn(List.of());
        when(desvioRepository.save(any())).thenReturn(desvio);
        mockToResponseDeps(desvio);

        DesvioResponse result = service.aprovar(desvioId, new AprovarDesvioRequest(null, List.of()));

        assertThat(result.id()).isEqualTo(desvioId);
        assertThat(desvio.getStatus()).isEqualTo(StatusDesvio.CONCLUIDO);
    }

    @Test
    void reprovar_quandoUsuarioNaoEhResponsavelDesvio_lancaBusinessExceptionMesmoSendoAdmin() {
        Usuario admin = Usuario.builder().id(UUID.randomUUID()).admin(true).build();
        Usuario responsavelDesvio = Usuario.builder().id(UUID.randomUUID()).build();

        Desvio desvio = buildDesvio(StatusDesvio.AGUARDANDO_APROVACAO);
        desvio.setResponsavelDesvio(responsavelDesvio);
        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));
        when(securityHelper.getUsuarioLogado()).thenReturn(admin);

        assertThatThrownBy(() -> service.reprovar(desvioId, new ReprovarTrativasDesvioRequest(List.of(), List.of())))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Apenas o responsável pelo desvio pode reprovar");
    }

    @Test
    void create_semDescricaoOrientacaoResponsaveis_criaComSucesso() {
        UUID estId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        Estabelecimento est = new Estabelecimento();
        est.setId(estId);
        Empresa empresaContratada = new Empresa();
        empresaContratada.setId(empresaId);

        DesvioRequest request = new DesvioRequest(
                estId, "Desvio sem detalhes", null, null, null, false,
                null, null, List.of(), List.of(), empresaId
        );

        when(estabelecimentoRepository.findById(estId)).thenReturn(Optional.of(est));
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresaContratada));

        Desvio saved = buildDesvio(StatusDesvio.ABERTO);
        saved.setDescricao(null);
        saved.setOrientacaoRealizada(null);
        saved.setResponsavelDesvio(null);
        saved.setResponsavelTratativa(null);
        mockToResponseDeps(saved);
        when(desvioRepository.save(any())).thenReturn(saved);
        when(desvioRepository.findById(any())).thenReturn(Optional.of(saved));

        Usuario usuarioLogado = Usuario.builder().id(UUID.randomUUID()).email("test@engseg.com").build();
        when(securityHelper.getUsuarioLogado()).thenReturn(usuarioLogado);

        DesvioResponse response = service.create(request);

        assertThat(response).isNotNull();

        ArgumentCaptor<Desvio> captor = ArgumentCaptor.forClass(Desvio.class);
        verify(desvioRepository).save(captor.capture());
        Desvio captured = captor.getValue();

        assertThat(captured.getDescricao()).isNull();
        assertThat(captured.getOrientacaoRealizada()).isNull();
        assertThat(captured.getResponsavelDesvio()).isNull();
        assertThat(captured.getResponsavelTratativa()).isNull();
    }

    @Test
    void abrirTratativa_quandoFaltamTodosOsCampos_lancaCamposObrigatoriosExceptionComTodosOsCodigos() {
        Usuario criador = Usuario.builder().id(UUID.randomUUID()).perfil(PerfilUsuario.TECNICO).build();
        Desvio desvio = buildDesvio(StatusDesvio.ABERTO);
        desvio.setDescricao(null);
        desvio.setOrientacaoRealizada(null);
        desvio.setUsuarioCriacao(criador);

        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));
        when(securityHelper.getUsuarioLogado()).thenReturn(criador);

        assertThatThrownBy(() -> service.abrirTratativa(desvioId))
                .isInstanceOf(CamposObrigatoriosException.class)
                .satisfies(ex -> assertThat(((CamposObrigatoriosException) ex).getCamposFaltantes())
                        .containsExactlyInAnyOrder("DESCRICAO", "ORIENTACAO_REALIZADA", "RESPONSAVEL_DESVIO", "RESPONSAVEL_TRATATIVA"));
    }

    @Test
    void abrirTratativa_quandoTodosOsCamposPreenchidos_transicionaParaAguardandoTratativa() {
        Usuario criador = Usuario.builder().id(UUID.randomUUID()).perfil(PerfilUsuario.TECNICO).build();
        Usuario responsavel = Usuario.builder().id(UUID.randomUUID()).perfil(PerfilUsuario.ENGENHEIRO).build();

        Desvio desvio = buildDesvio(StatusDesvio.ABERTO);
        desvio.setUsuarioCriacao(criador);
        desvio.setResponsavelDesvio(responsavel);
        desvio.setResponsavelTratativa(responsavel);

        when(desvioRepository.findById(desvioId)).thenReturn(Optional.of(desvio));
        when(securityHelper.getUsuarioLogado()).thenReturn(criador);
        when(desvioRepository.save(any())).thenReturn(desvio);
        mockToResponseDeps(desvio);

        DesvioResponse response = service.abrirTratativa(desvioId);

        assertThat(response).isNotNull();
        assertThat(desvio.getStatus()).isEqualTo(StatusDesvio.AGUARDANDO_TRATATIVA);
    }
}

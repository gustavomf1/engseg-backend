package com.engseg.service;

import com.engseg.dto.request.AprovarRejeitarRequest;
import com.engseg.dto.request.InvestigacaoRequest;
import com.engseg.dto.request.SubmeterEvidenciasRequest;
import com.engseg.dto.response.NaoConformidadeResponse;
import com.engseg.entity.*;
import com.engseg.exception.BusinessException;
import com.engseg.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
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
class NaoConformidadeServiceTest {

    @Mock NaoConformidadeRepository naoConformidadeRepository;
    @Mock EstabelecimentoRepository estabelecimentoRepository;
    @Mock LocalizacaoRepository localizacaoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock EvidenciaRepository evidenciaRepository;
    @Mock S3StorageService s3StorageService;
    @Mock NormaRepository normaRepository;
    @Mock HistoricoNcRepository historicoNcRepository;
    @Mock InvestigacaoSnapshotRepository investigacaoSnapshotRepository;
    @Mock ExecucaoSnapshotRepository execucaoSnapshotRepository;
    @Mock SecurityHelper securityHelper;

    @InjectMocks
    NaoConformidadeService service;

    private final UUID ncId = UUID.randomUUID();

    @BeforeEach
    void setupSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken("test@engseg.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(usuarioRepository.findByEmail("test@engseg.com")).thenReturn(Optional.empty());
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private NaoConformidade buildNc(StatusNaoConformidade status) {
        Estabelecimento est = new Estabelecimento();
        est.setId(UUID.randomUUID());
        est.setNome("Estabelecimento Teste");

        NaoConformidade nc = new NaoConformidade();
        nc.setId(ncId);
        nc.setEstabelecimento(est);
        nc.setTitulo("NC Teste");
        nc.setDescricao("Descrição");
        nc.setStatus(status);
        nc.setDataRegistro(LocalDateTime.now());
        nc.setDataLimiteResolucao(LocalDate.now().plusDays(30));
        nc.setRegraDeOuro(false);
        nc.setVencida("N");
        nc.setReincidencia("N");
        nc.setAtividades(new ArrayList<>());
        nc.setHistorico(new ArrayList<>());
        nc.setNormas(new ArrayList<>());
        return nc;
    }

    private void mockToResponseDeps(NaoConformidade nc) {
        when(naoConformidadeRepository.save(any())).thenReturn(nc);
        when(naoConformidadeRepository.findByNcAnteriorId(any())).thenReturn(List.of());
        when(investigacaoSnapshotRepository.findByNaoConformidadeIdOrderByDataSubmissaoAsc(any())).thenReturn(List.of());
        when(execucaoSnapshotRepository.findByNaoConformidadeIdOrderByDataSubmissaoAsc(any())).thenReturn(List.of());
    }

    private InvestigacaoRequest buildInvestigacaoRequest() {
        return new InvestigacaoRequest(
                List.of(
                        new InvestigacaoRequest.PorqueItem("Por que 1?", "Resposta 1"),
                        new InvestigacaoRequest.PorqueItem("Por que 2?", "Resposta 2"),
                        new InvestigacaoRequest.PorqueItem("Por que 3?", "Resposta 3")
                ),
                "Causa raiz identificada",
                List.of("Atividade A", "Atividade B")
        );
    }

    // ─── submeterInvestigacao ──────────────────────────────────────────────────

    @Test
    void submeterInvestigacao_quandoAberta_transicionaParaAguardandoAprovacaoPlano() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.ABERTA);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc)); // segunda chamada

        service.submeterInvestigacao(ncId, buildInvestigacaoRequest());

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
    }

    @Test
    void submeterInvestigacao_quandoEmAjustePeloExterno_transicionaParaAguardandoAprovacaoPlano() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);

        service.submeterInvestigacao(ncId, buildInvestigacaoRequest());

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
    }

    @Test
    void submeterInvestigacao_quandoStatusInvalido_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.EM_EXECUCAO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.submeterInvestigacao(ncId, buildInvestigacaoRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ABERTA ou EM_AJUSTE_PELO_EXTERNO");

        verify(naoConformidadeRepository, never()).save(any());
    }

    @Test
    void submeterInvestigacao_substituiAtividadesAnteriores() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.ABERTA);
        nc.getAtividades().add(new AtividadePlanoAcao()); // atividade prévia
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);

        InvestigacaoRequest req = buildInvestigacaoRequest();
        service.submeterInvestigacao(ncId, req);

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getAtividades()).hasSize(req.atividades().size());
    }

    // ─── aprovarPlano ──────────────────────────────────────────────────────────

    @Test
    void aprovarPlano_quandoAguardandoAprovacaoPlano_transicionaParaEmExecucao() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);
        when(investigacaoSnapshotRepository.findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(any(), any()))
                .thenReturn(Optional.empty());

        service.aprovarPlano(ncId, new AprovarRejeitarRequest("ok"));

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.EM_EXECUCAO);
    }

    @Test
    void aprovarPlano_quandoStatusInvalido_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.ABERTA);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.aprovarPlano(ncId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGUARDANDO_APROVACAO_PLANO");

        verify(naoConformidadeRepository, never()).save(any());
    }

    // ─── rejeitarPlano ────────────────────────────────────────────────────────

    @Test
    void rejeitarPlano_quandoAguardandoAprovacaoPlano_transicionaParaEmAjustePeloExterno() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);
        when(investigacaoSnapshotRepository.findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(any(), any()))
                .thenReturn(Optional.empty());

        service.rejeitarPlano(ncId, new AprovarRejeitarRequest("Motivo da rejeição"));

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO);
    }

    @Test
    void rejeitarPlano_semMotivo_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.rejeitarPlano(ncId, new AprovarRejeitarRequest("")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatório");

        assertThatThrownBy(() -> service.rejeitarPlano(ncId, null))
                .isInstanceOf(BusinessException.class);

        verify(naoConformidadeRepository, never()).save(any());
    }

    @Test
    void rejeitarPlano_quandoStatusInvalido_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.EM_EXECUCAO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.rejeitarPlano(ncId, new AprovarRejeitarRequest("motivo")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGUARDANDO_APROVACAO_PLANO");
    }

    // ─── submeterEvidencias ───────────────────────────────────────────────────

    @Test
    void submeterEvidencias_quandoEmExecucao_transicionaParaAguardandoValidacaoFinal() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.EM_EXECUCAO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);

        service.submeterEvidencias(ncId, new SubmeterEvidenciasRequest("Executamos as correções necessárias"));

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);
    }

    @Test
    void submeterEvidencias_quandoStatusInvalido_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.ABERTA);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.submeterEvidencias(ncId, new SubmeterEvidenciasRequest("desc")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EM_EXECUCAO");

        verify(naoConformidadeRepository, never()).save(any());
    }

    // ─── aprovarEvidencias ────────────────────────────────────────────────────

    @Test
    void aprovarEvidencias_quandoAguardandoValidacaoFinal_transicionaParaConcluido() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);
        when(execucaoSnapshotRepository.findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(any(), any()))
                .thenReturn(Optional.empty());

        service.aprovarEvidencias(ncId, new AprovarRejeitarRequest("Aprovado"));

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.CONCLUIDO);
    }

    @Test
    void aprovarEvidencias_quandoStatusInvalido_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.EM_EXECUCAO);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.aprovarEvidencias(ncId, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGUARDANDO_VALIDACAO_FINAL");
    }

    // ─── rejeitarEvidencias ───────────────────────────────────────────────────

    @Test
    void rejeitarEvidencias_quandoAguardandoValidacaoFinal_transicionaParaEmExecucao() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));
        mockToResponseDeps(nc);
        when(execucaoSnapshotRepository.findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(any(), any()))
                .thenReturn(Optional.empty());

        service.rejeitarEvidencias(ncId, new AprovarRejeitarRequest("Evidências insuficientes"));

        ArgumentCaptor<NaoConformidade> captor = ArgumentCaptor.forClass(NaoConformidade.class);
        verify(naoConformidadeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusNaoConformidade.EM_EXECUCAO);
    }

    @Test
    void rejeitarEvidencias_semMotivo_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.rejeitarEvidencias(ncId, new AprovarRejeitarRequest("  ")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("obrigatório");

        verify(naoConformidadeRepository, never()).save(any());
    }

    @Test
    void rejeitarEvidencias_quandoStatusInvalido_lancaBusinessException() {
        NaoConformidade nc = buildNc(StatusNaoConformidade.ABERTA);
        when(naoConformidadeRepository.findById(ncId)).thenReturn(Optional.of(nc));

        assertThatThrownBy(() -> service.rejeitarEvidencias(ncId, new AprovarRejeitarRequest("motivo")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AGUARDANDO_VALIDACAO_FINAL");
    }
}

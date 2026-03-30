package com.engseg.service;

import com.engseg.dto.request.AprovarRejeitarRequest;
import com.engseg.dto.request.InvestigacaoRequest;
import com.engseg.dto.request.NaoConformidadeRequest;
import com.engseg.dto.request.SubmeterEvidenciasRequest;
import com.engseg.dto.response.*;
import com.engseg.entity.*;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaoConformidadeService {

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final S3StorageService s3StorageService;
    private final NormaRepository normaRepository;
    private final HistoricoNcRepository historicoNcRepository;
    private final InvestigacaoSnapshotRepository investigacaoSnapshotRepository;
    private final ExecucaoSnapshotRepository execucaoSnapshotRepository;
    private final SecurityHelper securityHelper;

    public List<NaoConformidadeResponse> findAll(StatusNaoConformidade status, UUID estabelecimentoId) {
        // EXTERNO: restrito aos estabelecimentos vinculados à sua empresa
        if (securityHelper.isExterno()) {
            List<UUID> permitidos = securityHelper.getEstabelecimentosDoExterno();
            if (permitidos.isEmpty()) return List.of();
            if (estabelecimentoId != null) {
                if (!permitidos.contains(estabelecimentoId)) return List.of();
                return (status != null
                        ? naoConformidadeRepository.findByStatusAndEstabelecimentoId(status, estabelecimentoId)
                        : naoConformidadeRepository.findByEstabelecimentoId(estabelecimentoId))
                        .stream().map(this::toResponse).toList();
            }
            return (status != null
                    ? naoConformidadeRepository.findByStatusAndEstabelecimentoIdIn(status, permitidos)
                    : naoConformidadeRepository.findByEstabelecimentoIdIn(permitidos))
                    .stream().map(this::toResponse).toList();
        }

        // ENGENHEIRO / TECNICO: sem restrição de empresa, filtra por estabelecimento se informado
        List<NaoConformidade> list;
        if (status != null && estabelecimentoId != null) {
            list = naoConformidadeRepository.findByStatusAndEstabelecimentoId(status, estabelecimentoId);
        } else if (status != null) {
            list = naoConformidadeRepository.findByStatus(status);
        } else if (estabelecimentoId != null) {
            list = naoConformidadeRepository.findByEstabelecimentoId(estabelecimentoId);
        } else {
            list = naoConformidadeRepository.findAll();
        }
        return list.stream().map(this::toResponse).toList();
    }

    public NaoConformidadeResponse findById(UUID id) {
        return naoConformidadeRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));
    }

    @Transactional
    public NaoConformidadeResponse create(NaoConformidadeRequest request) {
        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        var engConstrutora = request.engResponsavelConstrutoraId() != null
                ? usuarioRepository.findById(request.engResponsavelConstrutoraId())
                        .orElseThrow(() -> new ResourceNotFoundException("Engenheiro (construtora) não encontrado: " + request.engResponsavelConstrutoraId()))
                : null;

        var engVerificacao = request.engResponsavelVerificacaoId() != null
                ? usuarioRepository.findById(request.engResponsavelVerificacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Engenheiro (verificação) não encontrado: " + request.engResponsavelVerificacaoId()))
                : null;

        var localizacao = request.localizacaoId() != null
                ? localizacaoRepository.findById(request.localizacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + request.localizacaoId()))
                : null;

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var tecnico = usuarioRepository.findByEmail(email).orElse(null);

        LocalDateTime now = LocalDateTime.now();

        NaoConformidade nc = new NaoConformidade();
        nc.setEstabelecimento(estabelecimento);
        nc.setTitulo(request.titulo());
        nc.setLocalizacao(localizacao);
        nc.setDescricao(request.descricao());
        nc.setDataRegistro(now);
        nc.setTecnico(tecnico);
        nc.setUsuarioCriacao(tecnico);
        nc.setRegraDeOuro(request.regraDeOuro());
        nc.setNivelSeveridade(request.nivelSeveridade());
        if (engConstrutora != null) nc.setEngResponsavelConstrutora(engConstrutora);
        if (engVerificacao != null) nc.setEngResponsavelVerificacao(engVerificacao);
        nc.setDataLimiteResolucao(now.toLocalDate().plusDays(30));
        nc.setStatus(StatusNaoConformidade.ABERTA);
        nc.setAtividades(new ArrayList<>());
        nc.setHistorico(new ArrayList<>());

        if (request.normaIds() != null && !request.normaIds().isEmpty()) {
            nc.setNormas(normaRepository.findAllById(request.normaIds()));
        }

        nc.setReincidencia(request.reincidencia() ? "S" : "N");
        if (request.reincidencia()) {
            if (request.ncAnteriorId() == null) {
                throw new BusinessException("NC anterior é obrigatória quando reincidência está marcada");
            }
            var ncAnterior = naoConformidadeRepository.findById(request.ncAnteriorId())
                    .orElseThrow(() -> new ResourceNotFoundException("NC anterior não encontrada: " + request.ncAnteriorId()));
            validarFimDaCadeia(ncAnterior, null);
            nc.setNcAnterior(ncAnterior);
        }

        NaoConformidade saved = naoConformidadeRepository.save(nc);
        registrarHistorico(saved, tecnico, TipoAcaoHistorico.CRIACAO, null, null, StatusNaoConformidade.ABERTA);

        return toResponse(naoConformidadeRepository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public NaoConformidadeResponse update(UUID id, NaoConformidadeRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.ABERTA) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            var usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null && usuario.getPerfil() == PerfilUsuario.TECNICO) {
                throw new BusinessException("Técnico não pode editar uma NC que não está com status ABERTA");
            }
        }

        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        var engConstrutora = request.engResponsavelConstrutoraId() != null
                ? usuarioRepository.findById(request.engResponsavelConstrutoraId())
                        .orElseThrow(() -> new ResourceNotFoundException("Engenheiro (construtora) não encontrado: " + request.engResponsavelConstrutoraId()))
                : null;

        var engVerificacao = request.engResponsavelVerificacaoId() != null
                ? usuarioRepository.findById(request.engResponsavelVerificacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Engenheiro (verificação) não encontrado: " + request.engResponsavelVerificacaoId()))
                : null;

        var localizacao = request.localizacaoId() != null
                ? localizacaoRepository.findById(request.localizacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + request.localizacaoId()))
                : null;

        nc.setEstabelecimento(estabelecimento);
        nc.setTitulo(request.titulo());
        nc.setLocalizacao(localizacao);
        nc.setDescricao(request.descricao());
        nc.setRegraDeOuro(request.regraDeOuro());
        nc.setNivelSeveridade(request.nivelSeveridade());
        nc.setEngResponsavelConstrutora(engConstrutora);
        nc.setEngResponsavelVerificacao(engVerificacao);

        if (request.normaIds() != null) {
            nc.setNormas(request.normaIds().isEmpty() ? new ArrayList<>() : normaRepository.findAllById(request.normaIds()));
        }

        nc.setReincidencia(request.reincidencia() ? "S" : "N");
        if (request.reincidencia()) {
            if (request.ncAnteriorId() == null) {
                throw new BusinessException("NC anterior é obrigatória quando reincidência está marcada");
            }
            if (request.ncAnteriorId().equals(id)) {
                throw new BusinessException("Uma NC não pode ser reincidência de si mesma");
            }
            var ncAnterior = naoConformidadeRepository.findById(request.ncAnteriorId())
                    .orElseThrow(() -> new ResourceNotFoundException("NC anterior não encontrada: " + request.ncAnteriorId()));
            validarFimDaCadeia(ncAnterior, id);
            nc.setNcAnterior(ncAnterior);
        } else {
            nc.setNcAnterior(null);
        }

        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public void delete(UUID id) {
        NaoConformidade nc = findNcOrThrow(id);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario != null && usuario.getPerfil() == PerfilUsuario.TECNICO && nc.getStatus() != StatusNaoConformidade.ABERTA) {
            throw new BusinessException("Técnico só pode excluir NC com status ABERTA");
        }

        List<Evidencia> evidenciasNc = evidenciaRepository.findByNaoConformidadeId(id);
        for (Evidencia ev : evidenciasNc) {
            s3StorageService.delete(ev.getUrlArquivo());
        }
        evidenciaRepository.deleteAll(evidenciasNc);

        if (nc.getExecucoes() != null) {
            for (ExecucaoAcao execucao : nc.getExecucoes()) {
                List<Evidencia> evidenciasExec = evidenciaRepository.findByExecucaoAcaoId(execucao.getId());
                for (Evidencia ev : evidenciasExec) {
                    s3StorageService.delete(ev.getUrlArquivo());
                }
                evidenciaRepository.deleteAll(evidenciasExec);
            }
        }

        naoConformidadeRepository.delete(nc);
    }

    // -------------------------------------------------------------------------
    // Novo fluxo: Investigação → Plano → Execução → Validação Final
    // -------------------------------------------------------------------------

    @Transactional
    public NaoConformidadeResponse submeterInvestigacao(UUID id, InvestigacaoRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.ABERTA && nc.getStatus() != StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO) {
            throw new BusinessException("Investigação só pode ser submetida quando NC está ABERTA ou EM_AJUSTE_PELO_EXTERNO");
        }

        nc.setPorqueUm(request.porqueUm());
        nc.setPorqueUmResposta(request.porqueUmResposta());
        nc.setPorqueDois(request.porqueDois());
        nc.setPorqueDoisResposta(request.porqueDoisResposta());
        nc.setPorqueTres(request.porqueTres());
        nc.setPorqueTresResposta(request.porqueTresResposta());
        nc.setPorqueQuatro(request.porqueQuatro());
        nc.setPorqueQuatroResposta(request.porqueQuatroResposta());
        nc.setPorqueCinco(request.porqueCinco());
        nc.setPorqueCincoResposta(request.porqueCincoResposta());
        nc.setCausaRaiz(request.causaRaiz());

        // Substitui atividades existentes pelas novas
        nc.getAtividades().clear();
        for (int i = 0; i < request.atividades().size(); i++) {
            AtividadePlanoAcao atividade = new AtividadePlanoAcao();
            atividade.setNaoConformidade(nc);
            atividade.setDescricao(request.atividades().get(i));
            atividade.setOrdem(i + 1);
            nc.getAtividades().add(atividade);
        }

        StatusNaoConformidade statusAnterior = nc.getStatus();
        nc.setStatus(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
        naoConformidadeRepository.save(nc);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);
        registrarHistorico(nc, usuario, TipoAcaoHistorico.SUBMISSAO_INVESTIGACAO, null, statusAnterior, StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);

        // Snapshot da investigação submetida
        InvestigacaoSnapshot snapshot = new InvestigacaoSnapshot();
        snapshot.setNaoConformidade(nc);
        snapshot.setPorqueUm(request.porqueUm());
        snapshot.setPorqueUmResposta(request.porqueUmResposta());
        snapshot.setPorqueDois(request.porqueDois());
        snapshot.setPorqueDoisResposta(request.porqueDoisResposta());
        snapshot.setPorqueTres(request.porqueTres());
        snapshot.setPorqueTresResposta(request.porqueTresResposta());
        snapshot.setPorqueQuatro(request.porqueQuatro());
        snapshot.setPorqueQuatroResposta(request.porqueQuatroResposta());
        snapshot.setPorqueCinco(request.porqueCinco());
        snapshot.setPorqueCincoResposta(request.porqueCincoResposta());
        snapshot.setCausaRaiz(request.causaRaiz());
        snapshot.setAtividades(new ArrayList<>(request.atividades()));
        snapshot.setDataSubmissao(LocalDateTime.now());
        snapshot.setStatus("PENDENTE");
        investigacaoSnapshotRepository.save(snapshot);

        return toResponse(naoConformidadeRepository.findById(id).orElseThrow());
    }

    @Transactional
    public NaoConformidadeResponse aprovarPlano(UUID id, AprovarRejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO) {
            throw new BusinessException("Plano só pode ser aprovado quando NC está AGUARDANDO_APROVACAO_PLANO");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.APROVACAO_PLANO,
                request != null ? request.comentario() : null,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_EXECUCAO);

        investigacaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> { s.setStatus("APROVADO"); s.setComentarioRevisao(request != null ? request.comentario() : null); investigacaoSnapshotRepository.save(s); });

        nc.setStatus(StatusNaoConformidade.EM_EXECUCAO);
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse rejeitarPlano(UUID id, AprovarRejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO) {
            throw new BusinessException("Plano só pode ser rejeitado quando NC está AGUARDANDO_APROVACAO_PLANO");
        }

        if (request == null || request.comentario() == null || request.comentario().isBlank()) {
            throw new BusinessException("Motivo da rejeição é obrigatório");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.REJEICAO_PLANO, request.comentario(),
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO);

        investigacaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> { s.setStatus("REPROVADO"); s.setComentarioRevisao(request.comentario()); investigacaoSnapshotRepository.save(s); });

        nc.setStatus(StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO);
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse submeterEvidencias(UUID id, SubmeterEvidenciasRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.EM_EXECUCAO) {
            throw new BusinessException("Evidências só podem ser submetidas para validação quando NC está EM_EXECUCAO");
        }

        nc.setDescricaoExecucao(request.descricaoExecucao());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.SUBMISSAO_EVIDENCIAS, null,
                StatusNaoConformidade.EM_EXECUCAO, StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);

        ExecucaoSnapshot execSnapshot = new ExecucaoSnapshot();
        execSnapshot.setNaoConformidade(nc);
        execSnapshot.setDescricaoExecucao(request.descricaoExecucao());
        execSnapshot.setDataSubmissao(LocalDateTime.now());
        execSnapshot.setStatus("PENDENTE");

        // Carry-forward: inclui evidências da última submissão reprovada
        execucaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "REPROVADO")
                .ifPresent(anterior -> execSnapshot.getEvidencias().addAll(anterior.getEvidencias()));

        // Evidências novas (ainda sem snapshot vinculado)
        List<Evidencia> novas = evidenciaRepository
                .findByNaoConformidadeIdAndTipoEvidenciaAndExecucaoSnapshotIsNull(id, TipoEvidencia.TRATATIVA);
        novas.forEach(e -> e.setExecucaoSnapshot(execSnapshot));
        execSnapshot.getEvidencias().addAll(novas);

        execucaoSnapshotRepository.save(execSnapshot);
        evidenciaRepository.saveAll(novas);

        nc.setStatus(StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse aprovarEvidencias(UUID id, AprovarRejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL) {
            throw new BusinessException("Evidências só podem ser aprovadas quando NC está AGUARDANDO_VALIDACAO_FINAL");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.APROVACAO_EVIDENCIAS,
                request != null ? request.comentario() : null,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.CONCLUIDO);

        execucaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> { s.setStatus("APROVADO"); s.setComentarioRevisao(request != null ? request.comentario() : null); execucaoSnapshotRepository.save(s); });

        nc.setStatus(StatusNaoConformidade.CONCLUIDO);
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse rejeitarEvidencias(UUID id, AprovarRejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL) {
            throw new BusinessException("Evidências só podem ser rejeitadas quando NC está AGUARDANDO_VALIDACAO_FINAL");
        }

        if (request == null || request.comentario() == null || request.comentario().isBlank()) {
            throw new BusinessException("Motivo da rejeição das evidências é obrigatório");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.REJEICAO_EVIDENCIAS, request.comentario(),
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.EM_EXECUCAO);

        execucaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> { s.setStatus("REPROVADO"); s.setComentarioRevisao(request.comentario()); execucaoSnapshotRepository.save(s); });

        nc.setStatus(StatusNaoConformidade.EM_EXECUCAO);
        return toResponse(naoConformidadeRepository.save(nc));
    }

    public List<HistoricoNcResponse> findHistorico(UUID id) {
        if (!naoConformidadeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Não conformidade não encontrada: " + id);
        }
        return historicoNcRepository.findByNaoConformidadeIdOrderByDataAcaoAsc(id)
                .stream()
                .map(h -> new HistoricoNcResponse(
                        h.getId(),
                        h.getAcao(),
                        h.getUsuario() != null ? h.getUsuario().getNome() : null,
                        h.getComentario(),
                        h.getStatusAnterior(),
                        h.getStatusAtual(),
                        h.getDataAcao()
                ))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Scheduled
    // -------------------------------------------------------------------------

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void atualizarVencidas() {
        List<NaoConformidade> vencidas = naoConformidadeRepository.findVencidas(LocalDate.now());
        log.info("Marcando {} NCs como vencidas", vencidas.size());
        for (NaoConformidade nc : vencidas) {
            nc.setVencida("S");
        }
        naoConformidadeRepository.saveAll(vencidas);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Valida que a NC selecionada como anterior é de fato o fim da cadeia.
     * Se já existe outra NC apontando para ela, encontra o fim real e informa o usuário.
     * O parâmetro excludeId é usado no update para ignorar a própria NC sendo editada.
     */
    private void validarFimDaCadeia(NaoConformidade ncAnterior, UUID excludeId) {
        List<NaoConformidade> sucessoras = naoConformidadeRepository.findByNcAnteriorId(ncAnterior.getId())
                .stream()
                .filter(s -> excludeId == null || !s.getId().equals(excludeId))
                .toList();

        if (sucessoras.isEmpty()) return;

        // Percorre a cadeia até o fim real
        NaoConformidade fim = sucessoras.get(0);
        while (true) {
            List<NaoConformidade> prox = naoConformidadeRepository.findByNcAnteriorId(fim.getId())
                    .stream()
                    .filter(s -> excludeId == null || !s.getId().equals(excludeId))
                    .toList();
            if (prox.isEmpty()) break;
            fim = prox.get(0);
        }

        throw new BusinessException(
                "A NC selecionada já possui uma reincidência registrada. " +
                "Para manter o rastro linear, selecione a última NC da cadeia: \"" + fim.getTitulo() + "\" (ID: " + fim.getId() + ")"
        );
    }

    private NaoConformidade findNcOrThrow(UUID id) {
        return naoConformidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));
    }

    private void registrarHistorico(NaoConformidade nc, Usuario usuario, TipoAcaoHistorico acao,
                                     String comentario, StatusNaoConformidade statusAnterior, StatusNaoConformidade statusAtual) {
        HistoricoNc historico = new HistoricoNc();
        historico.setNaoConformidade(nc);
        historico.setUsuario(usuario);
        historico.setAcao(acao);
        historico.setComentario(comentario);
        historico.setStatusAnterior(statusAnterior);
        historico.setStatusAtual(statusAtual);
        historico.setDataAcao(LocalDateTime.now());
        historicoNcRepository.save(historico);
    }

    private NaoConformidadeResponse toResponse(NaoConformidade nc) {
        List<NormaResponse> normas = nc.getNormas() == null ? List.of() :
                nc.getNormas().stream().map(n -> new NormaResponse(
                        n.getId(), n.getTitulo(), n.getDescricao(), n.getConteudo(), n.isAtivo()
                )).toList();

        List<AtividadeResponse> atividades = nc.getAtividades() == null ? List.of() :
                nc.getAtividades().stream().map(a -> new AtividadeResponse(
                        a.getId(), a.getDescricao(), a.getOrdem()
                )).toList();

        List<HistoricoNcResponse> historico = nc.getHistorico() == null ? List.of() :
                nc.getHistorico().stream()
                        .sorted((a, b) -> a.getDataAcao().compareTo(b.getDataAcao()))
                        .map(h -> new HistoricoNcResponse(
                                h.getId(),
                                h.getAcao(),
                                h.getUsuario() != null ? h.getUsuario().getNome() : null,
                                h.getComentario(),
                                h.getStatusAnterior(),
                                h.getStatusAtual(),
                                h.getDataAcao()
                        )).toList();

        List<InvestigacaoSnapshotResponse> investigacaoSnapshots =
                investigacaoSnapshotRepository.findByNaoConformidadeIdOrderByDataSubmissaoAsc(nc.getId())
                        .stream().map(s -> new InvestigacaoSnapshotResponse(
                                s.getId(), s.getPorqueUm(), s.getPorqueUmResposta(),
                                s.getPorqueDois(), s.getPorqueDoisResposta(),
                                s.getPorqueTres(), s.getPorqueTresResposta(),
                                s.getPorqueQuatro(), s.getPorqueQuatroResposta(),
                                s.getPorqueCinco(), s.getPorqueCincoResposta(),
                                s.getCausaRaiz(), s.getAtividades(),
                                s.getDataSubmissao(), s.getStatus(), s.getComentarioRevisao()
                        )).toList();

        List<ExecucaoSnapshotResponse> execucaoSnapshots =
                execucaoSnapshotRepository.findByNaoConformidadeIdOrderByDataSubmissaoAsc(nc.getId())
                        .stream().map(s -> new ExecucaoSnapshotResponse(
                                s.getId(), s.getDescricaoExecucao(),
                                s.getDataSubmissao(), s.getStatus(), s.getComentarioRevisao(),
                                s.getEvidencias().stream()
                                        .map(e -> new EvidenciaResponse(e.getId(), e.getNomeArquivo(), e.getUrlArquivo(), e.getDataUpload()))
                                        .toList()
                        )).toList();

        return new NaoConformidadeResponse(
                nc.getId(),
                nc.getEstabelecimento().getId(),
                nc.getEstabelecimento().getNome(),
                nc.getTitulo(),
                nc.getLocalizacao() != null ? nc.getLocalizacao().getId() : null,
                nc.getLocalizacao() != null ? nc.getLocalizacao().getNome() : null,
                nc.getDescricao(),
                nc.getDataRegistro(),
                nc.getTecnico() != null ? nc.getTecnico().getNome() : null,
                nc.isRegraDeOuro(),
                nc.getNivelSeveridade(),
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getId() : null,
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getNome() : null,
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getEmail() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getId() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getNome() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getEmail() : null,
                nc.getDataLimiteResolucao(),
                nc.getUsuarioCriacao() != null ? nc.getUsuarioCriacao().getNome() : null,
                nc.getUsuarioCriacao() != null ? nc.getUsuarioCriacao().getEmail() : null,
                nc.getStatus(),
                "S".equals(nc.getVencida()),
                "S".equals(nc.getReincidencia()),
                nc.getNcAnterior() != null ? nc.getNcAnterior().getId() : null,
                nc.getNcAnterior() != null ? nc.getNcAnterior().getTitulo() : null,
                buildCadeiaReincidencias(nc),
                naoConformidadeRepository.findByNcAnteriorId(nc.getId()).stream()
                        .map(r -> new NcResumoResponse(r.getId(), r.getTitulo(), r.getDataRegistro(), r.getStatus()))
                        .toList(),
                nc.getPorqueUm(),
                nc.getPorqueUmResposta(),
                nc.getPorqueDois(),
                nc.getPorqueDoisResposta(),
                nc.getPorqueTres(),
                nc.getPorqueTresResposta(),
                nc.getPorqueQuatro(),
                nc.getPorqueQuatroResposta(),
                nc.getPorqueCinco(),
                nc.getPorqueCincoResposta(),
                nc.getCausaRaiz(),
                nc.getDescricaoExecucao(),
                atividades,
                historico,
                investigacaoSnapshots,
                execucaoSnapshots,
                List.of(),
                List.of(),
                List.of(),
                normas
        );
    }

    private List<NcResumoResponse> buildCadeiaReincidencias(NaoConformidade nc) {
        List<NcResumoResponse> cadeia = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        visited.add(nc.getId());
        NaoConformidade atual = nc.getNcAnterior();
        while (atual != null && !visited.contains(atual.getId())) {
            visited.add(atual.getId());
            cadeia.add(0, new NcResumoResponse(atual.getId(), atual.getTitulo(), atual.getDataRegistro(), atual.getStatus()));
            atual = atual.getNcAnterior();
        }
        return cadeia;
    }
}

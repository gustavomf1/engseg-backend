package com.engseg.service;

import com.engseg.dto.request.AprovarRejeitarRequest;
import com.engseg.dto.request.InvestigacaoRequest;
import com.engseg.dto.request.RejeitarRequest;
import com.engseg.dto.request.NaoConformidadeRequest;
import com.engseg.dto.request.RevisarAtividadesRequest;
import com.engseg.dto.request.RevisarExecucaoRequest;
import com.engseg.dto.request.SubmeterEvidenciasRequest;
import com.engseg.dto.request.SubmeterExecucaoRequest;
import com.engseg.dto.response.*;
import com.engseg.entity.*;
import com.engseg.event.NcEmailEvent;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.*;
import com.engseg.util.MatrizRisco;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final AtividadePlanoAcaoRepository atividadePlanoAcaoRepository;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher eventPublisher;

    public List<NaoConformidadeResponse> findAll(StatusNaoConformidade status, UUID estabelecimentoId, UUID empresaId) {
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

        // ENGENHEIRO / TECNICO / ADMIN: filtra por empresa e/ou estabelecimento se informado
        List<NaoConformidade> list;
        if (estabelecimentoId != null) {
            list = status != null
                    ? naoConformidadeRepository.findByStatusAndEstabelecimentoId(status, estabelecimentoId)
                    : naoConformidadeRepository.findByEstabelecimentoId(estabelecimentoId);
        } else if (empresaId != null) {
            list = status != null
                    ? naoConformidadeRepository.findByStatusAndEstabelecimento_EmpresaId(status, empresaId)
                    : naoConformidadeRepository.findByEstabelecimento_EmpresaId(empresaId);
        } else if (status != null) {
            list = naoConformidadeRepository.findByStatus(status);
        } else {
            list = naoConformidadeRepository.findAll();
        }

        if (securityHelper.isTecnico()) {
            var usuarioLogado = securityHelper.getUsuarioLogado();
            UUID uid = usuarioLogado.getId();
            list = list.stream()
                    .filter(nc -> nc.getUsuarioCriacao() != null && nc.getUsuarioCriacao().getId().equals(uid))
                    .toList();
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
        nc.setSeveridade(request.severidade());
        nc.setProbabilidade(request.probabilidade());
        nc.setNivelRisco(MatrizRisco.calcular(request.severidade(), request.probabilidade()));
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

        eventPublisher.publishEvent(new NcEmailEvent(this, saved.getId(),
                null, StatusNaoConformidade.ABERTA,
                request.emailsManuais(), request.emailsPadraoExcluidos(), null));

        return toResponse(naoConformidadeRepository.findById(saved.getId()).orElseThrow());
    }

    @Transactional
    public NaoConformidadeResponse update(UUID id, NaoConformidadeRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() == StatusNaoConformidade.CONCLUIDO) {
            throw new BusinessException("Não é permitido editar uma NC concluída");
        }

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
        nc.setSeveridade(request.severidade());
        nc.setProbabilidade(request.probabilidade());
        nc.setNivelRisco(MatrizRisco.calcular(request.severidade(), request.probabilidade()));
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
        if (nc.getStatus() == StatusNaoConformidade.CONCLUIDO && (usuario == null || !usuario.isAdmin())) {
            throw new BusinessException("Apenas administradores podem excluir NCs concluídas");
        }
        if (usuario != null && usuario.getPerfil() == PerfilUsuario.TECNICO && nc.getStatus() != StatusNaoConformidade.ABERTA) {
            throw new BusinessException("Técnico só pode excluir NC com status ABERTA");
        }

        // Remove arquivos do S3 (evidências da NC e das execuções)
        List<Evidencia> evidenciasNc = evidenciaRepository.findByNaoConformidadeId(id);
        for (Evidencia ev : evidenciasNc) {
            s3StorageService.delete(ev.getUrlArquivo());
        }
        if (nc.getExecucoes() != null) {
            for (ExecucaoAcao execucao : nc.getExecucoes()) {
                List<Evidencia> evidenciasExec = evidenciaRepository.findByExecucaoAcaoId(execucao.getId());
                for (Evidencia ev : evidenciasExec) {
                    s3StorageService.delete(ev.getUrlArquivo());
                }
            }
        }

        // ON DELETE CASCADE no banco cuida de todas as tabelas filhas
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

        List<InvestigacaoRequest.PorqueItem> porques = request.porques();
        nc.setPorqueUm(porques.get(0).pergunta());
        nc.setPorqueUmResposta(porques.get(0).resposta());
        nc.setPorqueDois(porques.size() > 1 ? porques.get(1).pergunta() : null);
        nc.setPorqueDoisResposta(porques.size() > 1 ? porques.get(1).resposta() : null);
        nc.setPorqueTres(porques.size() > 2 ? porques.get(2).pergunta() : null);
        nc.setPorqueTresResposta(porques.size() > 2 ? porques.get(2).resposta() : null);
        nc.setPorqueQuatro(porques.size() > 3 ? porques.get(3).pergunta() : null);
        nc.setPorqueQuatroResposta(porques.size() > 3 ? porques.get(3).resposta() : null);
        nc.setPorqueCinco(porques.size() > 4 ? porques.get(4).pergunta() : null);
        nc.setPorqueCincoResposta(porques.size() > 4 ? porques.get(4).resposta() : null);
        nc.setCausaRaiz(request.causaRaiz());

        if (nc.getStatus() == StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO) {
            // Mantém atividades APROVADA; remove REJEITADA e adiciona as corrigidas
            List<AtividadePlanoAcao> rejeitadas = nc.getAtividades().stream()
                    .filter(a -> "REJEITADA".equals(a.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            nc.getAtividades().removeAll(rejeitadas);

            int nextOrdem = nc.getAtividades().stream()
                    .mapToInt(AtividadePlanoAcao::getOrdem).max().orElse(0);
            for (int i = 0; i < request.atividades().size(); i++) {
                var item = request.atividades().get(i);
                AtividadePlanoAcao atividade = new AtividadePlanoAcao();
                atividade.setNaoConformidade(nc);
                atividade.setTitulo(item.titulo());
                atividade.setDescricao(item.descricao());
                atividade.setOrdem(nextOrdem + i + 1);
                atividade.setStatus("PENDENTE");
                nc.getAtividades().add(atividade);
            }
        } else {
            // ABERTA: substitui tudo
            nc.getAtividades().clear();
            for (int i = 0; i < request.atividades().size(); i++) {
                var item = request.atividades().get(i);
                AtividadePlanoAcao atividade = new AtividadePlanoAcao();
                atividade.setNaoConformidade(nc);
                atividade.setTitulo(item.titulo());
                atividade.setDescricao(item.descricao());
                atividade.setOrdem(i + 1);
                atividade.setStatus("PENDENTE");
                nc.getAtividades().add(atividade);
            }
        }

        StatusNaoConformidade statusAnterior = nc.getStatus();
        nc.setStatus(StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);
        naoConformidadeRepository.save(nc);

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);
        registrarHistorico(nc, usuario, TipoAcaoHistorico.SUBMISSAO_INVESTIGACAO, null, statusAnterior, StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO);

        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                statusAnterior, StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO,
                request.emailsManuais(), null, null));

        // Snapshot da investigação submetida
        InvestigacaoSnapshot snapshot = new InvestigacaoSnapshot();
        snapshot.setNaoConformidade(nc);
        snapshot.setPorqueUm(porques.get(0).pergunta());
        snapshot.setPorqueUmResposta(porques.get(0).resposta());
        snapshot.setPorqueDois(porques.size() > 1 ? porques.get(1).pergunta() : null);
        snapshot.setPorqueDoisResposta(porques.size() > 1 ? porques.get(1).resposta() : null);
        snapshot.setPorqueTres(porques.size() > 2 ? porques.get(2).pergunta() : null);
        snapshot.setPorqueTresResposta(porques.size() > 2 ? porques.get(2).resposta() : null);
        snapshot.setPorqueQuatro(porques.size() > 3 ? porques.get(3).pergunta() : null);
        snapshot.setPorqueQuatroResposta(porques.size() > 3 ? porques.get(3).resposta() : null);
        snapshot.setPorqueCinco(porques.size() > 4 ? porques.get(4).pergunta() : null);
        snapshot.setPorqueCincoResposta(porques.size() > 4 ? porques.get(4).resposta() : null);
        snapshot.setCausaRaiz(request.causaRaiz());
        snapshot.setAtividades(request.atividades().stream()
                .map(a -> a.titulo() + " — " + a.descricao())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
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
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_EXECUCAO,
                request != null ? request.emailsManuais() : null, null,
                request != null ? request.comentario() : null));
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse rejeitarPlano(UUID id, RejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO) {
            throw new BusinessException("Plano só pode ser rejeitado quando NC está AGUARDANDO_APROVACAO_PLANO");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.REJEICAO_PLANO, request.motivo(),
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO);

        investigacaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> { s.setStatus("REPROVADO"); s.setComentarioRevisao(request.motivo()); investigacaoSnapshotRepository.save(s); });

        nc.setStatus(StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO);
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO,
                request.emailsManuais(), null, request.motivo()));
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse revisarAtividades(UUID id, RevisarAtividadesRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO) {
            throw new BusinessException("Revisão só pode ser feita quando NC está AGUARDANDO_APROVACAO_PLANO");
        }

        for (var decisao : request.decisoes()) {
            if ("REJEITADA".equals(decisao.status()) && (decisao.motivo() == null || decisao.motivo().isBlank())) {
                throw new BusinessException("Motivo é obrigatório ao rejeitar uma atividade");
            }
            AtividadePlanoAcao atividade = atividadePlanoAcaoRepository.findById(decisao.atividadeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + decisao.atividadeId()));
            if (!atividade.getNaoConformidade().getId().equals(id)) {
                throw new BusinessException("Atividade não pertence a esta NC");
            }
            atividade.setStatus(decisao.status());
            atividade.setMotivoRejeicao("REJEITADA".equals(decisao.status()) ? decisao.motivo() : null);
            atividadePlanoAcaoRepository.save(atividade);
        }

        // Determina novo status: se TODAS as atividades da NC são APROVADA → EM_EXECUCAO
        boolean todasAprovadas = nc.getAtividades().stream()
                .allMatch(a -> "APROVADA".equals(a.getStatus()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        StatusNaoConformidade novoStatus;
        TipoAcaoHistorico tipoAcao;
        String snapshotStatus;

        if (todasAprovadas) {
            novoStatus = StatusNaoConformidade.EM_EXECUCAO;
            tipoAcao = TipoAcaoHistorico.APROVACAO_PLANO;
            snapshotStatus = "APROVADO";
        } else {
            novoStatus = StatusNaoConformidade.EM_AJUSTE_PELO_EXTERNO;
            tipoAcao = TipoAcaoHistorico.REJEICAO_PLANO;
            snapshotStatus = "REPROVADO";
        }

        String comentario = request.comentario();
        registrarHistorico(nc, usuario, tipoAcao, comentario,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, novoStatus);

        final String finalSnapshotStatus = snapshotStatus;
        final List<AtividadePlanoAcao> atividadesRevisadas = new ArrayList<>(nc.getAtividades());
        investigacaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> {
                    // Encode per-activity status into snapshot atividades using "||" delimiter
                    s.setAtividades(atividadesRevisadas.stream()
                            .map(a -> {
                                String suffix = "REJEITADA".equals(a.getStatus())
                                        ? " || REJEITADA" + (a.getMotivoRejeicao() != null ? ": " + a.getMotivoRejeicao() : "")
                                        : " || APROVADA";
                                return a.getTitulo() + " — " + a.getDescricao() + suffix;
                            })
                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
                    s.setStatus(finalSnapshotStatus);
                    s.setComentarioRevisao(comentario);
                    investigacaoSnapshotRepository.save(s);
                });

        nc.setStatus(novoStatus);
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.AGUARDANDO_APROVACAO_PLANO, novoStatus,
                request.emailsManuais(), null, comentario));
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse submeterExecucao(UUID id, SubmeterExecucaoRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.EM_EXECUCAO) {
            throw new BusinessException("Execução só pode ser submetida quando NC está EM_EXECUCAO");
        }

        for (var item : request.atividades()) {
            AtividadePlanoAcao atividade = atividadePlanoAcaoRepository.findById(item.atividadeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + item.atividadeId()));
            if (!atividade.getNaoConformidade().getId().equals(id)) {
                throw new BusinessException("Atividade não pertence a esta NC");
            }
            // Only update activities not yet approved in execution
            if (!"APROVADA".equals(atividade.getStatusExecucao())) {
                atividade.setDescricaoExecucao(item.descricaoExecucao());
                atividade.setStatusExecucao("PENDENTE");
                atividadePlanoAcaoRepository.save(atividade);
            }
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.SUBMISSAO_EVIDENCIAS, null,
                StatusNaoConformidade.EM_EXECUCAO, StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);

        ExecucaoSnapshot execSnapshot = new ExecucaoSnapshot();
        execSnapshot.setNaoConformidade(nc);
        execSnapshot.setDescricaoExecucao("");
        execSnapshot.setDataSubmissao(LocalDateTime.now());
        execSnapshot.setStatus("PENDENTE");
        // Store per-activity execution data with evidence IDs
        execSnapshot.setAtividades(nc.getAtividades().stream()
                .map(a -> {
                    String desc = a.getDescricaoExecucao() != null ? a.getDescricaoExecucao() : "";
                    List<Evidencia> evs = evidenciaRepository.findByAtividadePlanoAcaoId(a.getId());
                    String evIds = evs.stream().map(e -> e.getId().toString()).collect(java.util.stream.Collectors.joining(","));
                    return a.getTitulo() + " — " + desc + (evIds.isEmpty() ? "" : " §§ " + evIds);
                })
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        execucaoSnapshotRepository.save(execSnapshot);

        nc.setStatus(StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL);
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.EM_EXECUCAO, StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL,
                request.emailsManuais(), null, null));
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse revisarExecucao(UUID id, RevisarExecucaoRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL) {
            throw new BusinessException("Revisão da execução só pode ser feita quando NC está AGUARDANDO_VALIDACAO_FINAL");
        }

        for (var decisao : request.decisoes()) {
            if ("REJEITADA".equals(decisao.status()) && (decisao.motivo() == null || decisao.motivo().isBlank())) {
                throw new BusinessException("Motivo é obrigatório ao rejeitar uma atividade");
            }
            AtividadePlanoAcao atividade = atividadePlanoAcaoRepository.findById(decisao.atividadeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Atividade não encontrada: " + decisao.atividadeId()));
            if (!atividade.getNaoConformidade().getId().equals(id)) {
                throw new BusinessException("Atividade não pertence a esta NC");
            }
            atividade.setStatusExecucao(decisao.status());
            atividade.setMotivoRejeicaoExecucao("REJEITADA".equals(decisao.status()) ? decisao.motivo() : null);
            atividadePlanoAcaoRepository.save(atividade);
        }

        boolean todasAprovadas = nc.getAtividades().stream()
                .allMatch(a -> "APROVADA".equals(a.getStatusExecucao()));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        StatusNaoConformidade novoStatus;
        TipoAcaoHistorico tipoAcao;
        String snapshotStatus;

        if (todasAprovadas) {
            novoStatus = StatusNaoConformidade.CONCLUIDO;
            tipoAcao = TipoAcaoHistorico.APROVACAO_EVIDENCIAS;
            snapshotStatus = "APROVADO";
        } else {
            novoStatus = StatusNaoConformidade.EM_EXECUCAO;
            tipoAcao = TipoAcaoHistorico.REJEICAO_EVIDENCIAS;
            snapshotStatus = "REPROVADO";
        }

        String comentario = request.comentario();
        registrarHistorico(nc, usuario, tipoAcao, comentario,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, novoStatus);

        final String finalSnapshotStatus = snapshotStatus;
        final List<AtividadePlanoAcao> atividadesRevisadasExec = new ArrayList<>(nc.getAtividades());
        execucaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> {
                    // Encode per-activity execution status + evidence IDs into snapshot
                    s.setAtividades(atividadesRevisadasExec.stream()
                            .map(a -> {
                                String desc = a.getDescricaoExecucao() != null ? a.getDescricaoExecucao() : "";
                                String suffix = "REJEITADA".equals(a.getStatusExecucao())
                                        ? " || REJEITADA" + (a.getMotivoRejeicaoExecucao() != null ? ": " + a.getMotivoRejeicaoExecucao() : "")
                                        : " || APROVADA";
                                List<Evidencia> evs = evidenciaRepository.findByAtividadePlanoAcaoId(a.getId());
                                String evIds = evs.stream().map(e -> e.getId().toString()).collect(java.util.stream.Collectors.joining(","));
                                return a.getTitulo() + " — " + desc + suffix + (evIds.isEmpty() ? "" : " §§ " + evIds);
                            })
                            .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
                    s.setStatus(finalSnapshotStatus);
                    s.setComentarioRevisao(comentario);
                    execucaoSnapshotRepository.save(s);
                });

        nc.setStatus(novoStatus);
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, novoStatus,
                request.emailsManuais(), null, comentario));
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
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.EM_EXECUCAO, StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL,
                request.emailsManuais(), null, null));
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse aprovarEvidencias(UUID id, AprovarRejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL) {
            throw new BusinessException("Evidências só podem ser aprovadas quando NC está AGUARDANDO_VALIDACAO_FINAL");
        }

        boolean todasAprovadas = nc.getAtividades() == null || nc.getAtividades().stream()
                .allMatch(a -> "APROVADA".equals(a.getStatusExecucao()));
        if (!todasAprovadas) {
            throw new BusinessException("Todas as atividades devem estar com execução aprovada antes de concluir a NC");
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
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.CONCLUIDO,
                request != null ? request.emailsManuais() : null, null,
                request != null ? request.comentario() : null));
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse rejeitarEvidencias(UUID id, RejeitarRequest request) {
        NaoConformidade nc = findNcOrThrow(id);

        if (nc.getStatus() != StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL) {
            throw new BusinessException("Evidências só podem ser rejeitadas quando NC está AGUARDANDO_VALIDACAO_FINAL");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);

        registrarHistorico(nc, usuario, TipoAcaoHistorico.REJEICAO_EVIDENCIAS, request.motivo(),
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.EM_EXECUCAO);

        execucaoSnapshotRepository
                .findFirstByNaoConformidadeIdAndStatusOrderByDataSubmissaoDesc(id, "PENDENTE")
                .ifPresent(s -> { s.setStatus("REPROVADO"); s.setComentarioRevisao(request.motivo()); execucaoSnapshotRepository.save(s); });

        nc.setStatus(StatusNaoConformidade.EM_EXECUCAO);
        eventPublisher.publishEvent(new NcEmailEvent(this, id,
                StatusNaoConformidade.AGUARDANDO_VALIDACAO_FINAL, StatusNaoConformidade.EM_EXECUCAO,
                request.emailsManuais(), null, request.motivo()));
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
                        n.getId(), n.getTitulo(), n.getDescricao(), n.getConteudo(), n.isAtivo(), n.getDtInativacao()
                )).toList();

        // Bulk fetch de evidências por atividade (evita N+1)
        List<UUID> atividadeIds = nc.getAtividades() == null ? List.of() :
                nc.getAtividades().stream().map(AtividadePlanoAcao::getId).toList();
        Map<UUID, List<EvidenciaResponse>> evidenciasPorAtividade = atividadeIds.isEmpty() ? Map.of() :
                evidenciaRepository.findByAtividadePlanoAcaoIdIn(atividadeIds).stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getAtividadePlanoAcao().getId(),
                                Collectors.mapping(
                                        e -> new EvidenciaResponse(e.getId(), e.getNomeArquivo(), e.getUrlArquivo(), e.getDataUpload(), e.getTipoEvidencia() != null ? e.getTipoEvidencia().name() : null),
                                        Collectors.toList()
                                )
                        ));

        List<AtividadeResponse> atividades = nc.getAtividades() == null ? List.of() :
                nc.getAtividades().stream().map(a -> {
                    List<EvidenciaResponse> evs = evidenciasPorAtividade.getOrDefault(a.getId(), List.of());
                    return new AtividadeResponse(
                            a.getId(), a.getTitulo(), a.getDescricao(), a.getOrdem(),
                            a.getStatus(), a.getMotivoRejeicao(),
                            a.getDescricaoExecucao(), a.getStatusExecucao(), a.getMotivoRejeicaoExecucao(),
                            evs
                    );
                }).toList();

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
                                        .map(e -> new EvidenciaResponse(e.getId(), e.getNomeArquivo(), e.getUrlArquivo(), e.getDataUpload(), e.getTipoEvidencia() != null ? e.getTipoEvidencia().name() : null))
                                        .toList(),
                                s.getAtividades()
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
                nc.getSeveridade(),
                nc.getProbabilidade(),
                nc.getNivelRisco(),
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getId() : null,
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getNome() : null,
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getEmail() : null,
                nc.getEngResponsavelConstrutora() != null ? nc.getEngResponsavelConstrutora().getPerfil().name() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getId() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getNome() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getEmail() : null,
                nc.getEngResponsavelVerificacao() != null ? nc.getEngResponsavelVerificacao().getPerfil().name() : null,
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
                normas,
                nc.getUsuarioCriacao() != null ? nc.getUsuarioCriacao().getId() : null
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

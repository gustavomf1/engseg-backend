package com.engseg.service;

import com.engseg.dto.request.*;
import com.engseg.dto.response.DesvioResponse;
import com.engseg.dto.response.HistoricoDesvioResponse;
import com.engseg.dto.response.TrativaDesvioResponse;
import com.engseg.entity.*;
import com.engseg.event.DesvioEmailEvent;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DesvioService {

    private final DesvioRepository desvioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final HistoricoDesvioRepository historicoDesvioRepository;
    private final TrativaDesvioRepository trativaDesvioRepository;
    private final S3StorageService s3StorageService;
    private final SecurityHelper securityHelper;
    private final ApplicationEventPublisher eventPublisher;

    public List<DesvioResponse> findAll(UUID estabelecimentoId, UUID empresaId) {
        if (securityHelper.isExterno()) {
            List<UUID> permitidos = securityHelper.getEstabelecimentosDoExterno();
            if (permitidos.isEmpty()) return List.of();
            if (estabelecimentoId != null) {
                return desvioRepository.findByEstabelecimentoId(estabelecimentoId).stream()
                        .filter(d -> permitidos.contains(d.getEstabelecimento().getId()))
                        .map(this::toResponse)
                        .toList();
            }
            return desvioRepository.findByEstabelecimentoIdIn(permitidos).stream()
                    .map(this::toResponse)
                    .toList();
        }
        List<Desvio> list;
        if (estabelecimentoId != null) {
            list = desvioRepository.findByEstabelecimentoId(estabelecimentoId);
        } else if (empresaId != null) {
            list = desvioRepository.findByEstabelecimento_EmpresaId(empresaId);
        } else {
            list = desvioRepository.findAll();
        }

        if (securityHelper.isTecnico()) {
            var usuarioLogado = securityHelper.getUsuarioLogado();
            UUID uid = usuarioLogado.getId();
            list = list.stream()
                    .filter(d ->
                        (d.getUsuarioCriacao() != null && d.getUsuarioCriacao().getId().equals(uid)) ||
                        (d.getResponsavelTratativa() != null && d.getResponsavelTratativa().getId().equals(uid)) ||
                        (d.getResponsavelDesvio() != null && d.getResponsavelDesvio().getId().equals(uid)))
                    .toList();
        }

        return list.stream().map(this::toResponse).toList();
    }

    public DesvioResponse findById(UUID id) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (securityHelper.isExterno()) {
            var usuarioLogado = securityHelper.getUsuarioLogado();
            if (desvio.getResponsavelTratativa() == null ||
                    !desvio.getResponsavelTratativa().getId().equals(usuarioLogado.getId())) {
                throw new BusinessException("Acesso negado a este desvio");
            }
        }

        return toResponse(desvio);
    }

    @Transactional
    public DesvioResponse create(DesvioRequest request) {
        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        var localizacao = request.localizacaoId() != null
                ? localizacaoRepository.findById(request.localizacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + request.localizacaoId()))
                : null;

        var responsavelDesvio = usuarioRepository.findById(request.responsavelDesvioId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável pelo desvio não encontrado: " + request.responsavelDesvioId()));

        if (responsavelDesvio.getPerfil() == PerfilUsuario.EXTERNO || responsavelDesvio.getPerfil() == PerfilUsuario.TECNICO) {
            throw new BusinessException("Responsável pelo desvio deve ter perfil ENGENHEIRO ou ser administrador");
        }

        var responsavelTratativa = usuarioRepository.findById(request.responsavelTratativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável pela tratativa não encontrado: " + request.responsavelTratativaId()));

        var usuarioLogado = securityHelper.getUsuarioLogado();

        Desvio desvio = new Desvio();
        desvio.setEstabelecimento(estabelecimento);
        desvio.setTitulo(request.titulo());
        desvio.setLocalizacao(localizacao);
        desvio.setDescricao(request.descricao());
        desvio.setDataRegistro(LocalDateTime.now());
        desvio.setTecnico(usuarioLogado);
        desvio.setUsuarioCriacao(usuarioLogado);
        desvio.setRegraDeOuro(request.regraDeOuro());
        desvio.setOrientacaoRealizada(request.orientacaoRealizada());
        desvio.setResponsavelDesvio(responsavelDesvio);
        desvio.setResponsavelTratativa(responsavelTratativa);
        desvio.setStatus(StatusDesvio.AGUARDANDO_TRATATIVA);

        Desvio saved = desvioRepository.save(desvio);

        historicoDesvioRepository.save(HistoricoDesvio.builder()
                .desvio(saved)
                .usuario(usuarioLogado)
                .tipo(TipoAcaoHistoricoDesvio.CRIACAO)
                .statusAtual(StatusDesvio.AGUARDANDO_TRATATIVA)
                .dataAcao(LocalDateTime.now())
                .build());

        eventPublisher.publishEvent(new DesvioEmailEvent(
                this, saved.getId(), null, StatusDesvio.AGUARDANDO_TRATATIVA,
                request.emailsManuais(), request.emailsPadraoExcluidos(), null));

        return toResponse(saved);
    }

    @Transactional
    public DesvioResponse update(UUID id, DesvioRequest request) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() != StatusDesvio.AGUARDANDO_TRATATIVA) {
            throw new BusinessException("Só é possível editar um desvio com status AGUARDANDO_TRATATIVA");
        }

        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        var localizacao = request.localizacaoId() != null
                ? localizacaoRepository.findById(request.localizacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + request.localizacaoId()))
                : null;

        var responsavelDesvio = usuarioRepository.findById(request.responsavelDesvioId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável pelo desvio não encontrado: " + request.responsavelDesvioId()));

        if (responsavelDesvio.getPerfil() == PerfilUsuario.EXTERNO || responsavelDesvio.getPerfil() == PerfilUsuario.TECNICO) {
            throw new BusinessException("Responsável pelo desvio deve ter perfil ENGENHEIRO ou ser administrador");
        }

        var responsavelTratativa = usuarioRepository.findById(request.responsavelTratativaId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável pela tratativa não encontrado: " + request.responsavelTratativaId()));

        desvio.setEstabelecimento(estabelecimento);
        desvio.setTitulo(request.titulo());
        desvio.setLocalizacao(localizacao);
        desvio.setDescricao(request.descricao());
        desvio.setRegraDeOuro(request.regraDeOuro());
        desvio.setOrientacaoRealizada(request.orientacaoRealizada());
        desvio.setResponsavelDesvio(responsavelDesvio);
        desvio.setResponsavelTratativa(responsavelTratativa);

        return toResponse(desvioRepository.save(desvio));
    }

    @Transactional
    public DesvioResponse adicionarTratativa(UUID id, AdicionarTrativaRequest request) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() != StatusDesvio.AGUARDANDO_TRATATIVA) {
            throw new BusinessException("Só é possível adicionar tratativas quando o desvio está aguardando tratativa");
        }

        var usuarioLogado = securityHelper.getUsuarioLogado();
        boolean isResponsavel = desvio.getResponsavelTratativa() != null &&
                desvio.getResponsavelTratativa().getId().equals(usuarioLogado.getId());
        if (!isResponsavel && !usuarioLogado.isAdmin()) {
            throw new BusinessException("Apenas o responsável pela tratativa pode adicionar tratativas");
        }

        List<Evidencia> evidencias = request.evidenciaIds().stream()
                .map(eid -> evidenciaRepository.findById(eid)
                        .orElseThrow(() -> new ResourceNotFoundException("Evidência não encontrada: " + eid)))
                .toList();

        int numero = (int) trativaDesvioRepository.countByDesvioId(id) + 1;

        trativaDesvioRepository.save(TrativaDesvio.builder()
                .desvio(desvio)
                .titulo(request.titulo())
                .descricao(request.descricao())
                .evidencias(new ArrayList<>(evidencias))
                .status(StatusTratativaDesvio.PENDENTE)
                .numero(numero)
                .dtCriacao(LocalDateTime.now())
                .build());

        return toResponse(desvioRepository.findById(id).orElseThrow());
    }

    @Transactional
    public void removerTratativa(UUID id, UUID trativaId) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() != StatusDesvio.AGUARDANDO_TRATATIVA) {
            throw new BusinessException("Só é possível remover tratativas quando o desvio está aguardando tratativa");
        }

        TrativaDesvio tratativa = trativaDesvioRepository.findById(trativaId)
                .orElseThrow(() -> new ResourceNotFoundException("Tratativa não encontrada: " + trativaId));

        if (!tratativa.getDesvio().getId().equals(id)) {
            throw new BusinessException("Tratativa não pertence a este desvio");
        }

        if (tratativa.getStatus() != StatusTratativaDesvio.PENDENTE) {
            throw new BusinessException("Só é possível remover tratativas com status PENDENTE");
        }

        trativaDesvioRepository.delete(tratativa);
    }

    @Transactional
    public DesvioResponse submeterTratativa(UUID id, SubmeterTrativaDesvioRequest request) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() != StatusDesvio.AGUARDANDO_TRATATIVA) {
            throw new BusinessException("Desvio não está aguardando tratativa");
        }

        var usuarioLogado = securityHelper.getUsuarioLogado();
        boolean isResponsavel = desvio.getResponsavelTratativa() != null &&
                desvio.getResponsavelTratativa().getId().equals(usuarioLogado.getId());
        if (!isResponsavel && !usuarioLogado.isAdmin()) {
            throw new BusinessException("Apenas o responsável pela tratativa pode submeter");
        }

        List<TrativaDesvio> pendentes = trativaDesvioRepository
                .findByDesvioIdAndStatus(id, StatusTratativaDesvio.PENDENTE);
        if (pendentes.isEmpty()) {
            throw new BusinessException("É necessário adicionar pelo menos uma tratativa antes de submeter");
        }

        int rodada = trativaDesvioRepository.findMaxRodadaByDesvioId(id).orElse(0) + 1;
        pendentes.forEach(t -> t.setRodada(rodada));
        trativaDesvioRepository.saveAll(pendentes);

        StatusDesvio anterior = desvio.getStatus();
        desvio.setStatus(StatusDesvio.AGUARDANDO_APROVACAO);
        Desvio saved = desvioRepository.save(desvio);

        historicoDesvioRepository.save(HistoricoDesvio.builder()
                .desvio(saved)
                .usuario(usuarioLogado)
                .tipo(TipoAcaoHistoricoDesvio.TRATATIVA_SUBMETIDA)
                .statusAnterior(anterior)
                .statusAtual(StatusDesvio.AGUARDANDO_APROVACAO)
                .dataAcao(LocalDateTime.now())
                .build());

        eventPublisher.publishEvent(new DesvioEmailEvent(
                this, saved.getId(), anterior, StatusDesvio.AGUARDANDO_APROVACAO,
                request != null ? request.emailsManuais() : List.of(), List.of(), null));

        return toResponse(saved);
    }

    @Transactional
    public DesvioResponse aprovar(UUID id, AprovarDesvioRequest request) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() != StatusDesvio.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Desvio não está aguardando aprovação");
        }

        var usuarioLogado = securityHelper.getUsuarioLogado();
        boolean isResponsavel = desvio.getResponsavelDesvio() != null &&
                desvio.getResponsavelDesvio().getId().equals(usuarioLogado.getId());
        if (!isResponsavel && !usuarioLogado.isAdmin()) {
            throw new BusinessException("Apenas o responsável pelo desvio pode aprovar");
        }

        List<TrativaDesvio> pendentes = trativaDesvioRepository
                .findByDesvioIdAndStatus(id, StatusTratativaDesvio.PENDENTE);
        pendentes.forEach(t -> t.setStatus(StatusTratativaDesvio.APROVADO));
        trativaDesvioRepository.saveAll(pendentes);

        StatusDesvio anterior = desvio.getStatus();
        desvio.setStatus(StatusDesvio.CONCLUIDO);
        Desvio saved = desvioRepository.save(desvio);

        historicoDesvioRepository.save(HistoricoDesvio.builder()
                .desvio(saved)
                .usuario(usuarioLogado)
                .tipo(TipoAcaoHistoricoDesvio.APROVADO)
                .comentario(request.comentario())
                .statusAnterior(anterior)
                .statusAtual(StatusDesvio.CONCLUIDO)
                .dataAcao(LocalDateTime.now())
                .build());

        eventPublisher.publishEvent(new DesvioEmailEvent(
                this, saved.getId(), anterior, StatusDesvio.CONCLUIDO,
                request.emailsManuais(), List.of(), request.comentario()));

        return toResponse(saved);
    }

    @Transactional
    public DesvioResponse reprovar(UUID id, ReprovarTrativasDesvioRequest request) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() != StatusDesvio.AGUARDANDO_APROVACAO) {
            throw new BusinessException("Desvio não está aguardando aprovação");
        }

        var usuarioLogado = securityHelper.getUsuarioLogado();
        boolean isResponsavel = desvio.getResponsavelDesvio() != null &&
                desvio.getResponsavelDesvio().getId().equals(usuarioLogado.getId());
        if (!isResponsavel && !usuarioLogado.isAdmin()) {
            throw new BusinessException("Apenas o responsável pelo desvio pode reprovar");
        }

        List<UUID> reprovadaIds = request.itens().stream()
                .map(ReprovarTrativasDesvioRequest.ItemReprovacao::trativaId).toList();

        List<String> motivosSummary = new ArrayList<>();
        for (var item : request.itens()) {
            TrativaDesvio tratativa = trativaDesvioRepository.findById(item.trativaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tratativa não encontrada: " + item.trativaId()));
            if (!tratativa.getDesvio().getId().equals(id)) {
                throw new BusinessException("Tratativa não pertence a este desvio");
            }
            tratativa.setStatus(StatusTratativaDesvio.REPROVADO);
            tratativa.setMotivoReprovacao(item.motivo());
            trativaDesvioRepository.save(tratativa);
            motivosSummary.add("Tratativa " + tratativa.getNumero() + ": " + item.motivo());
        }

        // Não-reprovadas desta rodada são implicitamente aceitas
        trativaDesvioRepository.findByDesvioIdAndStatus(id, StatusTratativaDesvio.PENDENTE).stream()
                .filter(t -> !reprovadaIds.contains(t.getId()))
                .forEach(t -> { t.setStatus(StatusTratativaDesvio.APROVADO); trativaDesvioRepository.save(t); });

        StatusDesvio anterior = desvio.getStatus();
        desvio.setStatus(StatusDesvio.AGUARDANDO_TRATATIVA);
        Desvio saved = desvioRepository.save(desvio);

        historicoDesvioRepository.save(HistoricoDesvio.builder()
                .desvio(saved)
                .usuario(usuarioLogado)
                .tipo(TipoAcaoHistoricoDesvio.REPROVADO)
                .comentario(String.join("; ", motivosSummary))
                .statusAnterior(anterior)
                .statusAtual(StatusDesvio.AGUARDANDO_TRATATIVA)
                .dataAcao(LocalDateTime.now())
                .build());

        eventPublisher.publishEvent(new DesvioEmailEvent(
                this, saved.getId(), anterior, StatusDesvio.AGUARDANDO_TRATATIVA,
                request.emailsManuais(), List.of(), String.join("; ", motivosSummary)));

        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        var usuario = securityHelper.getUsuarioLogado();
        if (desvio.getStatus() == StatusDesvio.CONCLUIDO && !usuario.isAdmin()) {
            throw new BusinessException("Apenas administradores podem excluir desvios concluídos");
        }

        List<Evidencia> evidencias = evidenciaRepository.findByDesvioId(id);
        for (Evidencia ev : evidencias) {
            s3StorageService.delete(ev.getUrlArquivo());
        }
        evidenciaRepository.deleteAll(evidencias);
        desvioRepository.delete(desvio);
    }

    private DesvioResponse toResponse(Desvio d) {
        List<HistoricoDesvioResponse> historico = d.getHistorico() != null
                ? d.getHistorico().stream().map(this::toHistoricoResponse).toList()
                : List.of();

        List<TrativaDesvioResponse> tratativas = trativaDesvioRepository
                .findByDesvioIdOrderByNumeroAsc(d.getId())
                .stream().map(this::toTrativaResponse).toList();

        return new DesvioResponse(
                d.getId(),
                d.getEstabelecimento().getId(),
                d.getEstabelecimento().getNome(),
                d.getTitulo(),
                d.getLocalizacao() != null ? d.getLocalizacao().getId() : null,
                d.getLocalizacao() != null ? d.getLocalizacao().getNome() : null,
                d.getDescricao(),
                d.getDataRegistro(),
                d.getTecnico() != null ? d.getTecnico().getNome() : null,
                d.getUsuarioCriacao() != null ? d.getUsuarioCriacao().getNome() : null,
                d.getUsuarioCriacao() != null ? d.getUsuarioCriacao().getEmail() : null,
                d.getOrientacaoRealizada(),
                d.isRegraDeOuro(),
                d.getStatus(),
                d.getResponsavelDesvio() != null ? d.getResponsavelDesvio().getId() : null,
                d.getResponsavelDesvio() != null ? d.getResponsavelDesvio().getNome() : null,
                d.getResponsavelTratativa() != null ? d.getResponsavelTratativa().getId() : null,
                d.getResponsavelTratativa() != null ? d.getResponsavelTratativa().getNome() : null,
                d.getObservacaoTratativa(),
                d.getEvidenciaTratativa() != null ? d.getEvidenciaTratativa().getId() : null,
                d.getEvidenciaTratativa() != null ? d.getEvidenciaTratativa().getNomeArquivo() : null,
                d.getEvidenciaTratativa() != null ? d.getEvidenciaTratativa().getUrlArquivo() : null,
                historico,
                tratativas,
                d.getUsuarioCriacao() != null ? d.getUsuarioCriacao().getId() : null
        );
    }

    private TrativaDesvioResponse toTrativaResponse(TrativaDesvio t) {
        List<TrativaDesvioResponse.EvidenciaInfo> evidencias = t.getEvidencias() != null
                ? t.getEvidencias().stream()
                        .map(e -> new TrativaDesvioResponse.EvidenciaInfo(e.getId(), e.getNomeArquivo(), e.getUrlArquivo()))
                        .toList()
                : List.of();
        return new TrativaDesvioResponse(
                t.getId(),
                t.getTitulo(),
                t.getDescricao(),
                evidencias,
                t.getStatus(),
                t.getMotivoReprovacao(),
                t.getNumero(),
                t.getRodada(),
                t.getDtCriacao()
        );
    }

    private HistoricoDesvioResponse toHistoricoResponse(HistoricoDesvio h) {
        return new HistoricoDesvioResponse(
                h.getId(),
                h.getTipo(),
                h.getUsuario() != null ? h.getUsuario().getNome() : null,
                h.getComentario(),
                h.getStatusAnterior(),
                h.getStatusAtual(),
                h.getSnapshotObservacao(),
                h.getSnapshotEvidenciaId(),
                h.getDataAcao()
        );
    }
}

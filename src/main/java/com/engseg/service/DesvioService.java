package com.engseg.service;

import com.engseg.dto.request.*;
import com.engseg.dto.response.DesvioResponse;
import com.engseg.dto.response.HistoricoDesvioResponse;
import com.engseg.entity.*;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final S3StorageService s3StorageService;
    private final SecurityHelper securityHelper;

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
        if (estabelecimentoId != null) {
            return desvioRepository.findByEstabelecimentoId(estabelecimentoId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (empresaId != null) {
            return desvioRepository.findByEstabelecimento_EmpresaId(empresaId).stream()
                    .map(this::toResponse)
                    .toList();
        }
        return desvioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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

        if (responsavelDesvio.getPerfil() == PerfilUsuario.EXTERNO) {
            throw new BusinessException("Responsável pelo desvio não pode ter perfil EXTERNO");
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

        if (responsavelDesvio.getPerfil() == PerfilUsuario.EXTERNO) {
            throw new BusinessException("Responsável pelo desvio não pode ter perfil EXTERNO");
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
    public DesvioResponse submeterTratativa(UUID id, SubmeterTrativaRequest request) {
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

        var evidencia = evidenciaRepository.findById(request.evidenciaId())
                .orElseThrow(() -> new ResourceNotFoundException("Evidência não encontrada: " + request.evidenciaId()));

        StatusDesvio anterior = desvio.getStatus();
        desvio.setObservacaoTratativa(request.observacao());
        desvio.setEvidenciaTratativa(evidencia);
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

        return toResponse(saved);
    }

    @Transactional
    public DesvioResponse reprovar(UUID id, ReprovarDesvioRequest request) {
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

        StatusDesvio anterior = desvio.getStatus();
        UUID snapshotEvidId = desvio.getEvidenciaTratativa() != null
                ? desvio.getEvidenciaTratativa().getId() : null;
        String snapshotObs = desvio.getObservacaoTratativa();

        desvio.setObservacaoTratativa(null);
        desvio.setEvidenciaTratativa(null);
        desvio.setStatus(StatusDesvio.AGUARDANDO_TRATATIVA);

        Desvio saved = desvioRepository.save(desvio);

        historicoDesvioRepository.save(HistoricoDesvio.builder()
                .desvio(saved)
                .usuario(usuarioLogado)
                .tipo(TipoAcaoHistoricoDesvio.REPROVADO)
                .comentario(request.motivo())
                .statusAnterior(anterior)
                .statusAtual(StatusDesvio.AGUARDANDO_TRATATIVA)
                .snapshotObservacao(snapshotObs)
                .snapshotEvidenciaId(snapshotEvidId)
                .dataAcao(LocalDateTime.now())
                .build());

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
                historico
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

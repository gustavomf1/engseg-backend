package com.engseg.service;

import com.engseg.dto.request.DevolutivaRequest;
import com.engseg.dto.request.ExecucaoAcaoRequest;
import com.engseg.dto.request.NaoConformidadeRequest;
import com.engseg.dto.request.ValidacaoRequest;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NaoConformidadeService {

    private final NaoConformidadeRepository naoConformidadeRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DevolutivaRepository devolutivaRepository;
    private final ExecucaoAcaoRepository execucaoAcaoRepository;
    private final ValidacaoRepository validacaoRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final S3StorageService s3StorageService;

    public List<NaoConformidadeResponse> findAll(StatusNaoConformidade status, UUID estabelecimentoId) {
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
        nc.setNrRelacionada(request.nrRelacionada());
        nc.setNivelSeveridade(request.nivelSeveridade());
        if (engConstrutora != null) nc.setEngResponsavelConstrutora(engConstrutora);
        if (engVerificacao != null) nc.setEngResponsavelVerificacao(engVerificacao);
        nc.setDataLimiteResolucao(now.toLocalDate().plusDays(30));
        nc.setStatus(StatusNaoConformidade.ABERTA);

        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse update(UUID id, NaoConformidadeRequest request) {
        NaoConformidade nc = naoConformidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));

        // Técnico só pode editar NC com status ABERTA
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
        nc.setNrRelacionada(request.nrRelacionada());
        nc.setNivelSeveridade(request.nivelSeveridade());
        nc.setEngResponsavelConstrutora(engConstrutora);
        nc.setEngResponsavelVerificacao(engVerificacao);

        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public void delete(UUID id) {
        NaoConformidade nc = naoConformidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));

        // Técnico só pode excluir NC com status ABERTA
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario != null && usuario.getPerfil() == PerfilUsuario.TECNICO && nc.getStatus() != StatusNaoConformidade.ABERTA) {
            throw new BusinessException("Técnico só pode excluir NC com status ABERTA");
        }

        // Deletar evidências do S3
        List<Evidencia> evidencias = evidenciaRepository.findByNaoConformidadeId(id);
        for (Evidencia ev : evidencias) {
            s3StorageService.delete(ev.getUrlArquivo());
        }
        evidenciaRepository.deleteAll(evidencias);

        // Cascade deleta devolutivas, execuções e validação
        naoConformidadeRepository.delete(nc);
    }

    @Transactional
    public NaoConformidadeResponse registrarDevolutiva(UUID id, DevolutivaRequest request) {
        NaoConformidade nc = naoConformidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));

        if (nc.getStatus() != StatusNaoConformidade.ABERTA) {
            throw new BusinessException("Devolutiva só pode ser registrada em NCs com status ABERTA");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var engenheiro = usuarioRepository.findByEmail(email).orElse(null);

        Devolutiva devolutiva = new Devolutiva();
        devolutiva.setNaoConformidade(nc);
        devolutiva.setDescricaoPlanoAcao(request.descricaoPlanoAcao());
        devolutiva.setDataDevolutiva(LocalDateTime.now());
        devolutiva.setEngenheiro(engenheiro);

        devolutivaRepository.save(devolutiva);

        nc.setStatus(StatusNaoConformidade.EM_TRATAMENTO);
        return toResponse(naoConformidadeRepository.save(nc));
    }

    @Transactional
    public NaoConformidadeResponse registrarExecucaoAcao(UUID id, ExecucaoAcaoRequest request) {
        NaoConformidade nc = naoConformidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));

        if (nc.getStatus() != StatusNaoConformidade.EM_TRATAMENTO) {
            throw new BusinessException("Execução de ação só pode ser registrada em NCs com status EM_TRATAMENTO");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var engenheiro = usuarioRepository.findByEmail(email).orElse(null);

        ExecucaoAcao execucao = new ExecucaoAcao();
        execucao.setNaoConformidade(nc);
        execucao.setDescricaoAcaoExecutada(request.descricaoAcaoExecutada());
        execucao.setDataExecucao(LocalDateTime.now());
        execucao.setEngenheiro(engenheiro);

        execucaoAcaoRepository.save(execucao);

        return toResponse(naoConformidadeRepository.findById(id).orElseThrow());
    }

    @Transactional
    public NaoConformidadeResponse validar(UUID id, ValidacaoRequest request) {
        NaoConformidade nc = naoConformidadeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não conformidade não encontrada: " + id));

        if (nc.getStatus() != StatusNaoConformidade.EM_TRATAMENTO) {
            throw new BusinessException("Validação só pode ser feita em NCs com status EM_TRATAMENTO");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var engenheiro = usuarioRepository.findByEmail(email).orElse(null);

        Validacao validacao = new Validacao();
        validacao.setNaoConformidade(nc);
        validacao.setParecer(request.parecer());
        validacao.setObservacao(request.observacao());
        validacao.setDataValidacao(LocalDateTime.now());
        validacao.setEngenheiro(engenheiro);

        validacaoRepository.save(validacao);

        if (request.parecer() == ParecerValidacao.APROVADO) {
            nc.setStatus(StatusNaoConformidade.CONCLUIDO);
            naoConformidadeRepository.save(nc);
        }

        return toResponse(naoConformidadeRepository.findById(id).orElseThrow());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void atualizarStatusNaoResolvida() {
        List<NaoConformidade> vencidas = naoConformidadeRepository.findVencidas(LocalDate.now());
        log.info("Atualizando {} NCs vencidas para NAO_RESOLVIDA", vencidas.size());
        for (NaoConformidade nc : vencidas) {
            nc.setStatus(StatusNaoConformidade.NAO_RESOLVIDA);
        }
        naoConformidadeRepository.saveAll(vencidas);
    }

    private NaoConformidadeResponse toResponse(NaoConformidade nc) {
        List<DevolutivaResponse> devolutivas = nc.getDevolutivas() == null ? List.of() :
                nc.getDevolutivas().stream().map(d -> new DevolutivaResponse(
                        d.getId(),
                        d.getDescricaoPlanoAcao(),
                        d.getDataDevolutiva(),
                        d.getEngenheiro() != null ? d.getEngenheiro().getNome() : null
                )).toList();

        List<ExecucaoAcaoResponse> execucoes = nc.getExecucoes() == null ? List.of() :
                nc.getExecucoes().stream().map(e -> new ExecucaoAcaoResponse(
                        e.getId(),
                        e.getDescricaoAcaoExecutada(),
                        e.getDataExecucao(),
                        e.getEngenheiro() != null ? e.getEngenheiro().getNome() : null
                )).toList();

        ValidacaoResponse validacaoResponse = null;
        if (nc.getValidacao() != null) {
            var v = nc.getValidacao();
            validacaoResponse = new ValidacaoResponse(
                    v.getId(),
                    v.getParecer(),
                    v.getObservacao(),
                    v.getDataValidacao(),
                    v.getEngenheiro() != null ? v.getEngenheiro().getNome() : null
            );
        }

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
                nc.getNrRelacionada(),
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
                devolutivas,
                execucoes,
                validacaoResponse
        );
    }
}

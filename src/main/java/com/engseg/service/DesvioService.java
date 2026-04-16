package com.engseg.service;

import com.engseg.dto.request.DesvioRequest;
import com.engseg.dto.response.DesvioResponse;
import com.engseg.entity.Desvio;
import com.engseg.entity.Evidencia;
import com.engseg.entity.StatusDesvio;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.DesvioRepository;
import com.engseg.repository.EstabelecimentoRepository;
import com.engseg.repository.EvidenciaRepository;
import com.engseg.repository.LocalizacaoRepository;
import com.engseg.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final S3StorageService s3StorageService;
    private final SecurityHelper securityHelper;

    public List<DesvioResponse> findAll(UUID estabelecimentoId, UUID empresaId) {
        // EXTERNO: restrito aos estabelecimentos vinculados à sua empresa
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
        // ENGENHEIRO / TECNICO / ADMIN: filtra por estabelecimento e/ou empresa se informado
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
        return desvioRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));
    }

    @Transactional
    public DesvioResponse create(DesvioRequest request) {
        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        var localizacao = request.localizacaoId() != null
                ? localizacaoRepository.findById(request.localizacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + request.localizacaoId()))
                : null;

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var tecnico = usuarioRepository.findByEmail(email).orElse(null);

        Desvio desvio = new Desvio();
        desvio.setEstabelecimento(estabelecimento);
        desvio.setTitulo(request.titulo());
        desvio.setLocalizacao(localizacao);
        desvio.setDescricao(request.descricao());
        desvio.setDataRegistro(LocalDateTime.now());
        desvio.setTecnico(tecnico);
        desvio.setUsuarioCriacao(tecnico);
        desvio.setRegraDeOuro(false);
        desvio.setOrientacaoRealizada(request.orientacaoRealizada());
        desvio.setStatus(StatusDesvio.CONCLUIDO);

        return toResponse(desvioRepository.save(desvio));
    }

    @Transactional
    public DesvioResponse update(UUID id, DesvioRequest request) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        if (desvio.getStatus() == StatusDesvio.CONCLUIDO) {
            throw new BusinessException("Não é permitido editar um desvio concluído");
        }

        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        var localizacao = request.localizacaoId() != null
                ? localizacaoRepository.findById(request.localizacaoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + request.localizacaoId()))
                : null;

        desvio.setEstabelecimento(estabelecimento);
        desvio.setTitulo(request.titulo());
        desvio.setLocalizacao(localizacao);
        desvio.setDescricao(request.descricao());
        desvio.setOrientacaoRealizada(request.orientacaoRealizada());

        return toResponse(desvioRepository.save(desvio));
    }

    @Transactional
    public void delete(UUID id) {
        Desvio desvio = desvioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desvio não encontrado: " + id));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (desvio.getStatus() == StatusDesvio.CONCLUIDO && (usuario == null || !usuario.isAdmin())) {
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
                d.getStatus()
        );
    }
}

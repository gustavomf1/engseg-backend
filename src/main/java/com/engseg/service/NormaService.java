package com.engseg.service;

import com.engseg.dto.request.BuscarTrechoRequest;
import com.engseg.dto.request.NormaRequest;
import com.engseg.dto.response.BuscarTrechoResponse;
import com.engseg.dto.response.NormaResponse;
import com.engseg.entity.Norma;
import com.engseg.entity.Usuario;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.repository.NormaRepository;
import com.engseg.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NormaService {

    private final NormaRepository normaRepository;
    private final NaoConformidadeRepository naoConformidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityHelper securityHelper;
    private final ClaudeService claudeService;

    public List<NormaResponse> findAll(Boolean ativo) {
        List<Norma> items = (ativo != null)
                ? normaRepository.findAllByAtivo(ativo)
                : normaRepository.findAll();
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    public NormaResponse findById(UUID id) {
        return normaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
    }

    @Transactional
    public NormaResponse create(NormaRequest request) {
        Usuario usuario = securityHelper.getUsuarioLogado();
        LocalDateTime now = LocalDateTime.now();

        Norma norma = Norma.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .conteudo(request.conteudo())
                .criadoEm(now)
                .criadoPorId(usuario.getId())
                .atualizadoEm(now)
                .atualizadoPorId(usuario.getId())
                .build();
        return toResponse(normaRepository.save(norma));
    }

    @Transactional
    public NormaResponse update(UUID id, NormaRequest request) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setTitulo(request.titulo());
        norma.setDescricao(request.descricao());
        norma.setConteudo(request.conteudo());
        marcarAtualizacao(norma);
        return toResponse(normaRepository.save(norma));
    }

    @Transactional
    public NormaResponse salvarConteudo(UUID id, String conteudo) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setConteudo(conteudo);
        marcarAtualizacao(norma);
        return toResponse(normaRepository.save(norma));
    }

    public BuscarTrechoResponse buscarTrecho(UUID id, BuscarTrechoRequest request) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));

        if (norma.getConteudo() == null || norma.getConteudo().isBlank()) {
            throw new BusinessException("Esta norma não possui conteúdo cadastrado. Adicione o texto completo da NR antes de buscar trechos.");
        }

        String trecho = claudeService.buscarTrecho(norma.getConteudo(), request.prompt());

        // Remove caracteres de controle (< 0x20) que quebram a serialização JSON,
        // preservando apenas \t (0x09), \n (0x0A) e \r (0x0D)
        String trechoSanitizado = trecho.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");

        log.info("[NormaService] buscarTrecho → normaId={} trechoLen={} sanitizadoLen={} preview={}",
                id,
                trecho.length(),
                trechoSanitizado.length(),
                trechoSanitizado.substring(0, Math.min(120, trechoSanitizado.length())).replace("\n", "↵"));

        return new BuscarTrechoResponse(trechoSanitizado, null);
    }

    @Transactional
    public void delete(UUID id) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setAtivo(false);
        norma.setDtInativacao(LocalDate.now());
        marcarAtualizacao(norma);
        normaRepository.save(norma);
    }

    @Transactional
    public NormaResponse reativar(UUID id) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setAtivo(true);
        norma.setDtInativacao(null);
        marcarAtualizacao(norma);
        return toResponse(normaRepository.save(norma));
    }

    public NormaResponse toResponse(Norma n) {
        UUID normaId = n.getId();
        long totalOcorrencias = normaId == null ? 0 : naoConformidadeRepository.countByNormaId(normaId);
        long totalNcsAtivas = normaId == null ? 0 : naoConformidadeRepository.countAtivasByNormaId(normaId);

        return new NormaResponse(
                n.getId(),
                n.getTitulo(),
                n.getDescricao(),
                n.getConteudo(),
                n.isAtivo(),
                n.getDtInativacao(),
                n.getCriadoEm(),
                resolveUsuarioNome(n.getCriadoPorId()),
                n.getAtualizadoEm(),
                resolveUsuarioNome(n.getAtualizadoPorId()),
                totalOcorrencias,
                totalNcsAtivas
        );
    }

    private void marcarAtualizacao(Norma norma) {
        Usuario usuario = securityHelper.getUsuarioLogado();
        norma.setAtualizadoEm(LocalDateTime.now());
        norma.setAtualizadoPorId(usuario.getId());
    }

    private String resolveUsuarioNome(UUID usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        return usuarioRepository.findById(usuarioId)
                .map(Usuario::getNome)
                .orElse(null);
    }
}

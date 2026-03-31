package com.engseg.service;

import com.engseg.dto.request.BuscarTrechoRequest;
import com.engseg.dto.request.NormaRequest;
import com.engseg.dto.response.BuscarTrechoResponse;
import com.engseg.dto.response.NormaResponse;
import com.engseg.entity.Norma;
import com.engseg.exception.BusinessException;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.NormaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NormaService {

    private final NormaRepository normaRepository;
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
        Norma norma = Norma.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .conteudo(request.conteudo())
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
        return toResponse(normaRepository.save(norma));
    }

    @Transactional
    public NormaResponse salvarConteudo(UUID id, String conteudo) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setConteudo(conteudo);
        return toResponse(normaRepository.save(norma));
    }

    public BuscarTrechoResponse buscarTrecho(UUID id, BuscarTrechoRequest request) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));

        if (norma.getConteudo() == null || norma.getConteudo().isBlank()) {
            throw new BusinessException("Esta norma não possui conteúdo cadastrado. Adicione o texto completo da NR antes de buscar trechos.");
        }

        String trecho = claudeService.buscarTrecho(norma.getConteudo(), request.prompt());
        return new BuscarTrechoResponse(trecho, null);
    }

    @Transactional
    public void delete(UUID id) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setAtivo(false);
        norma.setDtInativacao(LocalDate.now());
        normaRepository.save(norma);
    }

    @Transactional
    public NormaResponse reativar(UUID id) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setAtivo(true);
        norma.setDtInativacao(null);
        return toResponse(normaRepository.save(norma));
    }

    public NormaResponse toResponse(Norma n) {
        return new NormaResponse(n.getId(), n.getTitulo(), n.getDescricao(), n.getConteudo(), n.isAtivo(), n.getDtInativacao());
    }
}

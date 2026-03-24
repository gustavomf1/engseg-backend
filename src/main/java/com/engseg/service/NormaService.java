package com.engseg.service;

import com.engseg.dto.request.NormaRequest;
import com.engseg.dto.response.NormaResponse;
import com.engseg.entity.Norma;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.NormaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NormaService {

    private final NormaRepository normaRepository;

    public List<NormaResponse> findAll() {
        return normaRepository.findAllByAtivo(true).stream()
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
                .build();
        return toResponse(normaRepository.save(norma));
    }

    @Transactional
    public NormaResponse update(UUID id, NormaRequest request) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setTitulo(request.titulo());
        norma.setDescricao(request.descricao());
        return toResponse(normaRepository.save(norma));
    }

    @Transactional
    public void delete(UUID id) {
        Norma norma = normaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + id));
        norma.setAtivo(false);
        normaRepository.save(norma);
    }

    public NormaResponse toResponse(Norma n) {
        return new NormaResponse(n.getId(), n.getTitulo(), n.getDescricao(), n.isAtivo());
    }
}

package com.safecore.service;

import com.safecore.dto.request.NcTrechoNormaRequest;
import com.safecore.dto.response.NcTrechoNormaResponse;
import com.safecore.entity.NcTrechoNorma;
import com.safecore.exception.ResourceNotFoundException;
import com.safecore.repository.NaoConformidadeRepository;
import com.safecore.repository.NcTrechoNormaRepository;
import com.safecore.repository.NormaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NcTrechoNormaService {

    private final NcTrechoNormaRepository ncTrechoNormaRepository;
    private final NaoConformidadeRepository naoConformidadeRepository;
    private final NormaRepository normaRepository;

    public List<NcTrechoNormaResponse> findByNc(UUID ncId) {
        return ncTrechoNormaRepository.findByNaoConformidadeIdOrderByDataVinculoAsc(ncId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NcTrechoNormaResponse vincular(UUID ncId, NcTrechoNormaRequest request) {
        var nc = naoConformidadeRepository.findById(ncId)
                .orElseThrow(() -> new ResourceNotFoundException("NC não encontrada: " + ncId));
        var norma = normaRepository.findById(request.normaId())
                .orElseThrow(() -> new ResourceNotFoundException("Norma não encontrada: " + request.normaId()));

        NcTrechoNorma trecho = NcTrechoNorma.builder()
                .naoConformidade(nc)
                .norma(norma)
                .clausulaReferencia(request.clausulaReferencia())
                .textoEditado(request.textoEditado())
                .build();

        return toResponse(ncTrechoNormaRepository.save(trecho));
    }

    @Transactional
    public void deletar(UUID id) {
        if (!ncTrechoNormaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Trecho não encontrado: " + id);
        }
        ncTrechoNormaRepository.deleteById(id);
    }

    private NcTrechoNormaResponse toResponse(NcTrechoNorma t) {
        return new NcTrechoNormaResponse(
                t.getId(),
                t.getNorma().getId(),
                t.getNorma().getTitulo(),
                t.getClausulaReferencia(),
                t.getTextoEditado(),
                t.getDataVinculo()
        );
    }
}

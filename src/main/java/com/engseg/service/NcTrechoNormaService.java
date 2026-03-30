package com.engseg.service;

import com.engseg.dto.request.NcTrechoNormaRequest;
import com.engseg.dto.response.NcTrechoNormaResponse;
import com.engseg.entity.NcTrechoNorma;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.NaoConformidadeRepository;
import com.engseg.repository.NcTrechoNormaRepository;
import com.engseg.repository.NormaRepository;
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

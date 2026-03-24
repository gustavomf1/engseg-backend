package com.engseg.service;

import com.engseg.dto.request.LocalizacaoRequest;
import com.engseg.dto.response.LocalizacaoResponse;
import com.engseg.entity.Localizacao;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.EstabelecimentoRepository;
import com.engseg.repository.LocalizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalizacaoService {

    private final LocalizacaoRepository localizacaoRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    public List<LocalizacaoResponse> findAll() {
        return localizacaoRepository.findAllByAtivo(true).stream()
                .map(this::toResponse)
                .toList();
    }

    public LocalizacaoResponse findById(UUID id) {
        return localizacaoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + id));
    }

    public List<LocalizacaoResponse> findByEstabelecimentoId(UUID estabelecimentoId) {
        return localizacaoRepository.findByEstabelecimentoId(estabelecimentoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LocalizacaoResponse create(LocalizacaoRequest request) {
        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        Localizacao localizacao = Localizacao.builder()
                .nome(request.nome())
                .estabelecimento(estabelecimento)
                .ativo(true)
                .build();

        return toResponse(localizacaoRepository.save(localizacao));
    }

    @Transactional
    public LocalizacaoResponse update(UUID id, LocalizacaoRequest request) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + id));

        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + request.estabelecimentoId()));

        localizacao.setNome(request.nome());
        localizacao.setEstabelecimento(estabelecimento);

        return toResponse(localizacaoRepository.save(localizacao));
    }

    @Transactional
    public void delete(UUID id) {
        Localizacao localizacao = localizacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Localização não encontrada: " + id));
        localizacao.setAtivo(false);
        localizacaoRepository.save(localizacao);
    }

    private LocalizacaoResponse toResponse(Localizacao l) {
        return new LocalizacaoResponse(
                l.getId(),
                l.getNome(),
                l.getEstabelecimento().getId(),
                l.getEstabelecimento().getNome(),
                l.isAtivo()
        );
    }
}

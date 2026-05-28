package com.engseg.service;

import com.engseg.dto.request.EmailPadraoRequest;
import com.engseg.dto.response.EmailPadraoEscopoResponse;
import com.engseg.dto.response.EmailPadraoResponse;
import com.engseg.entity.EmailPadrao;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.EstabelecimentoEmpresaRepository;
import com.engseg.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailPadraoService {

    private final EmailPadraoRepository repository;
    private final EstabelecimentoEmpresaRepository estabelecimentoEmpresaRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public List<EmailPadraoEscopoResponse> listarEscopos() {
        Map<String, Long> emailCounts = repository.findAllFetched().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEstabelecimento().getId() + "|" + e.getEmpresa().getId(),
                        Collectors.counting()));

        return estabelecimentoEmpresaRepository.findAllAtivoFetched().stream()
                .map(ee -> {
                    String chave = ee.getEstabelecimento().getId() + "|" + ee.getEmpresa().getId();
                    String nomeEmpresa = ee.getEmpresa().getNomeFantasia() != null
                            ? ee.getEmpresa().getNomeFantasia()
                            : ee.getEmpresa().getRazaoSocial();
                    return new EmailPadraoEscopoResponse(
                            ee.getEstabelecimento().getId(),
                            ee.getEstabelecimento().getNome(),
                            ee.getEmpresa().getId(),
                            nomeEmpresa,
                            emailCounts.getOrDefault(chave, 0L).intValue());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmailPadraoResponse> listar(UUID estabelecimentoId, UUID empresaId) {
        return repository.findByEstabelecimentoIdAndEmpresaId(estabelecimentoId, empresaId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public EmailPadraoResponse criar(EmailPadraoRequest request) {
        var estabelecimento = estabelecimentoRepository.findById(request.estabelecimentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado"));
        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        EmailPadrao entity = EmailPadrao.builder()
                .estabelecimento(estabelecimento)
                .empresa(empresa)
                .email(request.email())
                .descricao(request.descricao())
                .build();
        return toResponse(repository.save(entity));
    }

    @Transactional
    public EmailPadraoResponse atualizarDescricao(UUID id, String descricao) {
        EmailPadrao entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email padrão não encontrado: " + id));
        entity.setDescricao(descricao);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void remover(UUID id) {
        EmailPadrao entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email padrão não encontrado: " + id));
        repository.delete(entity);
    }

    private EmailPadraoResponse toResponse(EmailPadrao e) {
        String nomeEmpresa = e.getEmpresa().getNomeFantasia() != null
                ? e.getEmpresa().getNomeFantasia()
                : e.getEmpresa().getRazaoSocial();
        return new EmailPadraoResponse(
                e.getId(),
                e.getEstabelecimento().getId(),
                e.getEstabelecimento().getNome(),
                e.getEmpresa().getId(),
                nomeEmpresa,
                e.getEmail(),
                e.getDescricao()
        );
    }
}

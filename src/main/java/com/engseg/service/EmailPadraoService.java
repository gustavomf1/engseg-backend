package com.engseg.service;

import com.engseg.dto.request.EmailPadraoRequest;
import com.engseg.dto.response.EmailPadraoResponse;
import com.engseg.entity.EmailPadrao;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.EmailPadraoRepository;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailPadraoService {

    private final EmailPadraoRepository repository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EmpresaRepository empresaRepository;

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

package com.safecore.service;

import com.safecore.dto.response.EmpresaResponse;
import com.safecore.dto.response.EstabelecimentoEmpresaResponse;
import com.safecore.entity.Empresa;
import com.safecore.entity.Estabelecimento;
import com.safecore.entity.EstabelecimentoEmpresa;
import com.safecore.exception.ResourceNotFoundException;
import com.safecore.repository.EmpresaRepository;
import com.safecore.repository.EstabelecimentoEmpresaRepository;
import com.safecore.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstabelecimentoEmpresaService {

    private final EstabelecimentoEmpresaRepository repository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EmpresaRepository empresaRepository;

    public List<EmpresaResponse> findEmpresasByEstabelecimento(UUID estabelecimentoId) {
        return repository.findByEstabelecimentoIdAndAtivo(estabelecimentoId, true).stream()
                .map(ee -> {
                    Empresa e = ee.getEmpresa();
                    return new EmpresaResponse(
                            e.getId(),
                            e.getRazaoSocial(),
                            e.getCnpj(),
                            e.getNomeFantasia(),
                            e.getEmail(),
                            e.getTelefone(),
                            e.getEmpresaMae() != null ? e.getEmpresaMae().getId() : null,
                            e.getEmpresaMae() != null ? e.getEmpresaMae().getRazaoSocial() : null,
                            e.isAtivo(),
                            e.isExibirNoSeletor(),
                            e.getDtInativacao()
                    );
                })
                .toList();
    }

    @Transactional
    public EstabelecimentoEmpresaResponse vincular(UUID estabelecimentoId, UUID empresaId) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + estabelecimentoId));
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + empresaId));

        var existente = repository.findByEstabelecimentoIdAndEmpresaId(estabelecimentoId, empresaId);
        if (existente.isPresent()) {
            EstabelecimentoEmpresa ee = existente.get();
            ee.setAtivo(true);
            return toResponse(repository.save(ee));
        }

        EstabelecimentoEmpresa ee = EstabelecimentoEmpresa.builder()
                .estabelecimento(estabelecimento)
                .empresa(empresa)
                .ativo(true)
                .build();
        return toResponse(repository.save(ee));
    }

    @Transactional
    public void desvincular(UUID estabelecimentoId, UUID empresaId) {
        EstabelecimentoEmpresa ee = repository.findByEstabelecimentoIdAndEmpresaId(estabelecimentoId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado"));
        ee.setAtivo(false);
        repository.save(ee);
    }

    private EstabelecimentoEmpresaResponse toResponse(EstabelecimentoEmpresa ee) {
        return new EstabelecimentoEmpresaResponse(
                ee.getId(),
                ee.getEstabelecimento().getId(),
                ee.getEstabelecimento().getNome(),
                ee.getEmpresa().getId(),
                ee.getEmpresa().getRazaoSocial(),
                ee.isAtivo()
        );
    }
}

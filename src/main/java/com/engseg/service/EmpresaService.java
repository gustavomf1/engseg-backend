package com.engseg.service;

import com.engseg.dto.request.EmpresaRequest;
import com.engseg.dto.response.EmpresaResponse;
import com.engseg.entity.Empresa;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public List<EmpresaResponse> findAll(Boolean ativo) {
        List<Empresa> empresas = (ativo != null)
                ? empresaRepository.findAllByAtivo(ativo)
                : empresaRepository.findAll();
        return empresas.stream()
                .map(this::toResponse)
                .toList();
    }

    public EmpresaResponse findById(UUID id) {
        return empresaRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
    }

    @Transactional
    public EmpresaResponse create(EmpresaRequest request) {
        Empresa empresa = Empresa.builder()
                .razaoSocial(request.razaoSocial())
                .cnpj(request.cnpj())
                .nomeFantasia(request.nomeFantasia())
                .email(request.email())
                .telefone(request.telefone())
                .ativo(true)
                .build();
        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public EmpresaResponse update(UUID id, EmpresaRequest request) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));

        empresa.setRazaoSocial(request.razaoSocial());
        empresa.setCnpj(request.cnpj());
        empresa.setNomeFantasia(request.nomeFantasia());
        empresa.setEmail(request.email());
        empresa.setTelefone(request.telefone());

        return toResponse(empresaRepository.save(empresa));
    }

    @Transactional
    public void delete(UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + id));
        empresa.setAtivo(false);
        empresaRepository.save(empresa);
    }

    private EmpresaResponse toResponse(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getRazaoSocial(),
                empresa.getCnpj(),
                empresa.getNomeFantasia(),
                empresa.getEmail(),
                empresa.getTelefone(),
                empresa.isAtivo()
        );
    }
}

package com.engseg.service;

import com.engseg.dto.request.EstabelecimentoRequest;
import com.engseg.dto.response.EstabelecimentoResponse;
import com.engseg.entity.Estabelecimento;
import com.engseg.exception.ResourceNotFoundException;
import com.engseg.repository.EmpresaRepository;
import com.engseg.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstabelecimentoService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final EmpresaRepository empresaRepository;

    public List<EstabelecimentoResponse> findAll(Boolean ativo) {
        List<Estabelecimento> items = (ativo != null)
                ? estabelecimentoRepository.findAllByAtivo(ativo)
                : estabelecimentoRepository.findAll();
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    public EstabelecimentoResponse findById(UUID id) {
        return estabelecimentoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + id));
    }

    @Transactional
    public EstabelecimentoResponse create(EstabelecimentoRequest request) {
        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + request.empresaId()));

        Estabelecimento estabelecimento = Estabelecimento.builder()
                .nome(request.nome())
                .codigo(request.codigo())
                .empresa(empresa)
                .cep(request.cep())
                .logradouro(request.logradouro())
                .numero(request.numero())
                .bairro(request.bairro())
                .cidade(request.cidade())
                .estado(request.estado())
                .ativo(true)
                .build();

        return toResponse(estabelecimentoRepository.save(estabelecimento));
    }

    @Transactional
    public EstabelecimentoResponse update(UUID id, EstabelecimentoRequest request) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + id));

        var empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada: " + request.empresaId()));

        estabelecimento.setNome(request.nome());
        estabelecimento.setCodigo(request.codigo());
        estabelecimento.setEmpresa(empresa);
        estabelecimento.setCep(request.cep());
        estabelecimento.setLogradouro(request.logradouro());
        estabelecimento.setNumero(request.numero());
        estabelecimento.setBairro(request.bairro());
        estabelecimento.setCidade(request.cidade());
        estabelecimento.setEstado(request.estado());

        return toResponse(estabelecimentoRepository.save(estabelecimento));
    }

    @Transactional
    public void delete(UUID id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estabelecimento não encontrado: " + id));
        estabelecimento.setAtivo(false);
        estabelecimentoRepository.save(estabelecimento);
    }

    private EstabelecimentoResponse toResponse(Estabelecimento e) {
        return new EstabelecimentoResponse(
                e.getId(),
                e.getNome(),
                e.getCodigo(),
                e.getEmpresa().getId(),
                e.getEmpresa().getRazaoSocial(),
                e.getCep(),
                e.getLogradouro(),
                e.getNumero(),
                e.getBairro(),
                e.getCidade(),
                e.getEstado(),
                e.isAtivo()
        );
    }
}

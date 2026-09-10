package com.safecore.controller;

import com.safecore.dto.request.EstabelecimentoEmpresaRequest;
import com.safecore.dto.request.EstabelecimentoRequest;
import com.safecore.dto.response.EmpresaResponse;
import com.safecore.dto.response.EstabelecimentoEmpresaResponse;
import com.safecore.dto.response.EstabelecimentoResponse;
import com.safecore.service.EstabelecimentoEmpresaService;
import com.safecore.service.EstabelecimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estabelecimentos")
@RequiredArgsConstructor
public class EstabelecimentoController {

    private final EstabelecimentoService estabelecimentoService;
    private final EstabelecimentoEmpresaService estabelecimentoEmpresaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO', 'EXTERNO')")
    public ResponseEntity<List<EstabelecimentoResponse>> getAll(
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) UUID empresaId) {
        return ResponseEntity.ok(estabelecimentoService.findAll(ativo, empresaId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO', 'EXTERNO')")
    public ResponseEntity<EstabelecimentoResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(estabelecimentoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstabelecimentoResponse> create(@Valid @RequestBody EstabelecimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estabelecimentoService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstabelecimentoResponse> update(@PathVariable UUID id, @Valid @RequestBody EstabelecimentoRequest request) {
        return ResponseEntity.ok(estabelecimentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        estabelecimentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reativar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstabelecimentoResponse> reativar(@PathVariable UUID id) {
        return ResponseEntity.ok(estabelecimentoService.reativar(id));
    }

    @GetMapping("/{id}/empresas")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGENHEIRO', 'TECNICO', 'EXTERNO')")
    public ResponseEntity<List<EmpresaResponse>> getEmpresas(@PathVariable UUID id) {
        return ResponseEntity.ok(estabelecimentoEmpresaService.findEmpresasByEstabelecimento(id));
    }

    @PostMapping("/{id}/empresas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EstabelecimentoEmpresaResponse> vincularEmpresa(
            @PathVariable UUID id,
            @Valid @RequestBody EstabelecimentoEmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estabelecimentoEmpresaService.vincular(id, request.empresaId()));
    }

    @DeleteMapping("/{estId}/empresas/{empId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desvincularEmpresa(
            @PathVariable UUID estId,
            @PathVariable UUID empId) {
        estabelecimentoEmpresaService.desvincular(estId, empId);
        return ResponseEntity.noContent().build();
    }
}

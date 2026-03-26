package com.engseg.controller;

import com.engseg.dto.request.EstabelecimentoEmpresaRequest;
import com.engseg.dto.request.EstabelecimentoRequest;
import com.engseg.dto.response.EmpresaResponse;
import com.engseg.dto.response.EstabelecimentoEmpresaResponse;
import com.engseg.dto.response.EstabelecimentoResponse;
import com.engseg.service.EstabelecimentoEmpresaService;
import com.engseg.service.EstabelecimentoService;
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
    public ResponseEntity<List<EstabelecimentoResponse>> getAll(
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(estabelecimentoService.findAll(ativo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(estabelecimentoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<EstabelecimentoResponse> create(@Valid @RequestBody EstabelecimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estabelecimentoService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<EstabelecimentoResponse> update(@PathVariable UUID id, @Valid @RequestBody EstabelecimentoRequest request) {
        return ResponseEntity.ok(estabelecimentoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        estabelecimentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -- Empresas vinculadas ao estabelecimento --

    @GetMapping("/{id}/empresas")
    public ResponseEntity<List<EmpresaResponse>> getEmpresas(@PathVariable UUID id) {
        return ResponseEntity.ok(estabelecimentoEmpresaService.findEmpresasByEstabelecimento(id));
    }

    @PostMapping("/{id}/empresas")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<EstabelecimentoEmpresaResponse> vincularEmpresa(
            @PathVariable UUID id,
            @Valid @RequestBody EstabelecimentoEmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estabelecimentoEmpresaService.vincular(id, request.empresaId()));
    }

    @DeleteMapping("/{estId}/empresas/{empId}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<Void> desvincularEmpresa(
            @PathVariable UUID estId,
            @PathVariable UUID empId) {
        estabelecimentoEmpresaService.desvincular(estId, empId);
        return ResponseEntity.noContent().build();
    }
}

package com.engseg.controller;

import com.engseg.dto.request.EmpresaRequest;
import com.engseg.dto.response.EmpresaResponse;
import com.engseg.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<List<EmpresaResponse>> getAll(
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(empresaService.findAll(ativo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(empresaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<EmpresaResponse> create(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<EmpresaResponse> update(@PathVariable UUID id, @Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.ok(empresaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        empresaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

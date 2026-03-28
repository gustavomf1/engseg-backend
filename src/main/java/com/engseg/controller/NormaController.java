package com.engseg.controller;

import com.engseg.dto.request.NormaRequest;
import com.engseg.dto.response.NormaResponse;
import com.engseg.service.NormaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/normas")
@RequiredArgsConstructor
public class NormaController {

    private final NormaService normaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<List<NormaResponse>> getAll(
            @RequestParam(required = false) Boolean ativo) {
        return ResponseEntity.ok(normaService.findAll(ativo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ENGENHEIRO')")
    public ResponseEntity<NormaResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(normaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NormaResponse> create(@Valid @RequestBody NormaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(normaService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<NormaResponse> update(@PathVariable UUID id, @Valid @RequestBody NormaRequest request) {
        return ResponseEntity.ok(normaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENGENHEIRO')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        normaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

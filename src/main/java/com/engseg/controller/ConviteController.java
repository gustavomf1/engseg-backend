package com.engseg.controller;

import com.engseg.dto.request.ConviteRequest;
import com.engseg.dto.request.RegistrarViaConviteRequest;
import com.engseg.dto.response.ConviteResponse;
import com.engseg.service.ConviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/convites")
@RequiredArgsConstructor
public class ConviteController {

    private final ConviteService conviteService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConviteResponse> criar(@Valid @RequestBody ConviteRequest request) {
        return ResponseEntity.ok(conviteService.criar(request));
    }

    @GetMapping("/{token}")
    public ResponseEntity<ConviteResponse> buscar(@PathVariable UUID token) {
        return ResponseEntity.ok(conviteService.buscar(token));
    }

    @PostMapping("/{token}/registrar")
    public ResponseEntity<Void> registrar(
            @PathVariable UUID token,
            @Valid @RequestBody RegistrarViaConviteRequest request
    ) {
        conviteService.registrar(token, request);
        return ResponseEntity.ok().build();
    }
}

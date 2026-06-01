package com.engseg.controller;

import com.engseg.dto.request.RedefinirSenhaRequest;
import com.engseg.dto.request.SolicitarResetRequest;
import com.engseg.dto.request.VerificarOtpRequest;
import com.engseg.dto.response.VerificarOtpResponse;
import com.engseg.service.SenhaResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/reset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final SenhaResetService senhaResetService;

    @PostMapping("/solicitar")
    public ResponseEntity<Map<String, String>> solicitar(
            @Valid @RequestBody SolicitarResetRequest request) {
        senhaResetService.solicitarReset(request.email());
        return ResponseEntity.ok(Map.of("mensagem", "Se o e-mail existir, um código foi enviado."));
    }

    @PostMapping("/verificar")
    public ResponseEntity<VerificarOtpResponse> verificar(
            @Valid @RequestBody VerificarOtpRequest request) {
        var resetToken = senhaResetService.verificarOtp(request.email(), request.otp());
        return ResponseEntity.ok(new VerificarOtpResponse(resetToken));
    }

    @PostMapping("/redefinir")
    public ResponseEntity<Void> redefinir(
            @Valid @RequestBody RedefinirSenhaRequest request) {
        senhaResetService.redefinirSenha(request.resetToken(), request.novaSenha());
        return ResponseEntity.ok().build();
    }
}

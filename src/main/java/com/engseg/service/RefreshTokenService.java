package com.engseg.service;

import com.engseg.entity.RefreshToken;
import com.engseg.entity.Usuario;
import com.engseg.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /** Resultado da rotação: o usuário dono e o novo refresh token em claro. */
    public record Rotacao(Usuario usuario, String refreshTokenPlano) {}

    /** Cria um novo refresh token para o usuário e devolve o valor em claro (não persistido). */
    @Transactional
    public String emitir(Usuario usuario) {
        String plano = gerarTokenAleatorio();
        repository.save(RefreshToken.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .tokenHash(hash(plano))
                .expiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build());
        return plano;
    }

    /** Valida o refresh token, revoga-o (rotação) e emite um novo. Lança 401 se inválido. */
    @Transactional
    public Rotacao rotacionar(String refreshTokenPlano) {
        RefreshToken atual = repository.findByTokenHash(hash(refreshTokenPlano))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido."));

        if (atual.isRevoked() || atual.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado ou revogado.");
        }

        atual.setRevoked(true);
        repository.save(atual);

        Usuario usuario = atual.getUsuario();
        String novo = emitir(usuario);
        return new Rotacao(usuario, novo);
    }

    /** Revoga o refresh token informado (logout). Silencioso se não existir. */
    @Transactional
    public void revogar(String refreshTokenPlano) {
        if (refreshTokenPlano == null || refreshTokenPlano.isBlank()) return;
        repository.findByTokenHash(hash(refreshTokenPlano)).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }

    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String valor) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}

package com.safecore.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_TENTATIVAS = 5;
    private static final long BLOCK_MINUTES = 15;

    private record Tentativa(int falhas, Instant bloqueadoAte) {}

    private final ConcurrentHashMap<String, Tentativa> cache = new ConcurrentHashMap<>();

    private String key(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    public boolean isBlocked(String email) {
        Tentativa t = cache.get(key(email));
        return t != null && t.bloqueadoAte() != null && Instant.now().isBefore(t.bloqueadoAte());
    }

    public void loginFailed(String email) {
        cache.compute(key(email), (k, t) -> {
            int falhas = (t == null ? 0 : t.falhas()) + 1;
            Instant bloqueio = falhas >= MAX_TENTATIVAS
                    ? Instant.now().plusSeconds(BLOCK_MINUTES * 60)
                    : null;
            return new Tentativa(falhas, bloqueio);
        });
    }

    public void loginSucceeded(String email) {
        cache.remove(key(email));
    }
}

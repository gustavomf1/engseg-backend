package com.engseg.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limit por IP (janela fixa) nos endpoints de autenticação públicos
 * (/api/auth/login e /api/auth/reset/*). Mitiga força-bruta de senha/OTP (A2).
 * IP real lido de X-Forwarded-For (atrás de proxy/Render/nginx).
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private record Janela(Instant inicio, AtomicInteger contador) {}

    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();

    private boolean rateLimited(String path) {
        return path.equals("/api/auth/login") || path.startsWith("/api/auth/reset/");
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!rateLimited(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + "|" + request.getRequestURI();
        Instant now = Instant.now();
        Janela janela = janelas.compute(key, (k, atual) -> {
            if (atual == null || atual.inicio().plusSeconds(WINDOW_SECONDS).isBefore(now)) {
                return new Janela(now, new AtomicInteger(0));
            }
            return atual;
        });

        if (janela.contador().incrementAndGet() > MAX_REQUESTS) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Muitas requisicoes. Aguarde e tente novamente.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

package com.safecore.service;

import com.safecore.dto.request.LoginRequest;
import com.safecore.dto.response.LoginResponse;
import com.safecore.dto.response.RefreshResponse;
import com.safecore.entity.Usuario;
import com.safecore.repository.UsuarioRepository;
import com.safecore.security.JwtService;
import com.safecore.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.email())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas de login. Tente novamente em alguns minutos.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha())
            );
        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(request.email());
            throw e;
        }
        loginAttemptService.loginSucceeded(request.email());

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getPerfil().name(), usuario.getId());
        String refreshToken = refreshTokenService.emitir(usuario);

        return new LoginResponse(
                usuario.getId(),
                token,
                refreshToken,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAdmin()
        );
    }

    public RefreshResponse refresh(String refreshToken) {
        var rotacao = refreshTokenService.rotacionar(refreshToken);
        Usuario usuario = rotacao.usuario();
        String access = jwtService.generateToken(usuario.getEmail(), usuario.getPerfil().name(), usuario.getId());
        return new RefreshResponse(access, rotacao.refreshTokenPlano());
    }

    public void logout(String refreshToken) {
        refreshTokenService.revogar(refreshToken);
    }
}

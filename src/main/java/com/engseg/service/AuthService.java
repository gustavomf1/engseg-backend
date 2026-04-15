package com.engseg.service;

import com.engseg.dto.request.LoginRequest;
import com.engseg.dto.response.LoginResponse;
import com.engseg.repository.UsuarioRepository;
import com.engseg.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        var usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getPerfil().name());

        return new LoginResponse(
                usuario.getId(),
                token,
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAdmin()
        );
    }
}

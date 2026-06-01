package com.engseg.service;

import com.engseg.entity.SenhaResetToken;
import com.engseg.exception.BusinessException;
import com.engseg.repository.SenhaResetTokenRepository;
import com.engseg.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SenhaResetService {

    private final SenhaResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailSender emailSender;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void solicitarReset(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            invalidarTokensAntigos(usuario.getId());
            String otp = gerarOtp();
            tokenRepository.save(SenhaResetToken.builder()
                    .id(UUID.randomUUID())
                    .usuario(usuario)
                    .otp(otp)
                    .otpExpiresAt(LocalDateTime.now().plusMinutes(15))
                    .createdAt(LocalDateTime.now())
                    .build());
            emailSender.enviar(email, otp);
        });
    }

    @Transactional
    public UUID verificarOtp(String email, String otp) {
        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Código inválido ou expirado."));

        var token = tokenRepository
                .findByUsuarioIdAndOtpAndUsadoFalseAndOtpExpiresAtAfter(
                        usuario.getId(), otp, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("Código inválido ou expirado."));

        UUID resetToken = UUID.randomUUID();
        token.setResetToken(resetToken);
        token.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(token);
        return resetToken;
    }

    @Transactional
    public void redefinirSenha(UUID resetToken, String novaSenha) {
        var token = tokenRepository
                .findByResetTokenAndUsadoFalseAndResetTokenExpiresAtAfter(resetToken, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException("Link de redefinição inválido ou expirado."));

        validarForcaSenha(novaSenha);

        token.getUsuario().setSenha(passwordEncoder.encode(novaSenha));
        token.setUsado(true);
        tokenRepository.save(token);
    }

    private void invalidarTokensAntigos(UUID usuarioId) {
        List<SenhaResetToken> ativos = tokenRepository.findByUsuarioIdAndUsadoFalse(usuarioId);
        ativos.forEach(t -> t.setUsado(true));
        tokenRepository.saveAll(ativos);
    }

    private String gerarOtp() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private void validarForcaSenha(String senha) {
        if (senha.length() < 8
                || !senha.matches(".*[A-Z].*")
                || !senha.matches(".*[0-9].*")
                || !senha.matches(".*[^A-Za-z0-9].*")) {
            throw new BusinessException("A senha deve ter mín. 8 caracteres, 1 maiúscula, 1 número e 1 símbolo.");
        }
    }
}

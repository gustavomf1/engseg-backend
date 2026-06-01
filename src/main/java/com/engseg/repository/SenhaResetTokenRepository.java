package com.engseg.repository;

import com.engseg.entity.SenhaResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SenhaResetTokenRepository extends JpaRepository<SenhaResetToken, UUID> {

    List<SenhaResetToken> findByUsuarioIdAndUsadoFalse(UUID usuarioId);

    Optional<SenhaResetToken> findByUsuarioIdAndOtpAndUsadoFalseAndOtpExpiresAtAfter(
            UUID usuarioId, String otp, LocalDateTime agora);

    Optional<SenhaResetToken> findByResetTokenAndUsadoFalseAndResetTokenExpiresAtAfter(
            UUID resetToken, LocalDateTime agora);
}

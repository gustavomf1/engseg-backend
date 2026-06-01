package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "senha_reset_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenhaResetToken {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 6)
    private String otp;

    @Column(name = "otp_expires_at", nullable = false)
    private LocalDateTime otpExpiresAt;

    @Column(name = "reset_token")
    private UUID resetToken;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Convert(converter = BooleanToSNConverter.class)
    @Column(nullable = false, length = 1)
    @Builder.Default
    private boolean usado = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "convite_usuario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConviteUsuario {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Convert(converter = BooleanToSNConverter.class)
    @Column(nullable = false, length = 1)
    @Builder.Default
    private boolean usado = false;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
}

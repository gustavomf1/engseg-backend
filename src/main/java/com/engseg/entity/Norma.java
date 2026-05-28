package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "norma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Norma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Convert(converter = BooleanToSNConverter.class)
    @Column(nullable = false, length = 1)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "dt_inativacao")
    private LocalDate dtInativacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "criado_por_id", updatable = false)
    private UUID criadoPorId;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "atualizado_por_id")
    private UUID atualizadoPorId;
}

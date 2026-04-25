package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tratativa_desvio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrativaDesvio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desvio_id", nullable = false)
    private Desvio desvio;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descricao;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tratativa_desvio_evidencia",
        joinColumns = @JoinColumn(name = "tratativa_desvio_id"),
        inverseJoinColumns = @JoinColumn(name = "evidencia_id")
    )
    @Builder.Default
    private List<Evidencia> evidencias = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusTratativaDesvio status = StatusTratativaDesvio.PENDENTE;

    @Column(name = "motivo_reprovacao", columnDefinition = "TEXT")
    private String motivoReprovacao;

    @Column(nullable = false)
    private Integer numero;

    @Column
    private Integer rodada;

    @Column(name = "dt_criacao", nullable = false)
    private LocalDateTime dtCriacao;
}

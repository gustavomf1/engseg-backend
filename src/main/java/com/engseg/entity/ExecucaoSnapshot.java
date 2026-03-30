package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "execucao_snapshot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecucaoSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @Column(name = "descricao_execucao", columnDefinition = "TEXT", nullable = false)
    private String descricaoExecucao;

    @Column(name = "data_submissao", nullable = false)
    private LocalDateTime dataSubmissao;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDENTE";

    @Column(name = "comentario_revisao", columnDefinition = "TEXT")
    private String comentarioRevisao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "execucao_snapshot_evidencia",
            joinColumns = @JoinColumn(name = "execucao_snapshot_id"),
            inverseJoinColumns = @JoinColumn(name = "evidencia_id")
    )
    @Builder.Default
    private List<Evidencia> evidencias = new ArrayList<>();
}

package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "atividade_plano_acao")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadePlanoAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false)
    private String status = "PENDENTE";

    @Column(columnDefinition = "TEXT")
    private String motivoRejeicao;

    @Column(name = "descricao_execucao", columnDefinition = "TEXT")
    private String descricaoExecucao;

    @Column(name = "status_execucao")
    private String statusExecucao;

    @Column(name = "motivo_rejeicao_execucao", columnDefinition = "TEXT")
    private String motivoRejeicaoExecucao;
}

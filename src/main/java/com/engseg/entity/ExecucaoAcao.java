package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "execucao_acao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecucaoAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @Column(name = "descricao_acao_executada", nullable = false)
    private String descricaoAcaoExecutada;

    @Column(name = "data_execucao", nullable = false)
    private LocalDateTime dataExecucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engenheiro_id")
    private Usuario engenheiro;
}

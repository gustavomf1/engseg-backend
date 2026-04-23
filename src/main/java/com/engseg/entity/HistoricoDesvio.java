package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historico_desvio")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoDesvio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desvio_id", nullable = false)
    private Desvio desvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAcaoHistoricoDesvio tipo;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private StatusDesvio statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_atual", length = 30)
    private StatusDesvio statusAtual;

    @Column(name = "snapshot_observacao", columnDefinition = "TEXT")
    private String snapshotObservacao;

    @Column(name = "snapshot_evidencia_id")
    private UUID snapshotEvidenciaId;

    @Column(name = "data_acao", nullable = false)
    private LocalDateTime dataAcao;
}

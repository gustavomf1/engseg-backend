package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "investigacao_snapshot")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigacaoSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @Column(name = "porque_um", columnDefinition = "TEXT")
    private String porqueUm;

    @Column(name = "porque_um_resposta", columnDefinition = "TEXT")
    private String porqueUmResposta;

    @Column(name = "porque_dois", columnDefinition = "TEXT")
    private String porqueDois;

    @Column(name = "porque_dois_resposta", columnDefinition = "TEXT")
    private String porqueDoisResposta;

    @Column(name = "porque_tres", columnDefinition = "TEXT")
    private String porqueTres;

    @Column(name = "porque_tres_resposta", columnDefinition = "TEXT")
    private String porqueTresResposta;

    @Column(name = "porque_quatro", columnDefinition = "TEXT")
    private String porqueQuatro;

    @Column(name = "porque_quatro_resposta", columnDefinition = "TEXT")
    private String porqueQuatroResposta;

    @Column(name = "porque_cinco", columnDefinition = "TEXT")
    private String porqueCinco;

    @Column(name = "porque_cinco_resposta", columnDefinition = "TEXT")
    private String porqueCincoResposta;

    @Column(name = "causa_raiz", columnDefinition = "TEXT")
    private String causaRaiz;

    @ElementCollection
    @CollectionTable(name = "investigacao_snapshot_atividade", joinColumns = @JoinColumn(name = "snapshot_id"))
    @Column(name = "descricao", columnDefinition = "TEXT")
    @OrderColumn(name = "ordem")
    @Builder.Default
    private List<String> atividades = new ArrayList<>();

    @Column(name = "data_submissao", nullable = false)
    private LocalDateTime dataSubmissao;

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDENTE";

    @Column(name = "comentario_revisao", columnDefinition = "TEXT")
    private String comentarioRevisao;
}

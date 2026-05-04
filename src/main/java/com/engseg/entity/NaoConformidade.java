package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "nao_conformidade")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NaoConformidade extends Ocorrencia {

@Column(name = "severidade", nullable = false)
    private int severidade;

    @Column(name = "probabilidade", nullable = false)
    private int probabilidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false)
    private NivelRisco nivelRisco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eng_responsavel_construtora_id")
    private Usuario engResponsavelConstrutora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eng_responsavel_verificacao_id")
    private Usuario engResponsavelVerificacao;

    @Column(name = "data_limite_resolucao")
    private LocalDate dataLimiteResolucao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusNaoConformidade status = StatusNaoConformidade.ABERTA;

    @Column(name = "vencida", length = 1, nullable = false)
    @Builder.Default
    private String vencida = "N";

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "nao_conformidade_norma",
            joinColumns = @JoinColumn(name = "nao_conformidade_id"),
            inverseJoinColumns = @JoinColumn(name = "norma_id")
    )
    @Builder.Default
    private List<Norma> normas = new ArrayList<>();

    @Column(name = "reincidencia", length = 1, nullable = false)
    @Builder.Default
    private String reincidencia = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nc_anterior_id")
    private NaoConformidade ncAnterior;

    // Campos de investigação — 5 Porquês e Causa Raiz (preenchidos pelo Externo)
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

    // Descrição do que foi executado antes de enviar evidências para validação
    @Column(name = "descricao_execucao", columnDefinition = "TEXT")
    private String descricaoExecucao;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AtividadePlanoAcao> atividades = new ArrayList<>();

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HistoricoNc> historico = new ArrayList<>();

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<InvestigacaoSnapshot> investigacaoSnapshots = new ArrayList<>();

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ExecucaoSnapshot> execucaoSnapshots = new ArrayList<>();

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Devolutiva> devolutivas;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExecucaoAcao> execucoes;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Validacao> validacoes;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NcTrechoNorma> trechosNorma = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "nc_email_manual", joinColumns = @JoinColumn(name = "nc_id"))
    @Column(name = "email")
    @Builder.Default
    private List<String> emailsManuais = new ArrayList<>();
}

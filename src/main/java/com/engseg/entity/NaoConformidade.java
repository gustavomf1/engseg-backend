package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

@Enumerated(EnumType.STRING)
    @Column(name = "nivel_severidade", nullable = false)
    private NivelSeveridade nivelSeveridade;

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

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Devolutiva> devolutivas;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExecucaoAcao> execucoes;

    @OneToMany(mappedBy = "naoConformidade", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Validacao> validacoes;
}

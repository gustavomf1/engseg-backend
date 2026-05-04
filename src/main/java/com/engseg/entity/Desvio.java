package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "desvio")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Desvio extends Ocorrencia {

    @Column(name = "orientacao_realizada", nullable = false)
    private String orientacaoRealizada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_contratada_id")
    private Empresa empresaContratada;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "desvio_email_manual", joinColumns = @JoinColumn(name = "desvio_id"))
    @Column(name = "email")
    @Builder.Default
    private List<String> emailsManuais = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_criacao_id")
    private Usuario usuarioCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_desvio_id")
    private Usuario responsavelDesvio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_tratativa_id")
    private Usuario responsavelTratativa;

    @Column(name = "observacao_tratativa", columnDefinition = "TEXT")
    private String observacaoTratativa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidencia_tratativa_id")
    private Evidencia evidenciaTratativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusDesvio status = StatusDesvio.AGUARDANDO_TRATATIVA;

    @OneToMany(mappedBy = "desvio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dataAcao ASC")
    @Builder.Default
    private List<HistoricoDesvio> historico = new ArrayList<>();

    @OneToMany(mappedBy = "desvio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("numero ASC")
    @Builder.Default
    private List<TrativaDesvio> tratativas = new ArrayList<>();
}

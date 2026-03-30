package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nc_trecho_norma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NcTrechoNorma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "norma_id", nullable = false)
    private Norma norma;

    @Column(name = "clausula_referencia", length = 100)
    private String clausulaReferencia;

    @Column(name = "texto_editado", columnDefinition = "TEXT", nullable = false)
    private String textoEditado;

    @Column(name = "data_vinculo", nullable = false)
    @Builder.Default
    private LocalDateTime dataVinculo = LocalDateTime.now();
}

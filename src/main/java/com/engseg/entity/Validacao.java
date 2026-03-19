package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "validacao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Validacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParecerValidacao parecer;

    private String observacao;

    @Column(name = "data_validacao", nullable = false)
    private LocalDateTime dataValidacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engenheiro_id")
    private Usuario engenheiro;
}

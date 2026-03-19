package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devolutiva")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Devolutiva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id", nullable = false)
    private NaoConformidade naoConformidade;

    @Column(name = "descricao_plano_acao", nullable = false)
    private String descricaoPlanoAcao;

    @Column(name = "data_devolutiva", nullable = false)
    private LocalDateTime dataDevolutiva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engenheiro_id")
    private Usuario engenheiro;
}

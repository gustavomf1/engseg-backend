package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evidencia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "url_arquivo", nullable = false)
    private String urlArquivo;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evidencia", nullable = false)
    private TipoEvidencia tipoEvidencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nao_conformidade_id")
    private NaoConformidade naoConformidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execucao_acao_id")
    private ExecucaoAcao execucaoAcao;
}

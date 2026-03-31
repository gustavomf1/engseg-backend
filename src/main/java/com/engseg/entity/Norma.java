package com.engseg.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "norma")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Norma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "TEXT")
    private String conteudo;

    @Convert(converter = BooleanToSNConverter.class)
    @Column(nullable = false, length = 1)
    @Builder.Default
    private boolean ativo = true;

    @Column(name = "dt_inativacao")
    private LocalDate dtInativacao;
}

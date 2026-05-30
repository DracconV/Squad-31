package br.gov.seed.relatorios.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "desempenho_turma", indexes = {
    @Index(name = "idx_turma_id_unique", columnList = "turma_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesempenhoTurma {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID turmaId;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal mediaTurma = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal medianaTurma = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maiorNota = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal menorNota = BigDecimal.TEN;

    @Builder.Default
    @Column(nullable = false)
    private Float taxaConclusao = 0f;

    @Builder.Default
    @Column(nullable = false)
    private Integer alunosAtivos = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer totalAlunos = 0;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}

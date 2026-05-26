package br.gov.seed.relatorios.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "desempenho_aluno", indexes = {
    @Index(name = "idx_aluno_id", columnList = "aluno_id"),
    @Index(name = "idx_turma_id", columnList = "turma_id"),
    @Index(name = "idx_disciplina", columnList = "disciplina")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DesempenhoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID alunoId;

    @Column(nullable = false)
    private UUID turmaId;

    @Column(nullable = false)
    private String disciplina;

    @Builder.Default
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal notaMedia = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Integer questoesAcertadas = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer questoesTotal = 0;

    @Builder.Default
    @Column(nullable = false)
    private Float taxaAcerto = 0f;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    public void calcularTaxaAcerto() {
        if (questoesTotal > 0) {
            this.taxaAcerto = (questoesAcertadas.floatValue() / questoesTotal) * 100;
        }
    }

    public void calcularNotaMedia() {
        if (questoesTotal > 0) {
            float nota = (questoesAcertadas.floatValue() / questoesTotal) * 10;
            this.notaMedia = BigDecimal.valueOf(nota);
        }
    }
}

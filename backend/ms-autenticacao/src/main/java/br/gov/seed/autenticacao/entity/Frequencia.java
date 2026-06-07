package br.gov.seed.autenticacao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/** Frequência (presença/faltas) de um aluno em uma disciplina. */
@Entity
@Table(name = "frequencia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Frequencia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(name = "turma_id", nullable = false)
    private UUID turmaId;

    @Column(nullable = false, length = 100)
    private String disciplina;

    @Builder.Default
    @Column(name = "total_aulas", nullable = false)
    private Integer totalAulas = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer faltas = 0;

    @Builder.Default
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    /** Percentual de presença (0–100). */
    public double getPresenca() {
        if (totalAulas == null || totalAulas <= 0) return 100.0;
        int presentes = Math.max(0, totalAulas - (faltas == null ? 0 : faltas));
        return Math.round((presentes * 10000.0 / totalAulas)) / 100.0;
    }
}

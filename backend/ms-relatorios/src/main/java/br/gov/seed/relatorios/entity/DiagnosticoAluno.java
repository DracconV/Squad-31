package br.gov.seed.relatorios.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Diagnóstico adaptativo por disciplina gerado a partir do historico_questao_aluno.
 * Mapeado à tabela diagnostico_aluno (V5__criar_historico_e_diagnostico.sql).
 */
@Entity
@Table(name = "diagnostico_aluno",
       indexes = @Index(name = "idx_diagnostico_aluno", columnList = "aluno_id, gerado_em DESC"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DiagnosticoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(name = "disciplina_id", nullable = false)
    private UUID disciplinaId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> payload;

    @Builder.Default
    @Column(name = "gerado_em", nullable = false, updatable = false)
    private LocalDateTime geradoEm = LocalDateTime.now();

    @Column(name = "versao_modelo", length = 50)
    private String versaoModelo;
}

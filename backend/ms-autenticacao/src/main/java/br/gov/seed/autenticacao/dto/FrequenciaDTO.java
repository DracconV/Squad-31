package br.gov.seed.autenticacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FrequenciaDTO {

    /** Upsert de frequência de um aluno numa disciplina. */
    public record RegistrarRequest(
        @NotNull UUID alunoId,
        @NotNull UUID turmaId,
        @NotBlank String disciplina,
        @NotNull @Min(0) Integer totalAulas,
        @NotNull @Min(0) Integer faltas
    ) {}

    /** Item do resumo de frequência do aluno (por disciplina). */
    public record ResumoItem(
        String disciplina,
        int aulas,
        int faltas,
        double presenca,
        int limite,
        @JsonProperty("atualizado_em") LocalDateTime atualizadoEm
    ) {}

    /** Linha de aluno na visão da turma. */
    public record TurmaAlunoItem(
        UUID alunoId,
        String nome,
        String matricula,
        int faltas,
        double presenca,
        String status
    ) {}

    /** Visão agregada de frequência de uma turma. */
    public record TurmaResponse(
        UUID turmaId,
        @JsonProperty("presenca_media") double presencaMedia,
        @JsonProperty("total_alunos") int totalAlunos,
        @JsonProperty("alunos_em_atencao") int alunosEmAtencao,
        List<TurmaAlunoItem> alunos,
        @JsonProperty("atualizado_em") LocalDateTime atualizadoEm
    ) {}
}

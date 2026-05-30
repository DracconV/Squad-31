package br.gov.seed.relatorios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DesempenhoDTO {

    public record DesempenhoAlunoResponse(
        UUID id,
        UUID alunoId,
        UUID turmaId,
        String disciplina,
        @JsonProperty("nota_media")
        BigDecimal notaMedia,
        @JsonProperty("questoes_acertadas")
        Integer questoesAcertadas,
        @JsonProperty("questoes_total")
        Integer questoesTotal,
        @JsonProperty("taxa_acerto")
        Float taxaAcerto,
        @JsonProperty("atualizado_em")
        LocalDateTime atualizadoEm
    ) {}

    public record DesempenhoTurmaResponse(
        UUID id,
        UUID turmaId,
        @JsonProperty("media_turma")
        BigDecimal mediaTurma,
        @JsonProperty("mediana_turma")
        BigDecimal medianaTurma,
        @JsonProperty("maior_nota")
        BigDecimal maiorNota,
        @JsonProperty("menor_nota")
        BigDecimal menorNota,
        @JsonProperty("taxa_conclusao")
        Float taxaConclusao,
        @JsonProperty("alunos_ativos")
        Integer alunosAtivos,
        @JsonProperty("total_alunos")
        Integer totalAlunos,
        @JsonProperty("atualizado_em")
        LocalDateTime atualizadoEm
    ) {}
}


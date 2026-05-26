package br.gov.seed.relatorios.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RelatorioDTO {

    public record ResumoRede(
            @JsonProperty("total_instituicoes") long totalInstituicoes,
            @JsonProperty("total_turmas") long totalTurmas,
            @JsonProperty("total_alunos") long totalAlunos,
            @JsonProperty("total_professores") long totalProfessores,
            @JsonProperty("media_geral_nota") BigDecimal mediaGeralNota,
            @JsonProperty("gerado_em") LocalDateTime geradoEm
    ) {}

    public record ResumoEscola(
            @JsonProperty("instituicao_id") UUID instituicaoId,
            @JsonProperty("total_turmas") long totalTurmas,
            @JsonProperty("total_alunos") long totalAlunos,
            @JsonProperty("media_nota_escola") BigDecimal mediaNotaEscola,
            @JsonProperty("gerado_em") LocalDateTime geradoEm
    ) {}

    public record ResultadoSimuladoRede(
            @JsonProperty("simulado_id") UUID simuladoId,
            @JsonProperty("total_tentativas") long totalTentativas,
            @JsonProperty("nota_media") BigDecimal notaMedia,
            @JsonProperty("taxa_acerto") Double taxaAcerto,
            @JsonProperty("gerado_em") LocalDateTime geradoEm
    ) {}

    public record AlunosPrimeiroAcesso(
            @JsonProperty("total") long total,
            @JsonProperty("alunos") List<AlunoPrimeiroAcessoItem> alunos
    ) {}

    public record AlunoPrimeiroAcessoItem(
            UUID id,
            String nome,
            String matricula,
            String perfil,
            @JsonProperty("criado_em") LocalDateTime criadoEm
    ) {}

    public record TaxaConclusaoCurso(
            @JsonProperty("curso_id") UUID cursoId,
            @JsonProperty("total_inscritos") long totalInscritos,
            @JsonProperty("total_concluidos") long totalConcluidos,
            @JsonProperty("taxa_conclusao") double taxaConclusao
    ) {}

    public record RelatorioAuditoria(
            @JsonProperty("total") long total,
            @JsonProperty("registros") List<Map<String, Object>> registros
    ) {}
}

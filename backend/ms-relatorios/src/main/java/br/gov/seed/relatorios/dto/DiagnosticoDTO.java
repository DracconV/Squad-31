package br.gov.seed.relatorios.dto;

import br.gov.seed.relatorios.entity.DiagnosticoAluno;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiagnosticoDTO {

    /** Um item de desempenho por disciplina calculado do histórico. */
    public record DesempenhoDisciplina(
            @JsonProperty("disciplina_id")    UUID disciplinaId,
            @JsonProperty("disciplina_nome")  String disciplinaNome,
            @JsonProperty("total_respondidas") long totalRespondidas,
            @JsonProperty("total_acertos")     long totalAcertos,
            @JsonProperty("taxa_acerto")       double taxaAcerto,
            String nivel
    ) {
        /** Classifica o nível com base na taxa de acerto. */
        public static String classificarNivel(double taxa) {
            if (taxa >= 75) return "FORTE";
            if (taxa >= 50) return "ADEQUADO";
            return "FRACO";
        }
    }

    /** Resposta completa do diagnóstico de um aluno. */
    public record DiagnosticoResponse(
            @JsonProperty("aluno_id")   UUID alunoId,
            @JsonProperty("gerado_em")  LocalDateTime geradoEm,
            @JsonProperty("disciplinas") List<DesempenhoDisciplina> disciplinas
    ) {}

    /** Resposta do diagnóstico persistido (inclui id do registro). */
    public record DiagnosticoSalvoResponse(
            UUID id,
            @JsonProperty("aluno_id")      UUID alunoId,
            @JsonProperty("disciplina_id") UUID disciplinaId,
            Map<String, Object> payload,
            @JsonProperty("gerado_em")     LocalDateTime geradoEm,
            @JsonProperty("versao_modelo") String versaoModelo
    ) {
        public static DiagnosticoSalvoResponse from(DiagnosticoAluno d) {
            return new DiagnosticoSalvoResponse(
                    d.getId(), d.getAlunoId(), d.getDisciplinaId(),
                    d.getPayload(), d.getGeradoEm(), d.getVersaoModelo()
            );
        }
    }
}

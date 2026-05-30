package br.gov.seed.simulados.dto;

import java.util.List;
import java.util.UUID;

/** Gabarito comentado de uma questão para revisão pós-simulado. */
public record RevisaoResponse(
        int ordem,
        UUID questaoId,
        String enunciado,
        String explicacao,
        List<Alternativa> alternativas
) {
    public record Alternativa(
            UUID id,
            String texto,
            boolean correta,
            int ordem
    ) {}
}

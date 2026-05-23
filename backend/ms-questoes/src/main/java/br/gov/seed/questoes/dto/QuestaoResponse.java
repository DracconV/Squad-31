package br.gov.seed.questoes.dto;

import br.gov.seed.questoes.entity.Questao;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record QuestaoResponse(
        UUID id,
        String enunciado,
        String tipo,
        String dificuldade,
        String tipoUso,
        String disciplina,
        List<AlternativaDto> alternativas
) {
    public static QuestaoResponse from(Questao q) {
        List<AlternativaDto> alts = q.getAlternativas() == null ? List.of() :
                q.getAlternativas().stream()
                        .map(a -> new AlternativaDto(a.getId(), a.getTexto(), a.isCorreta(), a.getOrdem()))
                        .collect(Collectors.toList());
        return new QuestaoResponse(
                q.getId(),
                q.getEnunciado(),
                q.getTipo(),
                q.getDificuldade(),
                q.getTipoUso(),
                q.getDisciplina() != null ? q.getDisciplina().getNome() : null,
                alts
        );
    }
}

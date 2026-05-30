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
        String nivelEnsino,
        String disciplina,
        String explicacao,
        List<AlternativaDto> alternativas
) {
    /** Para uso por alunos — gabarito oculto (correta = null). */
    public static QuestaoResponse from(Questao q) {
        return from(q, false);
    }

    /**
     * @param incluirGabarito true para PROFESSOR/ADMIN — expõe qual alternativa é correta.
     *                        false para ALUNO — campo {@code correta} retorna null.
     */
    public static QuestaoResponse from(Questao q, boolean incluirGabarito) {
        List<AlternativaDto> alts = q.getAlternativas() == null ? List.of() :
                q.getAlternativas().stream()
                        .map(a -> new AlternativaDto(
                                a.getId(),
                                a.getTexto(),
                                incluirGabarito ? a.isCorreta() : null,
                                a.getOrdem()))
                        .collect(Collectors.toList());
        return new QuestaoResponse(
                q.getId(),
                q.getEnunciado(),
                q.getTipo(),
                q.getDificuldade(),
                q.getTipoUso(),
                q.getNivelEnsino(),
                q.getDisciplina() != null ? q.getDisciplina().getNome() : null,
                // Explicação revela o gabarito → só exposta a professor/admin (durante prova fica oculta)
                incluirGabarito ? q.getExplicacao() : null,
                alts
        );
    }
}

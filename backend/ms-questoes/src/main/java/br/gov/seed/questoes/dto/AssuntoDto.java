package br.gov.seed.questoes.dto;

import br.gov.seed.questoes.entity.Assunto;

import java.util.UUID;

public record AssuntoDto(
        UUID id,
        String nome,
        UUID disciplinaId,
        String nomeDisciplina
) {
    public static AssuntoDto from(Assunto a) {
        return new AssuntoDto(
                a.getId(),
                a.getNome(),
                a.getDisciplina() != null ? a.getDisciplina().getId() : null,
                a.getDisciplina() != null ? a.getDisciplina().getNome() : null
        );
    }
}

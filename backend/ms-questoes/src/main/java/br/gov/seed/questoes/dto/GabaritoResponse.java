package br.gov.seed.questoes.dto;

import java.util.UUID;

/** Resposta correta de uma questão, revelada sob demanda (modo praticar). */
public record GabaritoResponse(
        UUID questaoId,
        UUID alternativaCorretaId,
        String explicacao
) {}

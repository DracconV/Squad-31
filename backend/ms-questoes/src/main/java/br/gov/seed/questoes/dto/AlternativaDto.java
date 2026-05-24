package br.gov.seed.questoes.dto;

import java.util.UUID;

public record AlternativaDto(
        UUID id,
        String texto,
        boolean correta,
        int ordem
) {}

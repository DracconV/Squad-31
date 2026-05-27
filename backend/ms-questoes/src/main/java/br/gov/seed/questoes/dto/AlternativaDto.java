package br.gov.seed.questoes.dto;

import java.util.UUID;

public record AlternativaDto(
        UUID id,
        String texto,
        Boolean correta,  // null quando o chamador não tem perfil de professor/admin
        int ordem
) {}

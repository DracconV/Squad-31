package br.gov.seed.questoes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CriarQuestaoRequest(
        @NotBlank String enunciado,
        @NotBlank String tipo,
        @NotBlank String dificuldade,
        @NotBlank String tipoUso,
        @NotBlank String nivelEnsino,
        String explicacao,
        @NotNull UUID disciplinaId,
        UUID assuntoId,
        @NotNull @Size(min = 2, max = 5) @Valid List<AlternativaRequest> alternativas
) {
    public record AlternativaRequest(
            @NotBlank String texto,
            boolean correta,
            int ordem
    ) {}
}

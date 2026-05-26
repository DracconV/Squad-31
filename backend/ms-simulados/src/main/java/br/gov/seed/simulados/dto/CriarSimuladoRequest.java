package br.gov.seed.simulados.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

public record CriarSimuladoRequest(
        @NotBlank String titulo,
        UUID turmaId,
        @Min(1) int tempoMinutos,
        boolean pontuado,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}

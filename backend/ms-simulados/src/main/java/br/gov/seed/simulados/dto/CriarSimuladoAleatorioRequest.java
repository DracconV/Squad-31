package br.gov.seed.simulados.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Cria um simulado já preenchido com questões sorteadas aleatoriamente do banco.
 * Os filtros (disciplina, dificuldade, nível) são opcionais — se omitidos,
 * sorteia entre todas as questões ativas.
 */
public record CriarSimuladoAleatorioRequest(
        @NotBlank String titulo,
        UUID turmaId,
        @Min(1) int tempoMinutos,
        boolean pontuado,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        @Min(value = 1, message = "Informe ao menos 1 questão") int quantidade,
        UUID disciplinaId,
        String dificuldade,
        String nivelEnsino
) {}

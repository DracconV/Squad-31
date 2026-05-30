package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapeado à tabela simulado (V4__criar_simulados.sql).
 * Os campos professor_id e turma_id são UUIDs simples — sem join cross-service.
 */
@Data
@Entity
@Table(name = "simulado")
public class Simulado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titulo;

    @Column(name = "professor_id", nullable = false)
    private UUID professorId;

    @Column(name = "turma_id")
    private UUID turmaId;

    @Column(name = "tempo_minutos", nullable = false)
    private int tempoMinutos = 60;

    @Column(nullable = false)
    private boolean pontuado = true;

    @Column(name = "data_inicio")
    private LocalDateTime dataInicio;

    @Column(name = "data_fim")
    private LocalDateTime dataFim;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
}

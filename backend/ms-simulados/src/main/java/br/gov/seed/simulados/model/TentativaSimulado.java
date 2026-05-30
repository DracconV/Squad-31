package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapeado à tabela tentativa_simulado (V4__criar_simulados.sql).
 */
@Data
@Entity
@Table(name = "tentativa_simulado")
public class TentativaSimulado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(name = "simulado_id", nullable = false)
    private UUID simuladoId;

    @Column(precision = 5, scale = 2)
    private BigDecimal nota;

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm = LocalDateTime.now();

    @Column(name = "finalizado_em")
    private LocalDateTime finalizadoEm;

    @Column(name = "tempo_gasto_segundos")
    private Integer tempoGastoSegundos;
}

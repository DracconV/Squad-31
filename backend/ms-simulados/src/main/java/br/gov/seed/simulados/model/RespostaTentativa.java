package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapeado à tabela resposta_tentativa (V4__criar_simulados.sql).
 */
@Data
@Entity
@Table(name = "resposta_tentativa")
public class RespostaTentativa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tentativa_id", nullable = false)
    private UUID tentativaId;

    @Column(name = "questao_id", nullable = false)
    private UUID questaoId;

    @Column(name = "alternativa_id")
    private UUID alternativaId;

    @Column(name = "respondido_em", nullable = false)
    private LocalDateTime respondidoEm = LocalDateTime.now();
}

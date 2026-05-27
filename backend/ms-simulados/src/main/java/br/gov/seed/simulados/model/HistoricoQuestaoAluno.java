package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Grava cada questão respondida pelo aluno ao finalizar um simulado.
 * Mapeado à tabela historico_questao_aluno (V5__criar_historico_e_diagnostico.sql).
 */
@Data
@Entity
@Table(name = "historico_questao_aluno")
public class HistoricoQuestaoAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aluno_id", nullable = false)
    private UUID alunoId;

    @Column(name = "questao_id", nullable = false)
    private UUID questaoId;

    @Column(nullable = false)
    private boolean acertou;

    @Column(name = "respondido_em", nullable = false)
    private LocalDateTime respondidoEm = LocalDateTime.now();
}

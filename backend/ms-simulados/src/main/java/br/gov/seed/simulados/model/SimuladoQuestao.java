package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Mapeado à tabela simulado_questao (PK composta: simulado_id + questao_id).
 * questao_id é UUID simples — a Questao em si está no ms-questoes.
 */
@Data
@Entity
@Table(name = "simulado_questao")
public class SimuladoQuestao {

    @EmbeddedId
    private SimuladoQuestaoId id;

    @Column(nullable = false)
    private int ordem;
}

package br.gov.seed.simulados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SimuladoQuestaoId implements Serializable {

    @Column(name = "simulado_id")
    private UUID simuladoId;

    @Column(name = "questao_id")
    private UUID questaoId;
}

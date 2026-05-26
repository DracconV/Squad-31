package br.gov.seed.simulados.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessaoSimulado implements Serializable {

    private UUID simuladoId;

    /** ID do aluno que iniciou o simulado (para verificação de propriedade). */
    private UUID alunoId;

    private LocalDateTime iniciadoEm;
    private int questaoAtual = 0;

    /** Mapa questaoIndex (0-based) → alternativaId escolhida */
    private Map<Integer, String> respostas = new HashMap<>();
}

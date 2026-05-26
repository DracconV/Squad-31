package br.gov.seed.simulados.dto;

import br.gov.seed.simulados.model.TentativaSimulado;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ResultadoResponse {

    private UUID tentativaId;
    private UUID simuladoId;
    private BigDecimal nota;
    private int totalQuestoes;
    private int acertos;
    private LocalDateTime iniciadoEm;
    private LocalDateTime finalizadoEm;
    private Integer tempoGastoSegundos;

    public static ResultadoResponse from(TentativaSimulado t, int totalQuestoes, int acertos) {
        ResultadoResponse r = new ResultadoResponse();
        r.setTentativaId(t.getId());
        r.setSimuladoId(t.getSimuladoId());
        r.setNota(t.getNota());
        r.setTotalQuestoes(totalQuestoes);
        r.setAcertos(acertos);
        r.setIniciadoEm(t.getIniciadoEm());
        r.setFinalizadoEm(t.getFinalizadoEm());
        r.setTempoGastoSegundos(t.getTempoGastoSegundos());
        return r;
    }
}

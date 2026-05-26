package br.gov.seed.simulados.dto;

import br.gov.seed.simulados.model.TentativaSimulado;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TentativaResponse {

    private UUID id;
    private UUID simuladoId;
    private UUID alunoId;
    private BigDecimal nota;
    private LocalDateTime iniciadoEm;
    private LocalDateTime finalizadoEm;
    private Integer tempoGastoSegundos;

    public static TentativaResponse from(TentativaSimulado t) {
        TentativaResponse r = new TentativaResponse();
        r.setId(t.getId());
        r.setSimuladoId(t.getSimuladoId());
        r.setAlunoId(t.getAlunoId());
        r.setNota(t.getNota());
        r.setIniciadoEm(t.getIniciadoEm());
        r.setFinalizadoEm(t.getFinalizadoEm());
        r.setTempoGastoSegundos(t.getTempoGastoSegundos());
        return r;
    }
}

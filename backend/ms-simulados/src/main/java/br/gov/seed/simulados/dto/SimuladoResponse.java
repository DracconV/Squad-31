package br.gov.seed.simulados.dto;

import br.gov.seed.simulados.model.Simulado;
import br.gov.seed.simulados.model.SimuladoQuestao;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SimuladoResponse {

    private UUID id;
    private String titulo;
    private UUID professorId;
    private UUID turmaId;
    private int tempoMinutos;
    private boolean pontuado;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private LocalDateTime criadoEm;

    /** Lista de questões ordenadas (apenas IDs, em ordem). Presente somente no GET /{id}. */
    private List<UUID> questaoIds;

    public static SimuladoResponse from(Simulado s) {
        SimuladoResponse r = new SimuladoResponse();
        r.setId(s.getId());
        r.setTitulo(s.getTitulo());
        r.setProfessorId(s.getProfessorId());
        r.setTurmaId(s.getTurmaId());
        r.setTempoMinutos(s.getTempoMinutos());
        r.setPontuado(s.isPontuado());
        r.setDataInicio(s.getDataInicio());
        r.setDataFim(s.getDataFim());
        r.setCriadoEm(s.getCriadoEm());
        return r;
    }

    public static SimuladoResponse from(Simulado s, List<SimuladoQuestao> questoes) {
        SimuladoResponse r = from(s);
        r.setQuestaoIds(questoes.stream()
                .map(q -> q.getId().getQuestaoId())
                .toList());
        return r;
    }
}

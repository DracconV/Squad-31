package br.gov.seed.relatorios.service;

import br.gov.seed.relatorios.dto.DiagnosticoDTO;
import br.gov.seed.relatorios.entity.DiagnosticoAluno;
import br.gov.seed.relatorios.repository.DiagnosticoAlunoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosticoService {

    private static final String VERSAO_MODELO = "regra-v1";

    private final JdbcTemplate jdbcTemplate;
    private final DiagnosticoAlunoRepository diagnosticoRepo;

    /**
     * Computa o diagnóstico de um aluno a partir do historico_questao_aluno,
     * persiste na tabela diagnostico_aluno e retorna a visão consolidada.
     */
    @Transactional
    public DiagnosticoDTO.DiagnosticoResponse gerarEPersistir(UUID alunoId) {
        List<DiagnosticoDTO.DesempenhoDisciplina> disciplinas = calcularPorDisciplina(alunoId);

        LocalDateTime agora = LocalDateTime.now();

        // Persiste um registro por disciplina
        for (DiagnosticoDTO.DesempenhoDisciplina d : disciplinas) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("total_respondidas", d.totalRespondidas());
            payload.put("total_acertos", d.totalAcertos());
            payload.put("taxa_acerto", d.taxaAcerto());
            payload.put("nivel", d.nivel());
            payload.put("disciplina_nome", d.disciplinaNome());

            DiagnosticoAluno diagnostico = DiagnosticoAluno.builder()
                    .alunoId(alunoId)
                    .disciplinaId(d.disciplinaId())
                    .payload(payload)
                    .geradoEm(agora)
                    .versaoModelo(VERSAO_MODELO)
                    .build();
            diagnosticoRepo.save(diagnostico);
        }

        log.info("Diagnóstico gerado para aluno {} — {} disciplinas", alunoId, disciplinas.size());
        return new DiagnosticoDTO.DiagnosticoResponse(alunoId, agora, disciplinas);
    }

    /**
     * Retorna o diagnóstico mais recente persistido de um aluno (sem recalcular).
     */
    public DiagnosticoDTO.DiagnosticoResponse buscar(UUID alunoId) {
        List<DiagnosticoAluno> registros = diagnosticoRepo.findByAlunoIdOrderByGeradoEmDesc(alunoId);
        if (registros.isEmpty()) {
            // Se não há diagnóstico salvo, calcula on-the-fly sem persistir
            List<DiagnosticoDTO.DesempenhoDisciplina> disciplinas = calcularPorDisciplina(alunoId);
            return new DiagnosticoDTO.DiagnosticoResponse(alunoId, LocalDateTime.now(), disciplinas);
        }

        // Agrupa o mais recente por disciplina (já ordenado por gerado_em DESC)
        Map<UUID, DiagnosticoAluno> ultimoPorDisciplina = new java.util.LinkedHashMap<>();
        for (DiagnosticoAluno r : registros) {
            ultimoPorDisciplina.putIfAbsent(r.getDisciplinaId(), r);
        }

        LocalDateTime geradoEm = registros.get(0).getGeradoEm();

        List<DiagnosticoDTO.DesempenhoDisciplina> disciplinas = ultimoPorDisciplina.values().stream()
                .map(r -> {
                    Map<String, Object> p = r.getPayload();
                    long total   = toLong(p.get("total_respondidas"));
                    long acertos = toLong(p.get("total_acertos"));
                    double taxa  = toDouble(p.get("taxa_acerto"));
                    String nivel = (String) p.getOrDefault("nivel", DiagnosticoDTO.DesempenhoDisciplina.classificarNivel(taxa));
                    String nome  = (String) p.getOrDefault("disciplina_nome", "");
                    return new DiagnosticoDTO.DesempenhoDisciplina(
                            r.getDisciplinaId(), nome, total, acertos, taxa, nivel
                    );
                })
                .toList();

        return new DiagnosticoDTO.DiagnosticoResponse(alunoId, geradoEm, disciplinas);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<DiagnosticoDTO.DesempenhoDisciplina> calcularPorDisciplina(UUID alunoId) {
        String sql =
            "SELECT q.disciplina_id, d.nome AS disciplina_nome, " +
            "       COUNT(*) AS total_respondidas, " +
            "       SUM(CASE WHEN h.acertou THEN 1 ELSE 0 END) AS total_acertos " +
            "FROM historico_questao_aluno h " +
            "JOIN questao q ON q.id = h.questao_id " +
            "JOIN disciplina d ON d.id = q.disciplina_id " +
            "WHERE h.aluno_id = ? " +
            "GROUP BY q.disciplina_id, d.nome " +
            "ORDER BY total_acertos DESC";

        List<Map<String, Object>> rows;
        try {
            rows = jdbcTemplate.queryForList(sql, alunoId);
        } catch (Exception e) {
            log.warn("Erro ao calcular diagnóstico para aluno {}: {}", alunoId, e.getMessage());
            return new ArrayList<>();
        }

        return rows.stream().map(r -> {
            UUID disciplinaId = UUID.fromString(r.get("disciplina_id").toString());
            String nome       = (String) r.get("disciplina_nome");
            long total        = toLong(r.get("total_respondidas"));
            long acertos      = toLong(r.get("total_acertos"));
            double taxa       = total > 0 ? (acertos * 100.0 / total) : 0.0;
            String nivel      = DiagnosticoDTO.DesempenhoDisciplina.classificarNivel(taxa);
            return new DiagnosticoDTO.DesempenhoDisciplina(disciplinaId, nome, total, acertos, taxa, nivel);
        }).toList();
    }

    private long toLong(Object obj) {
        if (obj == null) return 0L;
        return ((Number) obj).longValue();
    }

    private double toDouble(Object obj) {
        if (obj == null) return 0.0;
        return ((Number) obj).doubleValue();
    }
}

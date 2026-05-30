package br.gov.seed.relatorios.service;

import br.gov.seed.relatorios.dto.RelatorioDTO;
import br.gov.seed.relatorios.repository.DesempenhoAlunoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final JdbcTemplate jdbcTemplate;
    private final DesempenhoAlunoRepository desempenhoAlunoRepo;

    // ── Resumo da rede ────────────────────────────────────────────────────────

    public RelatorioDTO.ResumoRede resumoRede() {
        long totalInstituicoes = count("SELECT COUNT(*) FROM instituicao WHERE ativo = true");
        long totalTurmas       = count("SELECT COUNT(*) FROM turma WHERE ativo = true");
        long totalAlunos       = count("SELECT COUNT(*) FROM usuario WHERE perfil IN ('ALUNO_EM','ALUNO_EJA','ALUNO_PROF') AND ativo = true");
        long totalProfessores  = count("SELECT COUNT(*) FROM usuario WHERE perfil = 'PROFESSOR' AND ativo = true");

        // AVG no banco — evita carregar toda a tabela desempenho_aluno em memória
        Double mediaDouble = desempenhoAlunoRepo.findMediaGeral();
        BigDecimal mediaGeralNota = mediaDouble != null
                ? BigDecimal.valueOf(mediaDouble).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RelatorioDTO.ResumoRede(
                totalInstituicoes, totalTurmas, totalAlunos, totalProfessores,
                mediaGeralNota, LocalDateTime.now()
        );
    }

    // ── Resumo escola ─────────────────────────────────────────────────────────

    public RelatorioDTO.ResumoEscola resumoEscola(UUID instituicaoId) {
        long totalTurmas = count(
                "SELECT COUNT(*) FROM turma WHERE instituicao_id = ? AND ativo = true",
                instituicaoId.toString());
        long totalAlunos = count(
                "SELECT COUNT(*) FROM usuario u " +
                "JOIN aluno_turma at ON at.aluno_id = u.id " +
                "JOIN turma t ON t.id = at.turma_id " +
                "WHERE t.instituicao_id = ? AND u.ativo = true",
                instituicaoId.toString());

        // Média de nota por turmas da escola a partir de desempenho_aluno
        Double mediaDouble = jdbcTemplate.queryForObject(
                "SELECT AVG(da.nota_media) FROM desempenho_aluno da " +
                "JOIN turma t ON t.id = da.turma_id " +
                "WHERE t.instituicao_id = ?",
                Double.class, instituicaoId.toString());
        BigDecimal mediaNotaEscola = mediaDouble != null
                ? BigDecimal.valueOf(mediaDouble).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new RelatorioDTO.ResumoEscola(
                instituicaoId, totalTurmas, totalAlunos, mediaNotaEscola, LocalDateTime.now()
        );
    }

    // ── Resultado de simulado (visão rede) ────────────────────────────────────

    public RelatorioDTO.ResultadoSimuladoRede resultadoSimulado(UUID simuladoId) {
        long totalTentativas = count(
                "SELECT COUNT(*) FROM tentativa_simulado WHERE simulado_id = ? AND finalizado_em IS NOT NULL",
                simuladoId.toString());

        Double notaMediaDouble = jdbcTemplate.queryForObject(
                "SELECT AVG(nota) FROM tentativa_simulado WHERE simulado_id = ? AND finalizado_em IS NOT NULL",
                Double.class, simuladoId.toString());
        BigDecimal notaMedia = notaMediaDouble != null
                ? BigDecimal.valueOf(notaMediaDouble).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Double taxaAcerto = notaMedia.doubleValue() * 10; // nota 0-10 → taxa 0-100%

        return new RelatorioDTO.ResultadoSimuladoRede(
                simuladoId, totalTentativas, notaMedia, taxaAcerto, LocalDateTime.now()
        );
    }

    // ── Alunos primeiro acesso ────────────────────────────────────────────────

    public RelatorioDTO.AlunosPrimeiroAcesso alunosPrimeiroAcesso() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, nome, matricula, perfil, criado_em FROM usuario " +
                "WHERE primeiro_acesso = true AND ativo = true " +
                "ORDER BY criado_em DESC LIMIT 200");

        List<RelatorioDTO.AlunoPrimeiroAcessoItem> alunos = rows.stream().map(r ->
                new RelatorioDTO.AlunoPrimeiroAcessoItem(
                        UUID.fromString(r.get("id").toString()),
                        (String) r.get("nome"),
                        (String) r.get("matricula"),
                        (String) r.get("perfil"),
                        toLocalDateTime(r.get("criado_em"))
                )
        ).toList();

        return new RelatorioDTO.AlunosPrimeiroAcesso(alunos.size(), alunos);
    }

    // ── Taxa de conclusão de cursos ───────────────────────────────────────────

    public List<RelatorioDTO.TaxaConclusaoCurso> taxaConclusaoCursos() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT curso_id, " +
                "  COUNT(*) AS total_inscritos, " +
                "  SUM(CASE WHEN concluido = true THEN 1 ELSE 0 END) AS total_concluidos " +
                "FROM inscricao_curso " +
                "GROUP BY curso_id " +
                "ORDER BY total_inscritos DESC");

        return rows.stream().map(r -> {
            long total = ((Number) r.get("total_inscritos")).longValue();
            long concluidos = ((Number) r.get("total_concluidos")).longValue();
            double taxa = total > 0 ? (concluidos * 100.0 / total) : 0.0;
            return new RelatorioDTO.TaxaConclusaoCurso(
                    UUID.fromString(r.get("curso_id").toString()),
                    total, concluidos,
                    BigDecimal.valueOf(taxa).setScale(2, RoundingMode.HALF_UP).doubleValue()
            );
        }).toList();
    }

    // ── Painel macro por município ────────────────────────────────────────────

    public RelatorioDTO.PainelMacro painelMacro() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT " +
                "  i.municipio, " +
                "  COUNT(DISTINCT i.id)                                                      AS total_instituicoes, " +
                "  COUNT(DISTINCT CASE WHEN u.perfil IN ('ALUNO_EM','ALUNO_EJA','ALUNO_PROF')" +
                "                      AND u.ativo = true THEN u.id END)                     AS total_alunos, " +
                "  COUNT(DISTINCT CASE WHEN u.perfil = 'PROFESSOR'" +
                "                      AND u.ativo = true THEN u.id END)                     AS total_professores, " +
                "  AVG(da.nota_media)                                                        AS media_notas " +
                "FROM instituicao i " +
                "LEFT JOIN usuario u   ON u.instituicao_id = i.id " +
                "LEFT JOIN turma t     ON t.instituicao_id = i.id " +
                "LEFT JOIN desempenho_aluno da ON da.turma_id = t.id " +
                "WHERE i.ativo = true " +
                "GROUP BY i.municipio " +
                "ORDER BY total_alunos DESC");

        List<RelatorioDTO.PainelMunicipioItem> municipios = rows.stream().map(r -> {
            String municipio     = (String) r.get("municipio");
            long totalInst       = ((Number) r.get("total_instituicoes")).longValue();
            long totalAlunos     = ((Number) r.get("total_alunos")).longValue();
            long totalProf       = ((Number) r.get("total_professores")).longValue();
            Object mediaObj      = r.get("media_notas");
            BigDecimal media     = mediaObj != null
                    ? BigDecimal.valueOf(((Number) mediaObj).doubleValue()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            return new RelatorioDTO.PainelMunicipioItem(municipio, totalInst, totalAlunos, totalProf, media);
        }).toList();

        return new RelatorioDTO.PainelMacro(municipios.size(), municipios, LocalDateTime.now());
    }

    // ── Auditoria ─────────────────────────────────────────────────────────────

    public RelatorioDTO.RelatorioAuditoria auditoria(int limite) {
        int lim = Math.min(Math.max(limite, 1), 500);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, usuario_id, acao, entidade, entidade_id, \"timestamp\" " +
                "FROM audit_log " +
                "ORDER BY \"timestamp\" DESC LIMIT " + lim);

        return new RelatorioDTO.RelatorioAuditoria(
                count("SELECT COUNT(*) FROM audit_log"), rows
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private long count(String sql, Object... args) {
        try {
            Long result = jdbcTemplate.queryForObject(sql, Long.class, args);
            return result != null ? result : 0L;
        } catch (Exception e) {
            log.warn("Erro ao contar registros: {} — {}", sql, e.getMessage());
            return 0L;
        }
    }

    private LocalDateTime toLocalDateTime(Object obj) {
        if (obj == null) return null;
        if (obj instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        if (obj instanceof LocalDateTime ldt) return ldt;
        return null;
    }
}

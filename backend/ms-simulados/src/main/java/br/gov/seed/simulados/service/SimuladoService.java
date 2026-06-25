package br.gov.seed.simulados.service;

import br.gov.seed.simulados.dto.CriarSimuladoAleatorioRequest;
import br.gov.seed.simulados.dto.CriarSimuladoRequest;
import br.gov.seed.simulados.dto.ResultadoResponse;
import br.gov.seed.simulados.dto.RevisaoResponse;
import br.gov.seed.simulados.dto.SimuladoResponse;
import br.gov.seed.simulados.dto.TentativaResponse;
import br.gov.seed.simulados.model.*;
import br.gov.seed.simulados.repository.HistoricoQuestaoAlunoRepository;
import br.gov.seed.simulados.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimuladoService {

    private final SimuladoRepository simuladoRepo;
    private final SimuladoQuestaoRepository simuladoQuestaoRepo;
    private final TentativaSimuladoRepository tentativaRepo;
    private final RespostaTentativaRepository respostaTentativaRepo;
    private final AlternativaRepository alternativaRepo;
    private final HistoricoQuestaoAlunoRepository historicoRepo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    /** Lista simulados disponíveis. Se turmaId informado, filtra pela turma. */
    public List<SimuladoResponse> listar(UUID turmaId) {
        if (turmaId != null) {
            return simuladoRepo.findByTurmaIdOrderByCriadoEmDesc(turmaId)
                    .stream()
                    .map(SimuladoResponse::from)
                    .toList();
        }
        return simuladoRepo.findDisponiveis(LocalDateTime.now())
                .stream()
                .map(SimuladoResponse::from)
                .toList();
    }

    /** Retorna simulado com a lista de IDs de questões. */
    public SimuladoResponse buscarPorId(UUID id) {
        Simulado simulado = simuladoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulado não encontrado: " + id));
        List<SimuladoQuestao> questoes = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(id);
        return SimuladoResponse.from(simulado, questoes);
    }

    /**
     * Finaliza o simulado: calcula nota, persiste TentativaSimulado e RespostaTentativa.
     * Retorna o resultado detalhado para exibição imediata.
     */
    @Transactional
    public ResultadoResponse finalizar(UUID simuladoId, UUID alunoId, SessaoSimulado sessao) {
        List<SimuladoQuestao> questoes = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(simuladoId);
        int total = questoes.size();
        int acertos = 0;

        // Salva tentativa
        TentativaSimulado tentativa = new TentativaSimulado();
        tentativa.setSimuladoId(simuladoId);
        tentativa.setAlunoId(alunoId);
        tentativa.setIniciadoEm(sessao.getIniciadoEm());

        LocalDateTime agora = LocalDateTime.now();
        tentativa.setFinalizadoEm(agora);
        tentativa.setTempoGastoSegundos(
                (int) Duration.between(sessao.getIniciadoEm(), agora).toSeconds()
        );

        // Coleta todos os alternativaIds de uma vez para fazer batch load
        List<RespostaTentativa> respostas = new ArrayList<>();
        List<UUID> alternativaIds = new ArrayList<>();

        for (int i = 0; i < questoes.size(); i++) {
            SimuladoQuestao sq = questoes.get(i);
            String alternativaIdStr = sessao.getRespostas().get(i);

            UUID alternativaId = null;
            if (alternativaIdStr != null && !alternativaIdStr.isBlank()) {
                try {
                    alternativaId = UUID.fromString(alternativaIdStr);
                } catch (IllegalArgumentException e) {
                    log.warn("alternativaId inválido na sessão index={}: {}", i, alternativaIdStr);
                }
            }
            alternativaIds.add(alternativaId); // null = não respondida

            RespostaTentativa resposta = new RespostaTentativa();
            resposta.setQuestaoId(sq.getId().getQuestaoId());
            resposta.setAlternativaId(alternativaId);
            resposta.setRespondidoEm(agora);
            respostas.add(resposta);
        }

        // Batch load — 1 query para todas as alternativas marcadas
        List<UUID> idsParaBuscar = alternativaIds.stream().filter(id -> id != null).toList();
        Map<UUID, Alternativa> alternativaCache = alternativaRepo.findAllById(idsParaBuscar)
                .stream().collect(java.util.stream.Collectors.toMap(Alternativa::getId, a -> a));

        for (UUID altId : idsParaBuscar) {
            Alternativa alt = alternativaCache.get(altId);
            if (alt != null && alt.isCorreta()) acertos++;
        }

        BigDecimal nota = total > 0
                ? BigDecimal.valueOf((double) acertos * 10 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        tentativa.setNota(nota);

        TentativaSimulado salva = tentativaRepo.save(tentativa);

        // Prepara respostas e histórico em listas — salva em batch (2 queries em vez de 2N)
        List<HistoricoQuestaoAluno> historicos = new ArrayList<>();
        for (int i = 0; i < respostas.size(); i++) {
            RespostaTentativa r = respostas.get(i);
            r.setTentativaId(salva.getId());

            UUID questaoId = questoes.get(i).getId().getQuestaoId();
            UUID alternativaId = r.getAlternativaId();
            boolean acertou = alternativaId != null &&
                    alternativaCache.getOrDefault(alternativaId, null) != null &&
                    alternativaCache.get(alternativaId).isCorreta();

            HistoricoQuestaoAluno historico = new HistoricoQuestaoAluno();
            historico.setAlunoId(alunoId);
            historico.setQuestaoId(questaoId);
            historico.setAcertou(acertou);
            historico.setRespondidoEm(agora);
            historicos.add(historico);
        }
        respostaTentativaRepo.saveAll(respostas);
        historicoRepo.saveAll(historicos);

        // Busca disciplina de cada questão via query no DB compartilhado
        // e publica um OutboxEvent por disciplina (para ms-relatorios segmentar desempenho)
        Simulado simulado = simuladoRepo.findById(simuladoId).orElse(null);
        UUID turmaId = simulado != null ? simulado.getTurmaId() : null;

        List<UUID> questaoIds = questoes.stream()
                .map(sq -> sq.getId().getQuestaoId())
                .toList();

        // questaoId → nome da disciplina (query no DB compartilhado)
        Map<UUID, String> disciplinaPorQuestao = buscarDisciplinasPorQuestoes(questaoIds);

        // Agrupa acertos/total por disciplina
        Map<String, int[]> acertosPorDisciplina = new HashMap<>();
        for (int i = 0; i < questoes.size(); i++) {
            UUID questaoId = questoes.get(i).getId().getQuestaoId();
            String disc = disciplinaPorQuestao.getOrDefault(questaoId, "GERAL");
            acertosPorDisciplina.computeIfAbsent(disc, k -> new int[]{0, 0});
            acertosPorDisciplina.get(disc)[1]++; // total

            UUID altId = respostas.get(i).getAlternativaId();
            if (altId != null) {
                Alternativa alt = alternativaCache.get(altId);
                if (alt != null && alt.isCorreta()) {
                    acertosPorDisciplina.get(disc)[0]++; // acertos
                }
            }
        }

        // Publica um evento por disciplina — RuntimeException mantém rollback do @Transactional
        for (Map.Entry<String, int[]> entry : acertosPorDisciplina.entrySet()) {
            OutboxEvent evento = new OutboxEvent();
            evento.setTipo("SIMULADO_FINALIZADO");
            // HashMap (não Map.of) para omitir turmaId quando nula —
            // "" quebraria a desserialização de UUID no consumidor (ms-relatorios)
            Map<String, Object> payload = new HashMap<>();
            payload.put("alunoId",    alunoId.toString());
            if (turmaId != null) payload.put("turmaId", turmaId.toString());
            payload.put("simuladoId", simuladoId.toString());
            payload.put("disciplina", entry.getKey());
            payload.put("acertos",    entry.getValue()[0]);
            payload.put("total",      entry.getValue()[1]);
            try {
                evento.setPayload(objectMapper.writeValueAsString(payload));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new RuntimeException("Erro ao serializar evento outbox para disciplina " + entry.getKey(), e);
            }
            outboxRepo.save(evento);
        }

        log.info("Simulado {} finalizado por aluno {} — nota={} acertos={}/{}", simuladoId, alunoId, nota, acertos, total);
        return ResultadoResponse.from(salva, total, acertos);
    }

    /** Resultado da última tentativa de um aluno em um simulado. */
    public ResultadoResponse resultado(UUID simuladoId, UUID alunoId) {
        TentativaSimulado tentativa = tentativaRepo
                .findTopByAlunoIdAndSimuladoIdOrderByIniciadoEmDesc(alunoId, simuladoId)
                .orElseThrow(() -> new RuntimeException("Nenhuma tentativa encontrada"));

        List<SimuladoQuestao> questoes = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(simuladoId);
        List<RespostaTentativa> respostas = respostaTentativaRepo.findByTentativaId(tentativa.getId());

        // Batch load — 1 query para todas as alternativas marcadas (evita N queries)
        List<UUID> idsRespondidos = respostas.stream()
                .map(RespostaTentativa::getAlternativaId)
                .filter(id -> id != null)
                .toList();
        Map<UUID, Alternativa> cache = alternativaRepo.findAllById(idsRespondidos)
                .stream().collect(Collectors.toMap(Alternativa::getId, a -> a));

        int acertos = (int) respostas.stream()
                .filter(r -> r.getAlternativaId() != null)
                .filter(r -> {
                    Alternativa alt = cache.get(r.getAlternativaId());
                    return alt != null && alt.isCorreta();
                })
                .count();

        return ResultadoResponse.from(tentativa, questoes.size(), acertos);
    }

    /** Todas as tentativas do aluno em qualquer simulado. */
    public List<TentativaResponse> minhasTentativas(UUID alunoId) {
        return tentativaRepo.findByAlunoIdOrderByIniciadoEmDesc(alunoId)
                .stream()
                .map(TentativaResponse::from)
                .toList();
    }

    // ── CRUD de Simulado (professor) ─────────────────────────────────────────

    @Transactional
    public SimuladoResponse criar(CriarSimuladoRequest request, UUID professorId) {
        validarPeriodo(request.dataInicio(), request.dataFim());
        Simulado simulado = new Simulado();
        simulado.setTitulo(request.titulo());
        simulado.setProfessorId(professorId);
        simulado.setTurmaId(request.turmaId());
        simulado.setTempoMinutos(request.tempoMinutos() > 0 ? request.tempoMinutos() : 60);
        simulado.setPontuado(request.pontuado());
        simulado.setDataInicio(request.dataInicio());
        simulado.setDataFim(request.dataFim());
        return SimuladoResponse.from(simuladoRepo.save(simulado));
    }

    /**
     * Cria um simulado já preenchido com questões sorteadas aleatoriamente,
     * respeitando filtros opcionais de disciplina, dificuldade e nível de ensino.
     */
    @Transactional
    public SimuladoResponse criarAleatorio(CriarSimuladoAleatorioRequest request, UUID professorId) {
        validarPeriodo(request.dataInicio(), request.dataFim());
        if (request.quantidade() < 1) {
            throw new IllegalArgumentException("A quantidade de questões deve ser no mínimo 1");
        }

        List<UUID> questaoIds = sortearQuestoes(
                request.disciplinaId(), request.dificuldade(), request.nivelEnsino(), request.quantidade());

        if (questaoIds.isEmpty()) {
            throw new IllegalArgumentException("Nenhuma questão encontrada para os filtros informados");
        }

        Simulado simulado = new Simulado();
        simulado.setTitulo(request.titulo());
        simulado.setProfessorId(professorId);
        simulado.setTurmaId(request.turmaId());
        simulado.setTempoMinutos(request.tempoMinutos() > 0 ? request.tempoMinutos() : 60);
        simulado.setPontuado(request.pontuado());
        simulado.setDataInicio(request.dataInicio());
        simulado.setDataFim(request.dataFim());
        Simulado salvo = simuladoRepo.save(simulado);

        List<SimuladoQuestao> vinculos = new ArrayList<>();
        int ordem = 1;
        for (UUID questaoId : questaoIds) {
            SimuladoQuestao sq = new SimuladoQuestao();
            sq.setId(new SimuladoQuestaoId(salvo.getId(), questaoId));
            sq.setOrdem(ordem++);
            vinculos.add(sq);
        }
        simuladoQuestaoRepo.saveAll(vinculos);

        log.info("Simulado aleatório {} criado por {} com {} questões (disciplina={}, dificuldade={}, nivel={})",
                salvo.getId(), professorId, questaoIds.size(),
                request.disciplinaId(), request.dificuldade(), request.nivelEnsino());

        List<SimuladoQuestao> questoes = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(salvo.getId());
        return SimuladoResponse.from(salvo, questoes);
    }

    /** Sorteia IDs de questões ativas no banco compartilhado conforme filtros opcionais. */
    private List<UUID> sortearQuestoes(UUID disciplinaId, String dificuldade, String nivelEnsino, int quantidade) {
        StringBuilder sql = new StringBuilder("SELECT id::text FROM questao WHERE ativa = true");
        List<Object> params = new ArrayList<>();

        if (disciplinaId != null) {
            sql.append(" AND disciplina_id = CAST(? AS uuid)");
            params.add(disciplinaId.toString());
        }
        if (dificuldade != null && !dificuldade.isBlank()) {
            sql.append(" AND dificuldade = ?");
            params.add(dificuldade.trim());
        }
        if (nivelEnsino != null && !nivelEnsino.isBlank()) {
            sql.append(" AND nivel_ensino = ?");
            params.add(nivelEnsino.trim());
        }
        sql.append(" ORDER BY random() LIMIT ?");
        params.add(quantidade);

        List<String> ids = jdbcTemplate.queryForList(sql.toString(), String.class, params.toArray());
        return ids.stream().map(UUID::fromString).toList();
    }

    @Transactional
    public SimuladoResponse atualizar(UUID id, CriarSimuladoRequest request) {
        validarPeriodo(request.dataInicio(), request.dataFim());
        Simulado simulado = simuladoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulado não encontrado: " + id));
        if (request.titulo() != null && !request.titulo().isBlank()) {
            simulado.setTitulo(request.titulo());
        }
        if (request.turmaId() != null) simulado.setTurmaId(request.turmaId());
        if (request.tempoMinutos() > 0) simulado.setTempoMinutos(request.tempoMinutos());
        simulado.setPontuado(request.pontuado());
        if (request.dataInicio() != null) simulado.setDataInicio(request.dataInicio());
        if (request.dataFim() != null) simulado.setDataFim(request.dataFim());
        return SimuladoResponse.from(simuladoRepo.save(simulado));
    }

    @Transactional
    public void desativar(UUID id) {
        Simulado simulado = simuladoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulado não encontrado: " + id));
        // Marca dataFim como agora para desativar
        simulado.setDataFim(java.time.LocalDateTime.now().minusSeconds(1));
        simuladoRepo.save(simulado);
    }

    /** Simulados criados pelo professor. */
    public List<SimuladoResponse> meusPorProfessor(UUID professorId) {
        return simuladoRepo.findByProfessorIdOrderByCriadoEmDesc(professorId)
                .stream()
                .map(SimuladoResponse::from)
                .toList();
    }

    // ── Gestão de questões do simulado ────────────────────────────────────────

    public List<SimuladoQuestao> listarQuestoes(UUID simuladoId) {
        if (!simuladoRepo.existsById(simuladoId)) {
            throw new RuntimeException("Simulado não encontrado: " + simuladoId);
        }
        return simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(simuladoId);
    }

    @Transactional
    public SimuladoQuestao adicionarQuestao(UUID simuladoId, UUID questaoId) {
        if (!simuladoRepo.existsById(simuladoId)) {
            throw new RuntimeException("Simulado não encontrado: " + simuladoId);
        }
        SimuladoQuestaoId sqId = new SimuladoQuestaoId(simuladoId, questaoId);
        if (simuladoQuestaoRepo.existsById(sqId)) {
            throw new IllegalStateException("Questão já está no simulado");
        }
        int proximaOrdem = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(simuladoId).size() + 1;
        SimuladoQuestao sq = new SimuladoQuestao();
        sq.setId(sqId);
        sq.setOrdem(proximaOrdem);
        return simuladoQuestaoRepo.save(sq);
    }

    @Transactional
    public void removerQuestao(UUID simuladoId, UUID questaoId) {
        SimuladoQuestaoId sqId = new SimuladoQuestaoId(simuladoId, questaoId);
        if (!simuladoQuestaoRepo.existsById(sqId)) {
            throw new RuntimeException("Questão não encontrada no simulado");
        }
        simuladoQuestaoRepo.deleteById(sqId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Garante que dataFim seja posterior a dataInicio quando ambas informadas. */
    private void validarPeriodo(LocalDateTime dataInicio, LocalDateTime dataFim) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("dataFim deve ser posterior a dataInicio");
        }
    }

    /** Consulta o nome da disciplina de cada questão no DB compartilhado. */
    private Map<UUID, String> buscarDisciplinasPorQuestoes(List<UUID> questaoIds) {
        if (questaoIds.isEmpty()) return Map.of();
        String placeholders = questaoIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT q.id, d.nome FROM questao q JOIN disciplina d ON d.id = q.disciplina_id WHERE q.id IN (" + placeholders + ")";
        // UUID nativo: o driver pgjdbc mapeia java.util.UUID -> uuid.
        // Passar String quebra o "WHERE id IN (?)" (operador uuid = text inexistente -> bad SQL grammar).
        Object[] params = questaoIds.toArray();
        Map<UUID, String> resultado = new HashMap<>();
        jdbcTemplate.query(sql, params, rs -> {
            resultado.put(UUID.fromString(rs.getString(1)), rs.getString(2));
        });
        return resultado;
    }

    // ── Revisão (gabarito comentado pós-simulado) ─────────────────────────────

    /**
     * Retorna cada questão do simulado com alternativas, gabarito e explicação.
     * Alunos só acessam após terem uma tentativa finalizada; professor/admin sempre.
     */
    public List<RevisaoResponse> revisao(UUID simuladoId, UUID alunoId, boolean privilegiado) {
        List<SimuladoQuestao> questoes = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(simuladoId);
        if (questoes.isEmpty() && !simuladoRepo.existsById(simuladoId)) {
            throw new RuntimeException("Simulado não encontrado: " + simuladoId);
        }
        if (!privilegiado) {
            boolean finalizou = tentativaRepo
                    .findTopByAlunoIdAndSimuladoIdOrderByIniciadoEmDesc(alunoId, simuladoId)
                    .map(t -> t.getFinalizadoEm() != null)
                    .orElse(false);
            if (!finalizou) {
                throw new IllegalStateException("Revisão disponível apenas após finalizar o simulado");
            }
        }

        List<UUID> questaoIds = questoes.stream().map(sq -> sq.getId().getQuestaoId()).toList();
        if (questaoIds.isEmpty()) return List.of();

        String placeholders = questaoIds.stream().map(id -> "?").collect(Collectors.joining(","));
        // UUID nativo: o driver pgjdbc mapeia java.util.UUID -> uuid.
        // Passar String quebra o "WHERE id IN (?)" (operador uuid = text inexistente -> bad SQL grammar).
        Object[] params = questaoIds.toArray();

        // questaoId → [enunciado, explicacao]
        Map<UUID, String[]> questaoInfo = new HashMap<>();
        jdbcTemplate.query(
                "SELECT id, enunciado, explicacao FROM questao WHERE id IN (" + placeholders + ")",
                params,
                rs -> {
                    questaoInfo.put(UUID.fromString(rs.getString("id")),
                            new String[]{rs.getString("enunciado"), rs.getString("explicacao")});
                });

        // questaoId → alternativas ordenadas
        Map<UUID, List<RevisaoResponse.Alternativa>> altsPorQuestao = new HashMap<>();
        jdbcTemplate.query(
                "SELECT id, questao_id, texto, correta, ordem FROM alternativa WHERE questao_id IN (" + placeholders + ") ORDER BY ordem",
                params,
                rs -> {
                    UUID qid = UUID.fromString(rs.getString("questao_id"));
                    altsPorQuestao.computeIfAbsent(qid, k -> new ArrayList<>())
                            .add(new RevisaoResponse.Alternativa(
                                    UUID.fromString(rs.getString("id")),
                                    rs.getString("texto"),
                                    rs.getBoolean("correta"),
                                    rs.getInt("ordem")));
                });

        List<RevisaoResponse> resultado = new ArrayList<>();
        for (SimuladoQuestao sq : questoes) {
            UUID qid = sq.getId().getQuestaoId();
            String[] info = questaoInfo.getOrDefault(qid, new String[]{null, null});
            resultado.add(new RevisaoResponse(
                    sq.getOrdem(), qid, info[0], info[1],
                    altsPorQuestao.getOrDefault(qid, List.of())));
        }
        return resultado;
    }

    // ── Gabarito ──────────────────────────────────────────────────────────────

    public List<java.util.Map<String, Object>> gabarito(UUID simuladoId) {
        List<SimuladoQuestao> questoes = simuladoQuestaoRepo.findByIdSimuladoIdOrderByOrdem(simuladoId);
        if (questoes.isEmpty() && !simuladoRepo.existsById(simuladoId)) {
            throw new RuntimeException("Simulado não encontrado: " + simuladoId);
        }
        return questoes.stream().map(sq -> {
            UUID questaoId = sq.getId().getQuestaoId();
            UUID alternativaCorreta = alternativaRepo.findByQuestaoIdAndCorretaTrue(questaoId)
                    .map(Alternativa::getId)
                    .orElse(null);
            return java.util.Map.<String, Object>of(
                    "ordem", sq.getOrdem(),
                    "questaoId", questaoId,
                    "alternativaCorretaId", alternativaCorreta != null ? alternativaCorreta.toString() : "N/D"
            );
        }).toList();
    }
}

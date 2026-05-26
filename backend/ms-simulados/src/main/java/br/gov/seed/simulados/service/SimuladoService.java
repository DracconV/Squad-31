package br.gov.seed.simulados.service;

import br.gov.seed.simulados.dto.CriarSimuladoRequest;
import br.gov.seed.simulados.dto.ResultadoResponse;
import br.gov.seed.simulados.dto.SimuladoResponse;
import br.gov.seed.simulados.dto.TentativaResponse;
import br.gov.seed.simulados.model.*;
import br.gov.seed.simulados.repository.HistoricoQuestaoAlunoRepository;
import br.gov.seed.simulados.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        // Conta acertos antecipadamente para calcular nota
        List<RespostaTentativa> respostas = new ArrayList<>();
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

            if (alternativaId != null) {
                Alternativa alt = alternativaRepo.findById(alternativaId).orElse(null);
                if (alt != null && alt.isCorreta()) {
                    acertos++;
                }
            }

            RespostaTentativa resposta = new RespostaTentativa();
            resposta.setQuestaoId(sq.getId().getQuestaoId());
            resposta.setAlternativaId(alternativaId);
            resposta.setRespondidoEm(agora);
            respostas.add(resposta);
        }

        BigDecimal nota = total > 0
                ? BigDecimal.valueOf((double) acertos * 10 / total).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        tentativa.setNota(nota);

        TentativaSimulado salva = tentativaRepo.save(tentativa);

        // Salva respostas com FK para a tentativa + grava histórico por questão
        for (int i = 0; i < respostas.size(); i++) {
            RespostaTentativa r = respostas.get(i);
            r.setTentativaId(salva.getId());
            respostaTentativaRepo.save(r);

            // Histórico individual de cada questão respondida
            UUID questaoId = questoes.get(i).getId().getQuestaoId();
            UUID alternativaId = r.getAlternativaId();
            boolean acertou = false;
            if (alternativaId != null) {
                Alternativa alt = alternativaRepo.findById(alternativaId).orElse(null);
                acertou = alt != null && alt.isCorreta();
            }
            HistoricoQuestaoAluno historico = new HistoricoQuestaoAluno();
            historico.setAlunoId(alunoId);
            historico.setQuestaoId(questaoId);
            historico.setAcertou(acertou);
            historico.setRespondidoEm(agora);
            historicoRepo.save(historico);
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

        int acertos = (int) respostas.stream()
                .filter(r -> r.getAlternativaId() != null)
                .filter(r -> alternativaRepo.findById(r.getAlternativaId())
                        .map(Alternativa::isCorreta)
                        .orElse(false))
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

    @Transactional
    public SimuladoResponse atualizar(UUID id, CriarSimuladoRequest request) {
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

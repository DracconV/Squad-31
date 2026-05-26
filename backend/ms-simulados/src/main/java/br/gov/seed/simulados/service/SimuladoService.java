package br.gov.seed.simulados.service;

import br.gov.seed.simulados.dto.ResultadoResponse;
import br.gov.seed.simulados.dto.SimuladoResponse;
import br.gov.seed.simulados.dto.TentativaResponse;
import br.gov.seed.simulados.model.*;
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

    /** Lista simulados disponíveis (sem dataFim ou com dataFim futura). */
    public List<SimuladoResponse> listar() {
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

        // Salva respostas com FK para a tentativa
        for (RespostaTentativa r : respostas) {
            r.setTentativaId(salva.getId());
            respostaTentativaRepo.save(r);
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
}

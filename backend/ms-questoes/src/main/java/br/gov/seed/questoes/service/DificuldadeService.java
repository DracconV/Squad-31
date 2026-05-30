package br.gov.seed.questoes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Recalcula a dificuldade das questões a partir do desempenho real dos alunos.
 *
 * Fonte: tabela {@code historico_questao_aluno} (preenchida pelo ms-simulados a
 * cada simulado finalizado). A dificuldade passa a refletir a taxa de acerto
 * observada, em vez da heurística (ano + índice) usada na importação inicial.
 *
 * Só recalcula questões com amostra mínima de respostas — abaixo disso a
 * dificuldade heurística é mantida.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DificuldadeService {

    private final JdbcTemplate jdbcTemplate;

    /** Mínimo de respostas para considerar a dificuldade estatisticamente significativa. */
    @Value("${dificuldade.amostra-minima:20}")
    private int amostraMinima;

    /** Taxa de acerto acima da qual a questão é FÁCIL (0–1). */
    @Value("${dificuldade.limiar-facil:0.70}")
    private double limiarFacil;

    /** Taxa de acerto abaixo da qual a questão é DIFÍCIL (0–1). */
    @Value("${dificuldade.limiar-dificil:0.40}")
    private double limiarDificil;

    /**
     * Recalcula a dificuldade de todas as questões com amostra suficiente.
     *
     * @return quantidade de questões cuja dificuldade foi efetivamente alterada.
     */
    @Transactional
    public int recalcular() {
        List<Map<String, Object>> agregados = jdbcTemplate.queryForList(
                "SELECT questao_id, " +
                "       COUNT(*) AS total, " +
                "       SUM(CASE WHEN acertou THEN 1 ELSE 0 END) AS acertos " +
                "FROM historico_questao_aluno " +
                "GROUP BY questao_id " +
                "HAVING COUNT(*) >= ?",
                amostraMinima);

        int atualizadas = 0;
        for (Map<String, Object> row : agregados) {
            String questaoId = row.get("questao_id").toString();
            long total = ((Number) row.get("total")).longValue();
            long acertos = ((Number) row.get("acertos")).longValue();
            double taxa = total > 0 ? (double) acertos / total : 0.0;

            String novaDificuldade = classificar(taxa);

            // Só grava quando muda — evita escrita desnecessária
            int afetadas = jdbcTemplate.update(
                    "UPDATE questao SET dificuldade = ?, atualizado_em = now() " +
                    "WHERE id = CAST(? AS uuid) AND dificuldade <> ?",
                    novaDificuldade, questaoId, novaDificuldade);
            atualizadas += afetadas;
        }

        log.info("Recálculo de dificuldade concluído: {} questões avaliadas (amostra >= {}), {} atualizadas.",
                agregados.size(), amostraMinima, atualizadas);
        return atualizadas;
    }

    private String classificar(double taxaAcerto) {
        if (taxaAcerto >= limiarFacil) return "FACIL";
        if (taxaAcerto >= limiarDificil) return "MEDIO";
        return "DIFICIL";
    }

    /** Recálculo automático diário às 3h da manhã. */
    @Scheduled(cron = "${dificuldade.cron:0 0 3 * * *}")
    public void recalculoAgendado() {
        try {
            recalcular();
        } catch (Exception e) {
            log.warn("Falha no recálculo agendado de dificuldade: {}", e.getMessage());
        }
    }
}

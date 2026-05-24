package br.gov.seed.questoes.service;

import br.gov.seed.questoes.entity.Alternativa;
import br.gov.seed.questoes.entity.Disciplina;
import br.gov.seed.questoes.entity.Questao;
import br.gov.seed.questoes.repository.DisciplinaRepository;
import br.gov.seed.questoes.repository.QuestaoRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EnemImporterService {

    private final QuestaoRepository questaoRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${enem.api.base-url}")
    private String baseUrl;

    @Value("${enem.api.auto-import}")
    private boolean autoImport;

    // Limite máximo aceito pela API pública
    private static final int PAGE_LIMIT = 50;

    // Delay entre páginas para respeitar rate limit (~1 req/s com margem)
    private static final long DELAY_ENTRE_PAGINAS_MS = 1200;

    // Delay entre anos
    private static final long DELAY_ENTRE_ANOS_MS = 2000;

    // Máximo de retentativas em caso de 429
    private static final int MAX_RETRIES = 3;

    // ── DTOs internos para deserializar a API ──────────────────────────────

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuestoesResponse {
        private Metadata metadata;
        private List<EnemQuestion> questions;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private int total;
        private boolean hasMore;
        private int offset;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnemQuestion {
        private String title;
        private Integer index;
        private String discipline;
        private Integer year;
        private String context;
        private String alternativesIntroduction;
        private String correctAlternative;
        private List<EnemAlternative> alternatives;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnemAlternative {
        private String letter;
        private String text;
        private Boolean isCorrect;
    }

    // ── Mapeamento disciplina ──────────────────────────────────────────────

    private static final Map<String, String> DISCIPLINA_MAP = Map.of(
            "linguagens",        "Linguagens e Códigos",
            "matematica",        "Matemática",
            "ciencias-humanas",  "Ciências Humanas",
            "ciencias-natureza", "Ciências da Natureza"
    );

    // ── Cache de disciplinas em memória ───────────────────────────────────

    private final Map<String, Disciplina> disciplinaCache = new HashMap<>();

    // ── Anos conhecidos do ENEM ────────────────────────────────────────────

    private static final int[] ANOS_ENEM = {
            2009, 2010, 2011, 2012, 2013, 2014,
            2015, 2016, 2017, 2018, 2019, 2020,
            2021, 2022, 2023
    };

    // ── Ponto de entrada no startup ───────────────────────────────────────

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void onReady() {
        if (!autoImport) {
            log.info("ENEM auto-import desativado (enem.api.auto-import=false)");
            return;
        }
        if (questaoRepository.countByAtivaTrue() > 0) {
            log.info("Banco de questões já populado — importação ignorada.");
            return;
        }
        log.info("Iniciando importação de questões do ENEM...");
        importarTudo();
    }

    // ── Disparo manual (via controller) ───────────────────────────────────

    public void importarTudo() {
        UUID adminId = buscarAdminId();
        RestTemplate rest = new RestTemplate();

        log.info("Importando anos: {}", Arrays.toString(ANOS_ENEM));

        int totalImportadas = 0;
        for (int i = 0; i < ANOS_ENEM.length; i++) {
            int ano = ANOS_ENEM[i];
            int importadas = importarAno(rest, ano, adminId);
            totalImportadas += importadas;
            log.info("Ano {}: {} questões importadas. Total acumulado: {}", ano, importadas, totalImportadas);

            // Delay entre anos (exceto após o último)
            if (i < ANOS_ENEM.length - 1) {
                sleep(DELAY_ENTRE_ANOS_MS);
            }
        }
        log.info("Importação concluída. Total: {} questões.", totalImportadas);
    }

    // ── Importa todas as questões de um ano com paginação ─────────────────

    private int importarAno(RestTemplate rest, int ano, UUID adminId) {
        int offset = 0;
        int importadas = 0;

        while (true) {
            String url = String.format("%s/exams/%d/questions?limit=%d&offset=%d",
                    baseUrl, ano, PAGE_LIMIT, offset);

            QuestoesResponse resp = buscarComRetry(rest, url, ano, offset);
            if (resp == null) break;

            List<EnemQuestion> questions = resp.getQuestions();
            if (questions == null || questions.isEmpty()) break;

            for (EnemQuestion eq : questions) {
                try {
                    salvarQuestao(eq, adminId);
                    importadas++;
                } catch (Exception e) {
                    log.debug("Questão ignorada ({}/{}): {}", ano, eq.getIndex(), e.getMessage());
                }
            }

            if (resp.getMetadata() == null || !resp.getMetadata().isHasMore()) break;

            offset += PAGE_LIMIT;
            sleep(DELAY_ENTRE_PAGINAS_MS);
        }
        return importadas;
    }

    // ── Busca com retry em caso de 429 ────────────────────────────────────

    private QuestoesResponse buscarComRetry(RestTemplate rest, String url, int ano, int offset) {
        for (int tentativa = 1; tentativa <= MAX_RETRIES; tentativa++) {
            try {
                return rest.getForObject(url, QuestoesResponse.class);
            } catch (HttpClientErrorException.TooManyRequests e) {
                // Extrai o tempo de espera sugerido da mensagem (ex: "Try again in 7224ms")
                long waitMs = extrairWaitMs(e.getMessage());
                log.warn("Rate limit atingido (ano={} offset={}). Aguardando {}ms antes de tentar novamente ({}/{}).",
                        ano, offset, waitMs, tentativa, MAX_RETRIES);
                sleep(waitMs + 500); // margem extra de 500ms
            } catch (Exception e) {
                log.warn("Erro ao buscar ano={} offset={}: {}", ano, offset, e.getMessage());
                return null;
            }
        }
        log.warn("Desistindo após {} tentativas para ano={} offset={}", MAX_RETRIES, ano, offset);
        return null;
    }

    // ── Extrai ms do texto "Try again in 7224ms" ──────────────────────────

    private long extrairWaitMs(String message) {
        if (message == null) return 8000;
        try {
            int idx = message.indexOf("in ");
            int end = message.indexOf("ms", idx);
            if (idx >= 0 && end > idx) {
                return Long.parseLong(message.substring(idx + 3, end).trim());
            }
        } catch (Exception ignored) {}
        return 8000; // fallback seguro
    }

    // ── Persiste uma questão ───────────────────────────────────────────────

    @Transactional
    public void salvarQuestao(EnemQuestion eq, UUID adminId) {
        if (eq.getAlternatives() == null || eq.getAlternatives().isEmpty()) return;

        String nomeDisciplina = DISCIPLINA_MAP.getOrDefault(
                eq.getDiscipline() != null ? eq.getDiscipline().toLowerCase() : "",
                "Geral"
        );

        Disciplina disciplina = disciplinaCache.computeIfAbsent(nomeDisciplina,
                nome -> disciplinaRepository.findByNome(nome)
                        .orElseGet(() -> disciplinaRepository.save(
                                Disciplina.builder().nome(nome).build())));

        // Monta enunciado: contexto + introdução + título
        StringBuilder enunciado = new StringBuilder();
        if (eq.getContext() != null && !eq.getContext().isBlank()) {
            enunciado.append(eq.getContext()).append("\n\n");
        }
        if (eq.getAlternativesIntroduction() != null && !eq.getAlternativesIntroduction().isBlank()) {
            enunciado.append(eq.getAlternativesIntroduction()).append("\n\n");
        }
        if (eq.getTitle() != null && !eq.getTitle().isBlank()) {
            enunciado.append(eq.getTitle());
        }

        String textoFinal = enunciado.toString().trim();
        if (textoFinal.isEmpty()) return;

        Questao questao = Questao.builder()
                .enunciado(textoFinal)
                .tipo("MULTIPLA_ESCOLHA")
                .dificuldade("MEDIO")
                .tipoUso("AMBOS")
                .disciplina(disciplina)
                .criadoPor(adminId)
                .ativa(true)
                .build();

        List<Alternativa> alternativas = new ArrayList<>();
        int ordem = 1;
        for (EnemAlternative ea : eq.getAlternatives()) {
            if (ea.getText() == null || ea.getText().isBlank()) continue;
            boolean correta = eq.getCorrectAlternative() != null
                    && eq.getCorrectAlternative().equalsIgnoreCase(ea.getLetter());

            alternativas.add(Alternativa.builder()
                    .texto(ea.getText())
                    .correta(correta)
                    .ordem(ordem++)
                    .questao(questao)
                    .build());
        }

        questao.setAlternativas(alternativas);
        questaoRepository.save(questao);
    }

    // ── Busca UUID do admin no banco compartilhado ─────────────────────────

    private UUID buscarAdminId() {
        try {
            String idStr = jdbcTemplate.queryForObject(
                    "SELECT id::text FROM usuario WHERE matricula = 'admin' LIMIT 1",
                    String.class
            );
            if (idStr != null) return UUID.fromString(idStr);
        } catch (Exception e) {
            log.warn("Não encontrou admin no banco: {}. Usando UUID fixo.", e.getMessage());
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    // ── Utilitário de sleep ────────────────────────────────────────────────

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

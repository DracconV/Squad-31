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

    // ── DTOs internos para deserializar a API ──────────────────────────────

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExamsResponse {
        private List<ExamItem> exams;
    }

    @Data @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExamItem {
        private Integer year;
    }

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

        // Busca lista de anos disponíveis
        int[] anos = buscarAnos(rest);
        log.info("Anos disponíveis na API: {}", Arrays.toString(anos));

        int totalImportadas = 0;
        for (int ano : anos) {
            int importadas = importarAno(rest, ano, adminId);
            totalImportadas += importadas;
            log.info("Ano {}: {} questões importadas", ano, importadas);
        }
        log.info("Importação concluída. Total: {} questões.", totalImportadas);
    }

    // ── Busca anos disponíveis ─────────────────────────────────────────────

    private int[] buscarAnos(RestTemplate rest) {
        try {
            // A API retorna um array de inteiros diretamente
            Integer[] anos = rest.getForObject(baseUrl + "/exams", Integer[].class);
            if (anos == null) return new int[0];
            return Arrays.stream(anos).mapToInt(Integer::intValue).toArray();
        } catch (Exception e) {
            log.warn("Falha ao buscar anos da API ENEM: {}. Usando lista padrão.", e.getMessage());
            // Fallback: anos conhecidos do ENEM
            return new int[]{2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023};
        }
    }

    // ── Importa todas as questões de um ano ────────────────────────────────

    private int importarAno(RestTemplate rest, int ano, UUID adminId) {
        int offset = 0;
        int limit  = 100;
        int importadas = 0;

        while (true) {
            String url = String.format("%s/exams/%d/questions?limit=%d&offset=%d", baseUrl, ano, limit, offset);
            QuestoesResponse resp;
            try {
                resp = rest.getForObject(url, QuestoesResponse.class);
            } catch (Exception e) {
                log.warn("Erro ao buscar questões do ano {} offset {}: {}", ano, offset, e.getMessage());
                break;
            }

            if (resp == null || resp.getQuestions() == null || resp.getQuestions().isEmpty()) break;

            for (EnemQuestion eq : resp.getQuestions()) {
                try {
                    salvarQuestao(eq, adminId);
                    importadas++;
                } catch (Exception e) {
                    log.debug("Questão ignorada (possível duplicata): {} - {}", eq.getYear(), eq.getIndex());
                }
            }

            if (resp.getMetadata() == null || !resp.getMetadata().isHasMore()) break;
            offset += limit;
        }
        return importadas;
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

        // Enunciado: contexto + enunciado principal
        StringBuilder enunciado = new StringBuilder();
        if (eq.getContext() != null && !eq.getContext().isBlank()) {
            enunciado.append(eq.getContext()).append("\n\n");
        }
        if (eq.getAlternativesIntroduction() != null && !eq.getAlternativesIntroduction().isBlank()) {
            enunciado.append(eq.getAlternativesIntroduction()).append("\n\n");
        }
        if (eq.getTitle() != null) {
            enunciado.append(eq.getTitle());
        }

        Questao questao = Questao.builder()
                .enunciado(enunciado.toString().trim())
                .tipo("MULTIPLA_ESCOLHA")
                .dificuldade("MEDIA")
                .tipoUso("ENEM")
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
        // UUID fixo de fallback (garante NOT NULL)
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}

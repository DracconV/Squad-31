package br.gov.seed.questoes.controller;

import br.gov.seed.questoes.dto.AlternativaDto;
import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.service.EnemImporterService;
import br.gov.seed.questoes.service.QuestaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("QuestaoController — testes de API (integração)")
class QuestaoControllerTest {

    private static final String JWT_SECRET =
            "test-secret-key-para-testes-unitarios-12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestaoService questaoService;

    @MockitoBean
    private EnemImporterService enemImporterService;

    // ── Helpers ───────────────────────────────────────────────

    private String bearer(String perfil) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("perfil", perfil)
                .claim("nome", "Teste")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key)
                .compact();
        return "Bearer " + token;
    }

    private QuestaoResponse questaoFake() {
        return new QuestaoResponse(
                UUID.randomUUID(),
                "Quanto é 2 + 2?",
                "MULTIPLA_ESCOLHA",
                "FACIL",
                "AMBOS",
                "Matemática",
                List.of(new AlternativaDto(UUID.randomUUID(), "4", true, 1))
        );
    }

    // ── GET /questoes ─────────────────────────────────────────

    @Test
    @DisplayName("GET /questoes retorna 200 com página de questões")
    void listar_retorna200() throws Exception {
        var page = new PageImpl<>(List.of(questaoFake()), PageRequest.of(0, 20), 1);
        when(questaoService.listar(any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/questoes")
                        .header("Authorization", bearer("ALUNO_EM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].enunciado").value("Quanto é 2 + 2?"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /questoes sem token retorna 403")
    void listar_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/questoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /questoes com filtros retorna 200")
    void listar_comFiltros_retorna200() throws Exception {
        var page = new PageImpl<QuestaoResponse>(List.of(), PageRequest.of(0, 10), 0);
        when(questaoService.listar(any(), eq("FACIL"), any(), any())).thenReturn(page);

        mockMvc.perform(get("/questoes?dificuldade=FACIL&size=10")
                        .header("Authorization", bearer("ALUNO_EM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── GET /questoes/{id} ────────────────────────────────────

    @Test
    @DisplayName("GET /questoes/{id} retorna 200 quando questão existe")
    void buscar_retorna200() throws Exception {
        QuestaoResponse q = questaoFake();
        when(questaoService.buscarPorId(q.id())).thenReturn(q);

        mockMvc.perform(get("/questoes/" + q.id())
                        .header("Authorization", bearer("ALUNO_EM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disciplina").value("Matemática"));
    }

    // ── GET /disciplinas ──────────────────────────────────────

    @Test
    @DisplayName("GET /disciplinas retorna lista de disciplinas")
    void listarDisciplinas_retorna200() throws Exception {
        when(questaoService.listarDisciplinas()).thenReturn(List.of(
                new DisciplinaDto(UUID.randomUUID(), "Matemática"),
                new DisciplinaDto(UUID.randomUUID(), "Física")
        ));

        mockMvc.perform(get("/disciplinas")
                        .header("Authorization", bearer("ALUNO_EM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── GET /questoes/stats ───────────────────────────────────

    @Test
    @DisplayName("GET /questoes/stats retorna total de questões")
    void stats_retorna200() throws Exception {
        when(questaoService.total()).thenReturn(2740L);

        mockMvc.perform(get("/questoes/stats")
                        .header("Authorization", bearer("ALUNO_EM")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2740));
    }

    // ── POST /questoes/importar ───────────────────────────────

    @Test
    @DisplayName("POST /questoes/importar com ADMIN_SEED retorna 202")
    void importar_adminSeed_retorna202() throws Exception {
        when(questaoService.total()).thenReturn(0L);

        mockMvc.perform(post("/questoes/importar")
                        .header("Authorization", bearer("ADMIN_SEED"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    @DisplayName("POST /questoes/importar com ALUNO retorna 403")
    void importar_aluno_retorna403() throws Exception {
        mockMvc.perform(post("/questoes/importar")
                        .header("Authorization", bearer("ALUNO_EM"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /questoes/importar sem token retorna 403")
    void importar_semToken_retorna403() throws Exception {
        mockMvc.perform(post("/questoes/importar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}

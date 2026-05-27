package br.gov.seed.simulados.controller;

import br.gov.seed.simulados.dto.ResultadoResponse;
import br.gov.seed.simulados.dto.SimuladoResponse;
import br.gov.seed.simulados.dto.TentativaResponse;
import br.gov.seed.simulados.model.SessaoSimulado;
import br.gov.seed.simulados.service.SimuladoService;
import br.gov.seed.simulados.service.TempoSimuladoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = SimuladoController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
class SimuladoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SimuladoService simuladoService;

    @MockitoBean
    private TempoSimuladoService tempoSimuladoService;

    private UUID simuladoId;
    private UUID alunoId;
    private MockHttpSession mockSession;
    private SessaoSimulado sessaoAtiva;

    @BeforeEach
    void setUp() {
        simuladoId = UUID.randomUUID();
        alunoId = UUID.randomUUID();

        sessaoAtiva = new SessaoSimulado();
        sessaoAtiva.setSimuladoId(simuladoId);
        sessaoAtiva.setAlunoId(alunoId);
        sessaoAtiva.setIniciadoEm(LocalDateTime.now().minusMinutes(10));

        mockSession = new MockHttpSession();
    }

    // ── GET /simulados ────────────────────────────────────────────────────────

    @Test
    void listar_retornaListaVazia() throws Exception {
        when(simuladoService.listar(any())).thenReturn(List.of());

        mockMvc.perform(get("/simulados"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void listar_retornaSimulados() throws Exception {
        SimuladoResponse dto = new SimuladoResponse();
        dto.setId(simuladoId);
        dto.setTitulo("Simulado ENEM 2024");
        dto.setTempoMinutos(180);
        when(simuladoService.listar(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/simulados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Simulado ENEM 2024"))
                .andExpect(jsonPath("$[0].tempoMinutos").value(180));
    }

    // ── GET /simulados/{id} ───────────────────────────────────────────────────

    @Test
    void detalhe_encontrado_retorna200() throws Exception {
        SimuladoResponse dto = new SimuladoResponse();
        dto.setId(simuladoId);
        dto.setTitulo("Simulado Matemática");
        dto.setQuestaoIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
        when(simuladoService.buscarPorId(simuladoId)).thenReturn(dto);

        mockMvc.perform(get("/simulados/{id}", simuladoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Simulado Matemática"))
                .andExpect(jsonPath("$.questaoIds").isArray());
    }

    @Test
    void detalhe_naoEncontrado_retorna404() throws Exception {
        when(simuladoService.buscarPorId(any())).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/simulados/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    // ── POST /simulados/{id}/iniciar ──────────────────────────────────────────

    @Test
    void iniciar_novaSessao_retorna201() throws Exception {
        mockMvc.perform(post("/simulados/{id}/iniciar", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.simuladoId").value(simuladoId.toString()));
    }

    @Test
    void iniciar_sessaoExistente_retorna200() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);

        mockMvc.perform(post("/simulados/{id}/iniciar", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()));
    }

    // ── GET /simulados/{id}/sessao ────────────────────────────────────────────

    @Test
    void getSessao_semSessao_retorna404() throws Exception {
        mockMvc.perform(get("/simulados/{id}/sessao", simuladoId)
                        .session(mockSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSessao_comSessao_retorna200() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);

        mockMvc.perform(get("/simulados/{id}/sessao", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.simuladoId").value(simuladoId.toString()));
    }

    // ── PUT /simulados/{id}/responder ─────────────────────────────────────────

    @Test
    void responder_semSessao_retorna404() throws Exception {
        mockMvc.perform(put("/simulados/{id}/responder", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("questaoIndex", "0", "alternativaId", UUID.randomUUID().toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void responder_donoErrado_retorna403() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);
        UUID outroAluno = UUID.randomUUID();

        mockMvc.perform(put("/simulados/{id}/responder", simuladoId)
                        .requestAttr("userID", outroAluno.toString())
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("questaoIndex", "0", "alternativaId", UUID.randomUUID().toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void responder_comSessaoValida_retorna200() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);

        mockMvc.perform(put("/simulados/{id}/responder", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("questaoIndex", "0", "alternativaId", UUID.randomUUID().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respostas").exists());
    }

    // ── POST /simulados/{id}/finalizar ────────────────────────────────────────

    @Test
    void finalizar_semSessao_retorna404() throws Exception {
        mockMvc.perform(post("/simulados/{id}/finalizar", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void finalizar_donoErrado_retorna403() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);

        mockMvc.perform(post("/simulados/{id}/finalizar", simuladoId)
                        .requestAttr("userID", UUID.randomUUID().toString())
                        .session(mockSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void finalizar_comSessaoValida_retorna200() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);

        ResultadoResponse resultado = new ResultadoResponse();
        resultado.setTentativaId(UUID.randomUUID());
        resultado.setSimuladoId(simuladoId);
        resultado.setNota(BigDecimal.valueOf(8.00));
        resultado.setTotalQuestoes(10);
        resultado.setAcertos(8);

        when(simuladoService.finalizar(eq(simuladoId), eq(alunoId), any(SessaoSimulado.class)))
                .thenReturn(resultado);

        mockMvc.perform(post("/simulados/{id}/finalizar", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(8.0))
                .andExpect(jsonPath("$.acertos").value(8))
                .andExpect(jsonPath("$.totalQuestoes").value(10));
    }

    // ── POST /simulados/{id}/encerrar ─────────────────────────────────────────

    @Test
    void encerrar_semSessao_chama_servico() throws Exception {
        doNothing().when(tempoSimuladoService).encerrarPorTempo(simuladoId);

        mockMvc.perform(post("/simulados/{id}/encerrar", simuladoId)
                        .requestAttr("userID", alunoId.toString())
                        .session(mockSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Simulado encerrado"));

        verify(tempoSimuladoService).encerrarPorTempo(simuladoId);
    }

    @Test
    void encerrar_donoErrado_retorna403() throws Exception {
        mockSession.setAttribute("sessao_simulado_" + simuladoId, sessaoAtiva);

        mockMvc.perform(post("/simulados/{id}/encerrar", simuladoId)
                        .requestAttr("userID", UUID.randomUUID().toString())
                        .session(mockSession))
                .andExpect(status().isForbidden());

        verify(tempoSimuladoService, never()).encerrarPorTempo(any());
    }

    // ── GET /simulados/{id}/resultado ─────────────────────────────────────────

    @Test
    void resultado_encontrado_retorna200() throws Exception {
        ResultadoResponse resultado = new ResultadoResponse();
        resultado.setTentativaId(UUID.randomUUID());
        resultado.setSimuladoId(simuladoId);
        resultado.setNota(BigDecimal.valueOf(9.50));
        resultado.setTotalQuestoes(20);
        resultado.setAcertos(19);

        when(simuladoService.resultado(eq(simuladoId), eq(alunoId))).thenReturn(resultado);

        mockMvc.perform(get("/simulados/{id}/resultado", simuladoId)
                        .requestAttr("userID", alunoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(9.5))
                .andExpect(jsonPath("$.acertos").value(19));
    }

    @Test
    void resultado_naoEncontrado_retorna404() throws Exception {
        when(simuladoService.resultado(any(), any())).thenThrow(new RuntimeException("not found"));

        mockMvc.perform(get("/simulados/{id}/resultado", UUID.randomUUID())
                        .requestAttr("userID", alunoId.toString()))
                .andExpect(status().isNotFound());
    }

    // ── GET /simulados/minhas-tentativas ──────────────────────────────────────

    @Test
    void minhasTentativas_retornaLista() throws Exception {
        TentativaResponse t = new TentativaResponse();
        t.setId(UUID.randomUUID());
        t.setSimuladoId(simuladoId);
        t.setAlunoId(alunoId);
        t.setNota(BigDecimal.valueOf(7.50));

        when(simuladoService.minhasTentativas(alunoId)).thenReturn(List.of(t));

        mockMvc.perform(get("/simulados/minhas-tentativas")
                        .requestAttr("userID", alunoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nota").value(7.5));
    }
}

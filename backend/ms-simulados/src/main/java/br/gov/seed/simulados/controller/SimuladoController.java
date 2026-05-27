package br.gov.seed.simulados.controller;

import br.gov.seed.simulados.dto.CriarSimuladoRequest;
import br.gov.seed.simulados.dto.ResultadoResponse;
import br.gov.seed.simulados.dto.SimuladoResponse;
import br.gov.seed.simulados.dto.TentativaResponse;
import br.gov.seed.simulados.model.SessaoSimulado;
import br.gov.seed.simulados.model.SimuladoQuestao;
import br.gov.seed.simulados.service.SimuladoService;
import br.gov.seed.simulados.service.TempoSimuladoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/simulados")
@RequiredArgsConstructor
@Tag(name = "Simulados", description = "Listagem, sessões de prova, respostas e resultados")
@SecurityRequirement(name = "BearerAuth")
public class SimuladoController {

    private final SimuladoService simuladoService;
    private final TempoSimuladoService tempoSimuladoService;

    // ── Listagem ─────────────────────────────────────────────────────────────

    @Operation(
        summary = "Lista simulados disponíveis",
        description = "Retorna todos os simulados sem dataFim ou com dataFim futura, ordenados por data de criação. " +
                      "Se `turmaId` for informado, filtra pelos simulados da turma."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<SimuladoResponse>> listar(
            @Parameter(description = "UUID da turma (opcional)") @RequestParam(required = false) UUID turmaId) {
        return ResponseEntity.ok(simuladoService.listar(turmaId));
    }

    @Operation(
        summary = "Detalhe de um simulado",
        description = "Retorna os dados do simulado incluindo a lista ordenada de IDs de questões."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulado encontrado"),
        @ApiResponse(responseCode = "404", description = "Simulado não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SimuladoResponse> detalhe(
            @Parameter(description = "UUID do simulado", required = true) @PathVariable UUID id) {
        try {
            return ResponseEntity.ok(simuladoService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Sessão ───────────────────────────────────────────────────────────────

    @Operation(
        summary = "Inicia a sessão de um simulado",
        description = """
            Cria uma sessão Redis para o aluno autenticado neste simulado (TTL 30 min).
            Se já existe sessão ativa para este simulado, retorna 200 com o estado atual (idempotente).
            Nova sessão retorna 201.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Sessão criada com sucesso"),
        @ApiResponse(responseCode = "200", description = "Sessão já existia — retorna estado atual")
    })
    @PostMapping("/{id}/iniciar")
    public ResponseEntity<SessaoSimulado> iniciar(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            HttpServletRequest request,
            HttpSession session) {

        UUID alunoId = extrairAlunoId(request);
        String sessionKey = "sessao_simulado_" + id;

        SessaoSimulado sessaoExistente = (SessaoSimulado) session.getAttribute(sessionKey);
        if (sessaoExistente != null) {
            return ResponseEntity.ok(sessaoExistente);
        }

        SessaoSimulado sessao = new SessaoSimulado();
        sessao.setSimuladoId(id);
        sessao.setAlunoId(alunoId);
        sessao.setIniciadoEm(LocalDateTime.now());
        session.setAttribute(sessionKey, sessao);
        return ResponseEntity.status(201).body(sessao);
    }

    @Operation(
        summary = "Retorna o estado atual da sessão",
        description = "Questão atual (índice) e todas as respostas registradas até o momento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sessão encontrada"),
        @ApiResponse(responseCode = "404", description = "Nenhuma sessão ativa para este simulado", content = @Content)
    })
    @GetMapping("/{id}/sessao")
    public ResponseEntity<SessaoSimulado> getSessao(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            HttpServletRequest request,
            HttpSession session) {

        SessaoSimulado sessao = (SessaoSimulado) session.getAttribute("sessao_simulado_" + id);
        if (sessao == null) {
            return ResponseEntity.notFound().build();
        }
        UUID alunoId = UUID.fromString((String) request.getAttribute("userID"));
        if (!alunoId.equals(sessao.getAlunoId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(sessao);
    }

    @Operation(
        summary = "Registra resposta para uma questão",
        description = """
            Salva ou atualiza a alternativa escolhida para o índice de questão informado.
            O corpo deve conter:
            - `questaoIndex`: índice 0-based da questão na lista do simulado
            - `alternativaId`: UUID da alternativa escolhida
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resposta registrada"),
        @ApiResponse(responseCode = "403", description = "Este simulado pertence a outro aluno", content = @Content),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada — chame /iniciar primeiro", content = @Content)
    })
    @PutMapping("/{id}/responder")
    public ResponseEntity<SessaoSimulado> responder(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "questaoIndex (int) e alternativaId (UUID)",
                required = true,
                content = @Content(schema = @Schema(example = "{\"questaoIndex\": 0, \"alternativaId\": \"uuid-da-alternativa\"}"))
            )
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpSession session) {

        SessaoSimulado sessao = (SessaoSimulado) session.getAttribute("sessao_simulado_" + id);
        if (sessao == null) {
            return ResponseEntity.notFound().build();
        }

        UUID alunoId = extrairAlunoId(request);
        if (!alunoId.equals(sessao.getAlunoId())) {
            return ResponseEntity.status(403).build();
        }

        int questaoIndex = Integer.parseInt(body.get("questaoIndex"));
        String alternativaId = body.get("alternativaId");

        sessao.getRespostas().put(questaoIndex, alternativaId);
        sessao.setQuestaoAtual(questaoIndex);
        session.setAttribute("sessao_simulado_" + id, sessao);

        return ResponseEntity.ok(sessao);
    }

    @Operation(
        summary = "Finaliza o simulado e calcula a nota",
        description = """
            Encerra a sessão, calcula a nota com base nas alternativas corretas,
            persiste a TentativaSimulado e as RespostaTentativa no banco e
            retorna o resultado completo (nota, acertos, total de questões).
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulado finalizado — resultado retornado"),
        @ApiResponse(responseCode = "403", description = "Sessão pertence a outro aluno", content = @Content),
        @ApiResponse(responseCode = "404", description = "Sessão não encontrada", content = @Content)
    })
    @PostMapping("/{id}/finalizar")
    public ResponseEntity<ResultadoResponse> finalizar(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            HttpServletRequest request,
            HttpSession session) {

        SessaoSimulado sessao = (SessaoSimulado) session.getAttribute("sessao_simulado_" + id);
        if (sessao == null) {
            return ResponseEntity.notFound().build();
        }

        UUID alunoId = extrairAlunoId(request);
        if (!alunoId.equals(sessao.getAlunoId())) {
            return ResponseEntity.status(403).build();
        }

        ResultadoResponse resultado = simuladoService.finalizar(id, alunoId, sessao);
        session.removeAttribute("sessao_simulado_" + id);
        return ResponseEntity.ok(resultado);
    }

    @Operation(
        summary = "Encerra o simulado por tempo esgotado",
        description = "Registra um evento TEMPO_ESGOTADO no Outbox (enviado ao Kafka pelo scheduler) e limpa a sessão."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Simulado encerrado"),
        @ApiResponse(responseCode = "403", description = "Sessão pertence a outro aluno", content = @Content)
    })
    @PostMapping("/{id}/encerrar")
    public ResponseEntity<Map<String, String>> encerrar(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            HttpServletRequest request,
            HttpSession session) {

        UUID alunoId = extrairAlunoId(request);
        SessaoSimulado sessao = (SessaoSimulado) session.getAttribute("sessao_simulado_" + id);

        if (sessao != null && !alunoId.equals(sessao.getAlunoId())) {
            return ResponseEntity.status(403).build();
        }

        tempoSimuladoService.encerrarPorTempo(id);
        session.removeAttribute("sessao_simulado_" + id);
        return ResponseEntity.ok(Map.of("status", "Simulado encerrado", "simuladoId", id.toString()));
    }

    // ── Resultados ───────────────────────────────────────────────────────────

    @Operation(
        summary = "Resultado da última tentativa",
        description = "Retorna nota, acertos e total de questões da tentativa mais recente do aluno neste simulado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Resultado encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhuma tentativa finalizada para este simulado", content = @Content)
    })
    @GetMapping("/{id}/resultado")
    public ResponseEntity<ResultadoResponse> resultado(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            HttpServletRequest request) {

        UUID alunoId = extrairAlunoId(request);
        try {
            return ResponseEntity.ok(simuladoService.resultado(id, alunoId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Histórico de tentativas do aluno",
        description = "Retorna todas as tentativas finalizadas do aluno autenticado em qualquer simulado, ordenadas da mais recente."
    )
    @ApiResponse(responseCode = "200", description = "Lista de tentativas")
    @GetMapping("/minhas-tentativas")
    public ResponseEntity<List<TentativaResponse>> minhasTentativas(HttpServletRequest request) {
        UUID alunoId = extrairAlunoId(request);
        return ResponseEntity.ok(simuladoService.minhasTentativas(alunoId));
    }

    // ── CRUD professor ───────────────────────────────────────────────────────

    @Operation(
        summary = "Cria um simulado (professor)",
        description = "Professor, ADMIN_ESCOLA ou ADMIN_SEED criam simulados. O professorId é extraído do token JWT."
    )
    @ApiResponse(responseCode = "201", description = "Simulado criado")
    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<SimuladoResponse> criar(
            @Valid @RequestBody CriarSimuladoRequest request,
            HttpServletRequest httpRequest) {
        UUID professorId = extrairAlunoId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(simuladoService.criar(request, professorId));
    }

    @Operation(summary = "Atualiza um simulado")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<SimuladoResponse> atualizar(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            @RequestBody CriarSimuladoRequest request) {
        try {
            return ResponseEntity.ok(simuladoService.atualizar(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Desativa um simulado", description = "Define dataFim como passada, tornando o simulado indisponível.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<Void> desativar(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id) {
        try {
            simuladoService.desativar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Simulados do professor autenticado")
    @GetMapping("/professor/meus")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<List<SimuladoResponse>> meusPorProfessor(HttpServletRequest request) {
        UUID professorId = extrairAlunoId(request);
        return ResponseEntity.ok(simuladoService.meusPorProfessor(professorId));
    }

    // ── Questões do simulado ─────────────────────────────────────────────────

    @Operation(summary = "Lista questões de um simulado")
    @GetMapping("/{id}/questoes")
    public ResponseEntity<List<SimuladoQuestao>> listarQuestoes(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id) {
        try {
            return ResponseEntity.ok(simuladoService.listarQuestoes(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Adiciona questão ao simulado")
    @PostMapping("/{id}/questoes")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<SimuladoQuestao> adicionarQuestao(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        UUID questaoId = UUID.fromString(body.get("questaoId"));
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(simuladoService.adicionarQuestao(id, questaoId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remove questão do simulado")
    @DeleteMapping("/{id}/questoes/{questaoId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<Void> removerQuestao(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id,
            @Parameter(description = "UUID da questão") @PathVariable UUID questaoId) {
        try {
            simuladoService.removerQuestao(id, questaoId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Gabarito ─────────────────────────────────────────────────────────────

    @Operation(
        summary = "Gabarito do simulado",
        description = "Retorna a alternativa correta de cada questão. Disponível após o período do simulado."
    )
    @GetMapping("/{id}/gabarito")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<List<Map<String, Object>>> gabarito(
            @Parameter(description = "UUID do simulado") @PathVariable UUID id) {
        try {
            return ResponseEntity.ok(simuladoService.gabarito(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private UUID extrairAlunoId(HttpServletRequest request) {
        Object attr = request.getAttribute("userID");
        if (attr == null) {
            throw new IllegalStateException("Atributo userID ausente — JWT inválido ou filtro não executado");
        }
        return UUID.fromString(attr.toString());
    }
}

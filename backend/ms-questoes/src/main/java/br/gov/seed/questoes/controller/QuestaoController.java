package br.gov.seed.questoes.controller;

import br.gov.seed.questoes.dto.AssuntoDto;
import br.gov.seed.questoes.dto.CriarQuestaoRequest;
import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.service.EnemImporterService;
import br.gov.seed.questoes.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Questões", description = "Banco de questões do ENEM e disciplinas")
public class QuestaoController {

    private final QuestaoService questaoService;
    private final EnemImporterService enemImporterService;

    // ── Leitura ──────────────────────────────────────────────────────────────

    @Operation(
        summary = "Lista questões com filtros e paginação",
        description = "Retorna página de questões ativas. Filtre por disciplina, dificuldade e tipo.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/questoes")
    public ResponseEntity<Page<QuestaoResponse>> listar(
            @Parameter(description = "UUID da disciplina") @RequestParam(required = false) UUID disciplinaId,
            @Parameter(description = "FACIL | MEDIO | DIFICIL") @RequestParam(required = false) String dificuldade,
            @Parameter(description = "MULTIPLA_ESCOLHA | VERDADEIRO_FALSO") @RequestParam(required = false) String tipo,
            @Parameter(description = "FUNDAMENTAL | MEDIO | PROFISSIONALIZANTE") @RequestParam(required = false) String nivelEnsino,
            @Parameter(description = "MEDIO | EJA | PROFISSIONALIZANTE — filtra automaticamente os níveis permitidos para a modalidade") @RequestParam(required = false) String modalidadeTurma,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("criadoEm").descending());
        boolean incluirGabarito = ehProfessorOuAdmin(httpRequest);
        return ResponseEntity.ok(questaoService.listar(disciplinaId, dificuldade, tipo, nivelEnsino, modalidadeTurma, pageable, incluirGabarito));
    }

    @Operation(
        summary = "Busca questão por ID",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/questoes/{id}")
    public ResponseEntity<QuestaoResponse> buscar(@PathVariable UUID id, HttpServletRequest request) {
        return ResponseEntity.ok(questaoService.buscarPorId(id, ehProfessorOuAdmin(request)));
    }

    @Operation(
        summary = "Minhas questões — criadas pelo professor autenticado",
        description = "Retorna questões criadas pelo professor logado, ordenadas por data de criação.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/questoes/minhas")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<Page<QuestaoResponse>> minhas(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID professorId = extrairUserId(request);
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("criadoEm").descending());
        // Professor sempre vê o gabarito das próprias questões
        return ResponseEntity.ok(questaoService.minhas(professorId, pageable, true));
    }

    // ── Escrita ───────────────────────────────────────────────────────────────

    @Operation(
        summary = "Criar questão",
        description = "Professor, ADMIN_ESCOLA ou ADMIN_SEED criam questões. Deve haver exatamente 1 alternativa correta.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/questoes")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<QuestaoResponse> criar(
            @Valid @RequestBody CriarQuestaoRequest request,
            HttpServletRequest httpRequest) {
        UUID professorId = extrairUserId(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(questaoService.criar(request, professorId));
    }

    @Operation(
        summary = "Atualizar questão",
        description = "Atualiza enunciado, tipo, dificuldade, tipoUso, nivelEnsino ou disciplina.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @PutMapping("/questoes/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<QuestaoResponse> atualizar(
            @PathVariable UUID id,
            @RequestBody CriarQuestaoRequest request,
            HttpServletRequest httpRequest) {
        UUID professorId = extrairUserId(httpRequest);
        return ResponseEntity.ok(questaoService.atualizar(id, request, professorId));
    }

    @Operation(
        summary = "Inativar questão",
        description = "Marca a questão como inativa (soft delete). Apenas ADMIN_SEED pode inativar qualquer questão.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @DeleteMapping("/questoes/{id}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<Void> inativar(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        UUID professorId = extrairUserId(httpRequest);
        questaoService.inativar(id, professorId);
        return ResponseEntity.noContent().build();
    }

    // ── Disciplinas e Assuntos ────────────────────────────────────────────────

    @Operation(
        summary = "Lista todas as disciplinas cadastradas",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/disciplinas")
    public ResponseEntity<List<DisciplinaDto>> listarDisciplinas() {
        return ResponseEntity.ok(questaoService.listarDisciplinas());
    }

    @Operation(
        summary = "Lista assuntos",
        description = "Retorna assuntos, opcionalmente filtrados por disciplina.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/assuntos")
    public ResponseEntity<List<AssuntoDto>> listarAssuntos(
            @RequestParam(required = false) UUID disciplinaId) {
        return ResponseEntity.ok(questaoService.listarAssuntos(disciplinaId));
    }

    @Operation(
        summary = "Criar assunto",
        description = "Cria um novo assunto vinculado a uma disciplina. Requer PROFESSOR ou admin.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/assuntos")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<AssuntoDto> criarAssunto(
            @RequestBody Map<String, String> body) {
        String nome = body.get("nome");
        String disciplinaIdStr = body.get("disciplinaId");
        if (nome == null || nome.isBlank() || disciplinaIdStr == null) {
            return ResponseEntity.badRequest().build();
        }
        UUID disciplinaId = UUID.fromString(disciplinaIdStr);
        return ResponseEntity.status(HttpStatus.CREATED).body(questaoService.criarAssunto(nome, disciplinaId));
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    @Operation(
        summary = "Retorna total de questões no banco",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/questoes/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of("total", questaoService.total()));
    }

    @Operation(
        summary = "Dispara importação das questões do ENEM (somente ADMIN_SEED)",
        description = "Executa a importação de forma assíncrona. Acompanhe o progresso nos logs.",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @PostMapping("/questoes/importar")
    @PreAuthorize("hasRole('ADMIN_SEED')")
    public ResponseEntity<Map<String, String>> importar() {
        long atual = questaoService.total();
        if (atual > 0) {
            log.info("Importação manual solicitada mas banco já possui {} questões.", atual);
        }
        CompletableFuture.runAsync(enemImporterService::importarTudo);
        return ResponseEntity.accepted().body(
                Map.of("status", "Importação iniciada em background. Acompanhe os logs do serviço.")
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UUID extrairUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userID");
        if (attr == null) {
            throw new IllegalStateException("Atributo userID ausente — JWT inválido ou filtro não executado");
        }
        return UUID.fromString(attr.toString());
    }

    /** Retorna true se o chamador tem perfil de PROFESSOR, ADMIN_ESCOLA ou ADMIN_SEED. */
    private boolean ehProfessorOuAdmin(HttpServletRequest request) {
        Object perfil = request.getAttribute("perfil");
        if (perfil == null) return false;
        String p = perfil.toString();
        return p.equals("PROFESSOR") || p.equals("ADMIN_ESCOLA") || p.equals("ADMIN_SEED");
    }
}

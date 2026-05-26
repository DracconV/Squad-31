package br.gov.seed.questoes.controller;

import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.service.EnemImporterService;
import br.gov.seed.questoes.service.QuestaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("criadoEm").descending());
        return ResponseEntity.ok(questaoService.listar(disciplinaId, dificuldade, tipo, nivelEnsino, modalidadeTurma, pageable));
    }

    @Operation(
        summary = "Busca questão por ID",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/questoes/{id}")
    public ResponseEntity<QuestaoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(questaoService.buscarPorId(id));
    }

    @Operation(
        summary = "Lista todas as disciplinas cadastradas",
        security = @SecurityRequirement(name = "BearerAuth")
    )
    @GetMapping("/disciplinas")
    public ResponseEntity<List<DisciplinaDto>> listarDisciplinas() {
        return ResponseEntity.ok(questaoService.listarDisciplinas());
    }

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
}

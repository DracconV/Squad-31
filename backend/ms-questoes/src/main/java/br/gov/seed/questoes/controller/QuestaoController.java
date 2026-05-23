package br.gov.seed.questoes.controller;

import br.gov.seed.questoes.dto.DisciplinaDto;
import br.gov.seed.questoes.dto.QuestaoResponse;
import br.gov.seed.questoes.service.EnemImporterService;
import br.gov.seed.questoes.service.QuestaoService;
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
public class QuestaoController {

    private final QuestaoService questaoService;
    private final EnemImporterService enemImporterService;

    /**
     * GET /questoes?disciplinaId=&dificuldade=&tipo=&page=0&size=20
     */
    @GetMapping("/questoes")
    public ResponseEntity<Page<QuestaoResponse>> listar(
            @RequestParam(required = false) UUID disciplinaId,
            @RequestParam(required = false) String dificuldade,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("criadoEm").descending());
        return ResponseEntity.ok(questaoService.listar(disciplinaId, dificuldade, tipo, pageable));
    }

    /**
     * GET /questoes/{id}
     */
    @GetMapping("/questoes/{id}")
    public ResponseEntity<QuestaoResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(questaoService.buscarPorId(id));
    }

    /**
     * GET /disciplinas — lista todas as disciplinas cadastradas
     */
    @GetMapping("/disciplinas")
    public ResponseEntity<List<DisciplinaDto>> listarDisciplinas() {
        return ResponseEntity.ok(questaoService.listarDisciplinas());
    }

    /**
     * GET /questoes/stats — total de questões no banco
     */
    @GetMapping("/questoes/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(Map.of("total", questaoService.total()));
    }

    /**
     * POST /questoes/importar — dispara importação manual (somente ADMIN_SEED)
     */
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

package br.gov.seed.autenticacao.controller;

import br.gov.seed.autenticacao.dto.FrequenciaDTO;
import br.gov.seed.autenticacao.service.FrequenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/frequencia")
@RequiredArgsConstructor
@Tag(name = "Frequencia", description = "Controle de presença e faltas")
@SecurityRequirement(name = "bearerAuth")
public class FrequenciaController {

    private final FrequenciaService frequenciaService;

    @GetMapping("/aluno/{alunoId}/resumo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resumo de frequência do aluno", description = "Presença/faltas por disciplina.")
    public ResponseEntity<List<FrequenciaDTO.ResumoItem>> resumoAluno(@PathVariable UUID alunoId) {
        return ResponseEntity.ok(frequenciaService.resumoAluno(alunoId));
    }

    @GetMapping("/turma/{turmaId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Frequência agregada da turma", description = "Presença média e situação de cada aluno.")
    public ResponseEntity<FrequenciaDTO.TurmaResponse> turma(@PathVariable UUID turmaId) {
        return ResponseEntity.ok(frequenciaService.turma(turmaId));
    }

    @PostMapping("/registrar")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Registrar/atualizar frequência", description = "Upsert de aulas e faltas de um aluno numa disciplina.")
    public ResponseEntity<FrequenciaDTO.ResumoItem> registrar(
            @Valid @RequestBody FrequenciaDTO.RegistrarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(frequenciaService.registrar(request));
    }
}

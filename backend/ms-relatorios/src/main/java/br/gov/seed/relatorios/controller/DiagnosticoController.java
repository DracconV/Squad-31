package br.gov.seed.relatorios.controller;

import br.gov.seed.relatorios.dto.DiagnosticoDTO;
import br.gov.seed.relatorios.service.DiagnosticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/diagnostico")
@RequiredArgsConstructor
@Tag(name = "Diagnóstico", description = "Diagnóstico adaptativo de desempenho por disciplina")
@SecurityRequirement(name = "Bearer Authentication")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    @GetMapping("/alunos/{alunoId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(
        summary = "Busca diagnóstico de um aluno",
        description = "Retorna o diagnóstico mais recente salvo. Se não houver registros, calcula on-the-fly a partir do histórico."
    )
    public ResponseEntity<DiagnosticoDTO.DiagnosticoResponse> buscar(@PathVariable UUID alunoId) {
        return ResponseEntity.ok(diagnosticoService.buscar(alunoId));
    }

    @PostMapping("/alunos/{alunoId}/gerar")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(
        summary = "Gera e persiste novo diagnóstico",
        description = "Recalcula o diagnóstico do aluno a partir do histórico completo de questões respondidas, " +
                      "persiste na tabela diagnostico_aluno e retorna o resultado."
    )
    public ResponseEntity<DiagnosticoDTO.DiagnosticoResponse> gerar(@PathVariable UUID alunoId) {
        return ResponseEntity.ok(diagnosticoService.gerarEPersistir(alunoId));
    }
}

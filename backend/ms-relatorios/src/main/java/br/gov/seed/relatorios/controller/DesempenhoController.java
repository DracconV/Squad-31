package br.gov.seed.relatorios.controller;

import br.gov.seed.relatorios.dto.DesempenhoDTO.*;
import br.gov.seed.relatorios.service.DesempenhoAlunoService;
import br.gov.seed.relatorios.service.DesempenhoTurmaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/desempenho")
@RequiredArgsConstructor
@Tag(name = "Desempenho", description = "Endpoints de desempenho de alunos e turmas")
@SecurityRequirement(name = "Bearer Authentication")
public class DesempenhoController {

    private final DesempenhoAlunoService alunoService;
    private final DesempenhoTurmaService turmaService;

    @GetMapping("/aluno/{alunoId}/disciplina/{disciplina}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obter desempenho do aluno em disciplina")
    public ResponseEntity<DesempenhoAlunoResponse> obterDesempenhoAluno(
        @PathVariable UUID alunoId,
        @PathVariable String disciplina
    ) {
        return ResponseEntity.ok(alunoService.obterDesempenho(alunoId, disciplina));
    }

    @GetMapping("/aluno/{alunoId}/historico")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obter histórico de desempenho do aluno")
    public ResponseEntity<List<DesempenhoAlunoResponse>> obterHistoricoAluno(
        @PathVariable UUID alunoId
    ) {
        return ResponseEntity.ok(alunoService.obterHistoricoAluno(alunoId));
    }

    @GetMapping("/turma/{turmaId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Obter desempenho agregado da turma")
    public ResponseEntity<DesempenhoTurmaResponse> obterDesempenhoTurma(
        @PathVariable UUID turmaId
    ) {
        return ResponseEntity.ok(turmaService.obterDesempenhoTurma(turmaId));
    }

    @GetMapping("/turma/{turmaId}/alunos-baixo-desempenho")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Listar alunos com baixo desempenho (nota < 6.0)")
    public ResponseEntity<List<DesempenhoAlunoResponse>> obterAlunosComBaixoDesempenho(
        @PathVariable UUID turmaId
    ) {
        return ResponseEntity.ok(alunoService.obterAlunosComBaixoDesempenho(turmaId));
    }
}


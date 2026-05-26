package br.gov.seed.autenticacao.controller;

import br.gov.seed.autenticacao.dto.TurmaDTO;
import br.gov.seed.autenticacao.service.TurmaService;
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
@RequestMapping("/turmas")
@RequiredArgsConstructor
@Tag(name = "Turmas", description = "Gerenciamento de turmas escolares")
@SecurityRequirement(name = "bearerAuth")
public class TurmaController {

    private final TurmaService turmaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Criar turma", description = "Professores e administradores podem criar turmas.")
    public ResponseEntity<TurmaDTO.Response> criar(@Valid @RequestBody TurmaDTO.CriarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.criar(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar turmas", description = "Lista turmas ativas. Filtra por instituicao se informado.")
    public ResponseEntity<List<TurmaDTO.Response>> listar(
            @RequestParam(required = false) UUID instituicaoId) {
        if (instituicaoId != null) {
            return ResponseEntity.ok(turmaService.listarPorInstituicao(instituicaoId));
        }
        return ResponseEntity.ok(turmaService.listarTodas());
    }

    @PostMapping("/{turmaId}/alunos")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Adicionar aluno a turma")
    public ResponseEntity<Void> adicionarAluno(
            @PathVariable UUID turmaId,
            @Valid @RequestBody TurmaDTO.AdicionarAlunoRequest request) {
        turmaService.adicionarAluno(turmaId, request.alunoId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{turmaId}/alunos/{alunoId}")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(summary = "Remover aluno da turma")
    public ResponseEntity<Void> removerAluno(
            @PathVariable UUID turmaId,
            @PathVariable UUID alunoId) {
        turmaService.removerAluno(turmaId, alunoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{turmaId}/alunos")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar alunos da turma")
    public ResponseEntity<List<TurmaDTO.AlunoResponse>> listarAlunos(@PathVariable UUID turmaId) {
        return ResponseEntity.ok(turmaService.listarAlunos(turmaId));
    }
}

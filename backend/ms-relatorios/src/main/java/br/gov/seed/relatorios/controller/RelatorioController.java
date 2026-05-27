package br.gov.seed.relatorios.controller;

import br.gov.seed.relatorios.dto.RelatorioDTO;
import br.gov.seed.relatorios.service.RelatorioService;
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
@RequestMapping("/relatorios")
@RequiredArgsConstructor
@Tag(name = "Relatórios", description = "Indicadores gerenciais da rede SEED Educa")
@SecurityRequirement(name = "Bearer Authentication")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/rede/resumo")
    @PreAuthorize("hasRole('ADMIN_SEED')")
    @Operation(
        summary = "Resumo geral da rede",
        description = "Total de instituições, turmas, alunos, professores e média geral de notas."
    )
    public ResponseEntity<RelatorioDTO.ResumoRede> resumoRede() {
        return ResponseEntity.ok(relatorioService.resumoRede());
    }

    @GetMapping("/escola/{id}/resumo")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(
        summary = "Resumo de uma escola",
        description = "Total de turmas, alunos e média de nota para a instituição informada."
    )
    public ResponseEntity<RelatorioDTO.ResumoEscola> resumoEscola(@PathVariable UUID id) {
        return ResponseEntity.ok(relatorioService.resumoEscola(id));
    }

    @GetMapping("/simulados/{id}/resultado")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(
        summary = "Resultado geral de um simulado",
        description = "Total de tentativas, nota média e taxa de acerto em toda a rede para o simulado."
    )
    public ResponseEntity<RelatorioDTO.ResultadoSimuladoRede> resultadoSimulado(@PathVariable UUID id) {
        return ResponseEntity.ok(relatorioService.resultadoSimulado(id));
    }

    @GetMapping("/alunos/primeiro-acesso")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(
        summary = "Alunos que ainda não fizeram login",
        description = "Lista usuários com primeiro_acesso = true (senha temporária não alterada)."
    )
    public ResponseEntity<RelatorioDTO.AlunosPrimeiroAcesso> alunosPrimeiroAcesso() {
        return ResponseEntity.ok(relatorioService.alunosPrimeiroAcesso());
    }

    @GetMapping("/cursos/taxa-conclusao")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED')")
    @Operation(
        summary = "Taxa de conclusão por curso",
        description = "Para cada curso, retorna total de inscritos, concluídos e percentual de conclusão."
    )
    public ResponseEntity<List<RelatorioDTO.TaxaConclusaoCurso>> taxaConclusao() {
        return ResponseEntity.ok(relatorioService.taxaConclusaoCursos());
    }

    @GetMapping("/auditoria")
    @PreAuthorize("hasRole('ADMIN_SEED')")
    @Operation(
        summary = "Log de auditoria",
        description = "Retorna os registros mais recentes da tabela audit_log. Máx 500 registros."
    )
    public ResponseEntity<RelatorioDTO.RelatorioAuditoria> auditoria(
            @RequestParam(defaultValue = "100") int limite) {
        return ResponseEntity.ok(relatorioService.auditoria(limite));
    }

    @GetMapping("/seed/painel-macro")
    @PreAuthorize("hasRole('ADMIN_SEED')")
    @Operation(
        summary = "Painel macro por município",
        description = "Agrega escolas, alunos, professores e média de notas de simulados por município. " +
                      "Exclusivo para ADMIN_SEED."
    )
    public ResponseEntity<RelatorioDTO.PainelMacro> painelMacro() {
        return ResponseEntity.ok(relatorioService.painelMacro());
    }
}

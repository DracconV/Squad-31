package br.gov.seed.autenticacao.controller;

import br.gov.seed.autenticacao.dto.AgendamentoDTO;
import br.gov.seed.autenticacao.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Agendamento de provas praticas")
@SecurityRequirement(name = "bearerAuth")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @GetMapping("/slots")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar slots disponiveis", description = "Retorna slots com vagas abertas para agendamento de prova pratica.")
    public ResponseEntity<List<AgendamentoDTO.SlotResponse>> listarSlots() {
        return ResponseEntity.ok(agendamentoService.listarSlotsDisponiveis());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ALUNO_EM', 'ALUNO_EJA', 'ALUNO_PROF')")
    @Operation(summary = "Agendar prova", description = "Aluno agenda uma prova pratica em um slot disponivel.")
    public ResponseEntity<AgendamentoDTO.Response> agendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AgendamentoDTO.CriarRequest request) {
        UUID alunoId = extrairId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.agendar(alunoId, request));
    }

    @GetMapping("/meus")
    @PreAuthorize("hasAnyRole('ALUNO_EM', 'ALUNO_EJA', 'ALUNO_PROF')")
    @Operation(summary = "Meus agendamentos", description = "Lista os agendamentos do aluno autenticado.")
    public ResponseEntity<List<AgendamentoDTO.Response>> meusAgendamentos(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID alunoId = extrairId(userDetails);
        return ResponseEntity.ok(agendamentoService.meusAgendamentos(alunoId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO_EM', 'ALUNO_EJA', 'ALUNO_PROF')")
    @Operation(summary = "Reagendar prova", description = "Troca o slot do agendamento sem cancelar e recriar.")
    public ResponseEntity<AgendamentoDTO.Response> reagendar(
            @PathVariable UUID id,
            @Valid @RequestBody AgendamentoDTO.ReagendarRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID alunoId = extrairId(userDetails);
        return ResponseEntity.ok(agendamentoService.reagendar(id, alunoId, request.novoSlotId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUNO_EM', 'ALUNO_EJA', 'ALUNO_PROF')")
    @Operation(summary = "Cancelar agendamento")
    public ResponseEntity<Void> cancelar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID alunoId = extrairId(userDetails);
        agendamentoService.cancelar(id, alunoId);
        return ResponseEntity.noContent().build();
    }

    private UUID extrairId(UserDetails userDetails) {
        if (userDetails instanceof br.gov.seed.autenticacao.entity.Usuario usuario) {
            return usuario.getId();
        }
        throw new IllegalStateException("Usuario nao identificado");
    }
}

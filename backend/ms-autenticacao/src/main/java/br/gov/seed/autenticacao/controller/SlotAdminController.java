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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin — Slots e Agendamentos", description = "Gerenciamento de slots de prova pratica e visao admin de agendamentos")
@SecurityRequirement(name = "bearerAuth")
public class SlotAdminController {

    private final AgendamentoService agendamentoService;

    // ── Slots ────────────────────────────────────────────────────────────────

    @GetMapping("/slots")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Listar todos os slots", description = "Lista todos os slots cadastrados, incluindo os sem vagas.")
    public ResponseEntity<List<AgendamentoDTO.SlotResponse>> listarSlots() {
        return ResponseEntity.ok(agendamentoService.listarTodosSlots());
    }

    @PostMapping("/slots")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Criar slot de prova pratica")
    public ResponseEntity<AgendamentoDTO.SlotResponse> criarSlot(
            @Valid @RequestBody AgendamentoDTO.CriarSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.criarSlot(request));
    }

    @PutMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Atualizar slot de prova pratica")
    public ResponseEntity<AgendamentoDTO.SlotResponse> atualizarSlot(
            @PathVariable UUID id,
            @RequestBody AgendamentoDTO.AtualizarSlotRequest request) {
        return ResponseEntity.ok(agendamentoService.atualizarSlot(id, request));
    }

    @DeleteMapping("/slots/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Remover slot de prova pratica", description = "Remove um slot sem agendamentos.")
    public ResponseEntity<Void> removerSlot(@PathVariable UUID id) {
        agendamentoService.removerSlot(id);
        return ResponseEntity.noContent().build();
    }

    // ── Agendamentos ─────────────────────────────────────────────────────────

    @GetMapping("/agendamentos")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Listar todos os agendamentos", description = "Visao administrativa de todos os agendamentos de prova pratica.")
    public ResponseEntity<List<AgendamentoDTO.Response>> listarAgendamentos() {
        return ResponseEntity.ok(agendamentoService.listarTodosAgendamentos());
    }
}

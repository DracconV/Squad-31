package br.gov.seed.autenticacao.controller;

import br.gov.seed.autenticacao.dto.InstituicaoDTO;
import br.gov.seed.autenticacao.service.InstituicaoService;
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
@RequestMapping("/instituicoes")
@RequiredArgsConstructor
@Tag(name = "Instituicoes", description = "Escolas e instituicoes da rede SEED")
@SecurityRequirement(name = "bearerAuth")
public class InstituicaoController {

    private final InstituicaoService instituicaoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar instituicoes ativas", description = "Retorna todas as escolas/instituicoes ativas da rede SEED.")
    public ResponseEntity<List<InstituicaoDTO.Response>> listar() {
        return ResponseEntity.ok(instituicaoService.listarAtivas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar instituicao por ID")
    public ResponseEntity<InstituicaoDTO.Response> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(instituicaoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN_SEED')")
    @Operation(summary = "Criar nova instituicao", description = "Cadastra nova escola na plataforma. Requer perfil ADMIN_SEED.")
    public ResponseEntity<InstituicaoDTO.Response> criar(@Valid @RequestBody InstituicaoDTO.CriarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(instituicaoService.criar(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_SEED')")
    @Operation(summary = "Editar instituicao", description = "Atualiza dados de uma escola. Requer perfil ADMIN_SEED.")
    public ResponseEntity<InstituicaoDTO.Response> atualizar(
            @PathVariable UUID id,
            @RequestBody InstituicaoDTO.EditarRequest request) {
        return ResponseEntity.ok(instituicaoService.atualizar(id, request));
    }
}

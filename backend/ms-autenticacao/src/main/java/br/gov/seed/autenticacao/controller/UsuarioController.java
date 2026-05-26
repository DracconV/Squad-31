package br.gov.seed.autenticacao.controller;

import br.gov.seed.autenticacao.dto.UsuarioDTO;
import br.gov.seed.autenticacao.service.UsuarioService;
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
@RequestMapping("/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gerenciamento de perfis de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Criar novo usuario", description = "Cria perfil de aluno ou professor. Requer perfil ADMIN_SEED ou ADMIN_ESCOLA.")
    public ResponseEntity<UsuarioDTO.Response> criar(@Valid @RequestBody UsuarioDTO.CriarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Listar todos os usuarios")
    public ResponseEntity<List<UsuarioDTO.Response>> listar(
            @RequestParam(required = false) UUID instituicaoId) {
        if (instituicaoId != null) {
            return ResponseEntity.ok(usuarioService.listarPorInstituicao(instituicaoId));
        }
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Buscar usuario por ID")
    public ResponseEntity<UsuarioDTO.Response> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Atualizar dados do usuario", description = "Atualiza nome, email, cpf e/ou instituicao do usuario.")
    public ResponseEntity<UsuarioDTO.Response> atualizar(
            @PathVariable UUID id,
            @RequestBody UsuarioDTO.AtualizarRequest request) {
        return ResponseEntity.ok(usuarioService.atualizar(id, request));
    }

    @PutMapping("/{id}/desativar")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Desativar usuario")
    public ResponseEntity<UsuarioDTO.Response> desativar(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.desativar(id));
    }

    @PutMapping("/{id}/reativar")
    @PreAuthorize("hasAnyRole('ADMIN_SEED', 'ADMIN_ESCOLA')")
    @Operation(summary = "Reativar usuario")
    public ResponseEntity<UsuarioDTO.Response> reativar(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.reativar(id));
    }
}

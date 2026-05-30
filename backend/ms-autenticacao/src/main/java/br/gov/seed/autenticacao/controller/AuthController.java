package br.gov.seed.autenticacao.controller;

import br.gov.seed.autenticacao.dto.AuthDTO;
import br.gov.seed.autenticacao.dto.AuthDTO.*;
import br.gov.seed.autenticacao.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/primeiro-acesso")
    public ResponseEntity<Void> primeiroAcesso(@Valid @RequestBody PrimeiroAcessoRequest request) {
        authService.primeiroAcesso(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<String> me() {
        return ResponseEntity.ok("autenticado");
    }

    /** Gera token de redefinição de senha (pública — uso pelo admin ou integração). */
    @PostMapping("/solicitar-redefinicao")
    public ResponseEntity<AuthDTO.RedefinicaoResponse> solicitarRedefinicao(
            @Valid @RequestBody AuthDTO.SolicitarRedefinicaoRequest request) {
        return ResponseEntity.ok(authService.solicitarRedefinicao(request));
    }

    /** Redefine a senha usando o token gerado em /solicitar-redefinicao. */
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
            @Valid @RequestBody AuthDTO.RedefinirSenhaRequest request) {
        authService.redefinirSenha(request);
        return ResponseEntity.noContent().build();
    }
}
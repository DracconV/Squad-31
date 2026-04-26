package br.gov.seed.autenticacao.controller;

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
}
package br.gov.seed.autenticacao.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDTO {

    public record LoginRequest(
        @NotBlank(message = "Matrícula obrigatória")
        String matricula,

        @NotBlank(message = "Senha obrigatória")
        String senha
    ) {}

    public record LoginResponse(
        String token,
        String tipo,
        String usuarioId,
        String nome,
        String perfil,
        Boolean primeiroAcesso
    ) {}

    public record PrimeiroAcessoRequest(
        @NotBlank(message = "Matrícula obrigatória")
        String matricula,

        @NotBlank(message = "Senha temporária obrigatória")
        String senhaTemporaria,

        @NotBlank(message = "Nova senha obrigatória")
        String novaSenha
    ) {}
}

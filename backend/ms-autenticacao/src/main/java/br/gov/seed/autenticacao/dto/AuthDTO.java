package br.gov.seed.autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    public record SolicitarRedefinicaoRequest(
        @NotBlank(message = "Matrícula obrigatória")
        String matricula
    ) {}

    public record RedefinicaoResponse(
        String token,
        String expiraEm,
        String mensagem
    ) {}

    public record RedefinirSenhaRequest(
        @NotBlank(message = "Token obrigatório")
        String token,

        @NotBlank(message = "Nova senha obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String novaSenha
    ) {}
}

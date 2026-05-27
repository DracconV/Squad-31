package br.gov.seed.autenticacao.dto;

import br.gov.seed.autenticacao.entity.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UsuarioDTO {

    public record CriarRequest(
        @NotBlank String nome,
        @NotBlank String matricula,
        String cpf,
        String email,
        @NotBlank @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres") String senhaTemporaria,
        @NotNull Usuario.Perfil perfil,
        UUID instituicaoId
    ) {}

    /** Item de resultado de importação — uma linha do CSV. */
    public record ImportacaoItem(
        int linha,
        String matricula,
        String status,   // "ok" | "erro"
        String mensagem
    ) {}

    /** Resultado completo de uma importação via CSV. */
    public record ImportacaoResult(
        int total,
        int importados,
        int erros,
        List<ImportacaoItem> detalhes
    ) {}

    public record AtualizarRequest(
        String nome,
        String email,
        String cpf,
        UUID instituicaoId
    ) {}

    public record Response(
        UUID id,
        String nome,
        String matricula,
        String cpf,
        String email,
        String perfil,
        Boolean ativo,
        Boolean primeiroAcesso,
        UUID instituicaoId,
        String nomeInstituicao,
        LocalDateTime criadoEm
    ) {
        public static Response from(Usuario u) {
            return new Response(
                u.getId(),
                u.getNome(),
                u.getMatricula(),
                u.getCpf(),
                u.getEmail(),
                u.getPerfil().name(),
                u.getAtivo(),
                u.getPrimeiroAcesso(),
                u.getInstituicao() != null ? u.getInstituicao().getId() : null,
                u.getInstituicao() != null ? u.getInstituicao().getNome() : null,
                u.getCriadoEm()
            );
        }
    }
}

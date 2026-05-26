package br.gov.seed.autenticacao.dto;

import br.gov.seed.autenticacao.entity.AlunoTurma;
import br.gov.seed.autenticacao.entity.Turma;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class TurmaDTO {

    public record CriarRequest(
        @NotBlank String nome,
        @NotNull Integer ano,
        @NotBlank String modalidade,
        @NotNull UUID instituicaoId
    ) {}

    public record AtualizarRequest(
        String nome,
        Integer ano,
        String modalidade
    ) {}

    public record AdicionarAlunoRequest(
        @NotNull UUID alunoId
    ) {}

    public record Response(
        UUID id,
        String nome,
        Integer ano,
        String modalidade,
        UUID instituicaoId,
        String nomeInstituicao,
        Boolean ativo,
        LocalDateTime criadoEm
    ) {
        public static Response from(Turma t) {
            return new Response(
                t.getId(),
                t.getNome(),
                t.getAno(),
                t.getModalidade(),
                t.getInstituicao() != null ? t.getInstituicao().getId() : null,
                t.getInstituicao() != null ? t.getInstituicao().getNome() : null,
                t.getAtivo(),
                t.getCriadoEm()
            );
        }
    }

    public record AlunoResponse(
        UUID alunoId,
        String nome,
        String matricula,
        String perfil
    ) {
        public static AlunoResponse from(AlunoTurma at) {
            return new AlunoResponse(
                at.getAlunoId(),
                at.getAluno().getNome(),
                at.getAluno().getMatricula(),
                at.getAluno().getPerfil().name()
            );
        }
    }
}

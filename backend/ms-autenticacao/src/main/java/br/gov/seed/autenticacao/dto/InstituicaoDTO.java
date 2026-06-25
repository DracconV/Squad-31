package br.gov.seed.autenticacao.dto;

import br.gov.seed.autenticacao.entity.Instituicao;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

public class InstituicaoDTO {

    public record CriarRequest(
        @NotBlank String nome,
        @NotBlank String municipio,
        @NotBlank String codigoInep
    ) {}

    public record EditarRequest(
        String nome,
        String municipio,
        String codigoInep,
        Boolean ativo
    ) {}

    public record Response(
        UUID id,
        String nome,
        String municipio,
        String codigoInep,
        Boolean ativo,
        LocalDateTime criadoEm
    ) {
        public static Response from(Instituicao i) {
            return new Response(
                i.getId(),
                i.getNome(),
                i.getMunicipio(),
                i.getCodigoInep(),
                i.getAtivo(),
                i.getCriadoEm()
            );
        }
    }
}

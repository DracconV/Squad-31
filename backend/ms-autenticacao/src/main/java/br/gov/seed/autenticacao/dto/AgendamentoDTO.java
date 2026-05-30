package br.gov.seed.autenticacao.dto;

import br.gov.seed.autenticacao.entity.AgendamentoProva;
import br.gov.seed.autenticacao.entity.SlotProvaPratica;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public class AgendamentoDTO {

    public record CriarRequest(
        @NotNull UUID slotId
    ) {}

    public record CriarSlotRequest(
        @NotNull UUID moduloId,
        @NotNull LocalDateTime data,
        @NotBlank String local,
        @NotNull @Min(value = 1, message = "Slot deve ter no mínimo 1 vaga") Integer vagasTotais
    ) {}

    public record AtualizarSlotRequest(
        LocalDateTime data,
        String local,
        Integer vagasTotais
    ) {}

    public record ReagendarRequest(
        @NotNull UUID novoSlotId
    ) {}

    public record SlotResponse(
        UUID id,
        UUID moduloId,
        LocalDateTime data,
        String local,
        Integer vagasTotais,
        Integer vagasOcupadas,
        Integer vagasDisponiveis
    ) {
        public static SlotResponse from(SlotProvaPratica s) {
            return new SlotResponse(
                s.getId(),
                s.getModuloId(),
                s.getData(),
                s.getLocal(),
                s.getVagasTotais(),
                s.getVagasOcupadas(),
                s.getVagasTotais() - s.getVagasOcupadas()
            );
        }
    }

    public record Response(
        UUID id,
        UUID alunoId,
        UUID slotId,
        LocalDateTime dataProva,
        String local
    ) {
        public static Response from(AgendamentoProva a) {
            return new Response(
                a.getId(),
                a.getAlunoId(),
                a.getSlotId(),
                a.getSlot() != null ? a.getSlot().getData() : null,
                a.getSlot() != null ? a.getSlot().getLocal() : null
            );
        }
    }
}

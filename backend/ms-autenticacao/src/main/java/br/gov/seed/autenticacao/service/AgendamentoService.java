package br.gov.seed.autenticacao.service;

import br.gov.seed.autenticacao.dto.AgendamentoDTO;
import br.gov.seed.autenticacao.entity.AgendamentoProva;
import br.gov.seed.autenticacao.entity.SlotProvaPratica;
import br.gov.seed.autenticacao.repository.AgendamentoProvaRepository;
import br.gov.seed.autenticacao.repository.SlotProvaPraticaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoProvaRepository agendamentoRepository;
    private final SlotProvaPraticaRepository slotRepository;

    public List<AgendamentoDTO.SlotResponse> listarSlotsDisponiveis() {
        return slotRepository.findDisponiveisOrderByData().stream()
                .map(AgendamentoDTO.SlotResponse::from)
                .toList();
    }

    @Transactional
    public AgendamentoDTO.Response agendar(UUID alunoId, AgendamentoDTO.CriarRequest request) {
        SlotProvaPratica slot = slotRepository.findById(request.slotId())
                .orElseThrow(() -> new IllegalArgumentException("Slot nao encontrado: " + request.slotId()));

        if (slot.getVagasOcupadas() >= slot.getVagasTotais()) {
            throw new IllegalStateException("Slot sem vagas disponiveis");
        }

        if (agendamentoRepository.existsByAlunoIdAndSlotId(alunoId, request.slotId())) {
            throw new IllegalArgumentException("Aluno ja possui agendamento neste slot");
        }

        slot.setVagasOcupadas(slot.getVagasOcupadas() + 1);
        slotRepository.save(slot);

        AgendamentoProva agendamento = new AgendamentoProva();
        agendamento.setAlunoId(alunoId);
        agendamento.setSlotId(request.slotId());

        AgendamentoProva saved = agendamentoRepository.save(agendamento);
        saved.setSlot(slot);

        return AgendamentoDTO.Response.from(saved);
    }

    public List<AgendamentoDTO.Response> meusAgendamentos(UUID alunoId) {
        return agendamentoRepository.findByAlunoIdWithSlot(alunoId).stream()
                .map(AgendamentoDTO.Response::from)
                .toList();
    }

    @Transactional
    public void cancelar(UUID agendamentoId, UUID alunoId) {
        AgendamentoProva agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento nao encontrado: " + agendamentoId));

        if (!agendamento.getAlunoId().equals(alunoId)) {
            throw new IllegalArgumentException("Agendamento nao pertence ao aluno informado");
        }

        SlotProvaPratica slot = slotRepository.findById(agendamento.getSlotId())
                .orElseThrow(() -> new IllegalStateException("Slot do agendamento nao encontrado"));

        slot.setVagasOcupadas(Math.max(0, slot.getVagasOcupadas() - 1));
        slotRepository.save(slot);

        agendamentoRepository.delete(agendamento);
    }
}

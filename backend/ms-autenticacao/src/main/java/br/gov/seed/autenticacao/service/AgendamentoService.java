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
    public AgendamentoDTO.SlotResponse criarSlot(AgendamentoDTO.CriarSlotRequest request) {
        SlotProvaPratica slot = SlotProvaPratica.builder()
                .moduloId(request.moduloId())
                .data(request.data())
                .local(request.local())
                .vagasTotais(request.vagasTotais())
                .vagasOcupadas(0)
                .build();
        return AgendamentoDTO.SlotResponse.from(slotRepository.save(slot));
    }

    public List<AgendamentoDTO.SlotResponse> listarTodosSlots() {
        return slotRepository.findAll().stream()
                .map(AgendamentoDTO.SlotResponse::from)
                .toList();
    }

    @Transactional
    public AgendamentoDTO.SlotResponse atualizarSlot(UUID id, AgendamentoDTO.AtualizarSlotRequest request) {
        SlotProvaPratica slot = slotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Slot nao encontrado: " + id));
        if (request.data() != null) slot.setData(request.data());
        if (request.local() != null && !request.local().isBlank()) slot.setLocal(request.local());
        if (request.vagasTotais() != null) {
            if (request.vagasTotais() < slot.getVagasOcupadas()) {
                throw new IllegalStateException("Nao e possivel reduzir vagas abaixo das ja ocupadas (" + slot.getVagasOcupadas() + ")");
            }
            slot.setVagasTotais(request.vagasTotais());
        }
        return AgendamentoDTO.SlotResponse.from(slotRepository.save(slot));
    }

    @Transactional
    public void removerSlot(UUID id) {
        SlotProvaPratica slot = slotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Slot nao encontrado: " + id));
        if (slot.getVagasOcupadas() > 0) {
            throw new IllegalStateException("Slot possui agendamentos — cancele-os antes de remover");
        }
        slotRepository.delete(slot);
    }

    public List<AgendamentoDTO.Response> listarTodosAgendamentos() {
        return agendamentoRepository.findAllWithSlot().stream()
                .map(AgendamentoDTO.Response::from)
                .toList();
    }

    @Transactional
    public AgendamentoDTO.Response reagendar(UUID agendamentoId, UUID alunoId, UUID novoSlotId) {
        AgendamentoProva agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento nao encontrado: " + agendamentoId));

        if (!agendamento.getAlunoId().equals(alunoId)) {
            throw new IllegalArgumentException("Agendamento nao pertence ao aluno informado");
        }

        if (agendamento.getSlotId().equals(novoSlotId)) {
            throw new IllegalArgumentException("O novo slot deve ser diferente do atual");
        }

        // Libera vaga no slot antigo
        SlotProvaPratica slotAntigo = slotRepository.findById(agendamento.getSlotId())
                .orElseThrow(() -> new IllegalStateException("Slot antigo nao encontrado"));
        slotAntigo.setVagasOcupadas(Math.max(0, slotAntigo.getVagasOcupadas() - 1));
        slotRepository.save(slotAntigo);

        // Ocupa vaga no novo slot
        SlotProvaPratica novoSlot = slotRepository.findById(novoSlotId)
                .orElseThrow(() -> new IllegalArgumentException("Novo slot nao encontrado: " + novoSlotId));
        if (novoSlot.getVagasOcupadas() >= novoSlot.getVagasTotais()) {
            throw new IllegalStateException("Novo slot sem vagas disponiveis");
        }
        novoSlot.setVagasOcupadas(novoSlot.getVagasOcupadas() + 1);
        slotRepository.save(novoSlot);

        // Atualiza agendamento
        agendamento.setSlotId(novoSlotId);
        AgendamentoProva salvo = agendamentoRepository.save(agendamento);
        salvo.setSlot(novoSlot);
        return AgendamentoDTO.Response.from(salvo);
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

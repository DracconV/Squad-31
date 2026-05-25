package org.example.mssimulados.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.mssimulados.model.OutboxEvent;
import org.example.mssimulados.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TempoSimuladoService {

    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper mapper;

    @Transactional
    public void encerrarPorTempo(Long simuladoId) {
        try {
            OutboxEvent evento = new OutboxEvent();
            evento.setAggregate("simulado");
            evento.setEventType("TEMPO_ESGOTADO");
            evento.setPayload(mapper.writeValueAsString(
                    Map.of("simuladoId", simuladoId, "motivo", "TEMPO_ESGOTADO")
            ));
            outboxRepo.save(evento);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encerrar simulado", e);
        }
    }
}
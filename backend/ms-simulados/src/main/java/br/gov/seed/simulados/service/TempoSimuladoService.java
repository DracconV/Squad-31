package br.gov.seed.simulados.service;

import br.gov.seed.simulados.model.OutboxEvent;
import br.gov.seed.simulados.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TempoSimuladoService {

    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper mapper;

    @Transactional
    public void encerrarPorTempo(UUID simuladoId) {
        try {
            OutboxEvent evento = new OutboxEvent();
            evento.setTipo("TEMPO_ESGOTADO");
            evento.setPayload(mapper.writeValueAsString(
                    Map.of("simuladoId", simuladoId.toString(), "motivo", "TEMPO_ESGOTADO")
            ));
            outboxRepo.save(evento);
            log.info("Evento TEMPO_ESGOTADO registrado no outbox para simuladoId={}", simuladoId);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao encerrar simulado por tempo: " + e.getMessage(), e);
        }
    }
}

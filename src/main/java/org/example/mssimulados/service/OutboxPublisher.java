package org.example.mssimulados.service;

import lombok.RequiredArgsConstructor;
import org.example.mssimulados.model.OutboxEvent;
import org.example.mssimulados.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publicar() {
        List<OutboxEvent> pendentes = outboxRepo.findByPublicadoFalse();

        for (OutboxEvent evento : pendentes) {
            kafkaTemplate.send(
                    "simulado-eventos",
                    evento.getId().toString(),
                    evento.getPayload()
            );
            evento.setPublicado(true);
            outboxRepo.save(evento);
        }
    }
}
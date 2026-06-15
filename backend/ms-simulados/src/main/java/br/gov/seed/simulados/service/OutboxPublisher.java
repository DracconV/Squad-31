package br.gov.seed.simulados.service;

import br.gov.seed.simulados.model.OutboxEvent;
import br.gov.seed.simulados.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publicar() {
        List<OutboxEvent> pendentes = outboxRepo.findByStatus("PENDENTE");

        for (OutboxEvent evento : pendentes) {
            try {
                // send() é assíncrono — o .get() força a confirmação do broker.
                // Sem isso, marcaríamos ENVIADO mesmo com Kafka fora do ar (perda de evento).
                kafkaTemplate.send(
                        "simulado-eventos",
                        evento.getId().toString(),
                        evento.getPayload()
                ).get(10, java.util.concurrent.TimeUnit.SECONDS);
                evento.setStatus("ENVIADO");
                evento.setEnviadoEm(LocalDateTime.now());
                outboxRepo.save(evento);
                log.debug("Evento outbox publicado id={} tipo={}", evento.getId(), evento.getTipo());
            } catch (Exception e) {
                evento.setTentativas(evento.getTentativas() + 1);
                evento.setErroMsg(e.getMessage());
                if (evento.getTentativas() >= 5) {
                    evento.setStatus("ERRO");
                    log.error("Evento outbox falhou definitivamente id={}: {}", evento.getId(), e.getMessage());
                } else {
                    log.warn("Tentativa {} falhou para evento outbox id={}: {}",
                            evento.getTentativas(), evento.getId(), e.getMessage());
                }
                outboxRepo.save(evento);
            }
        }
    }
}

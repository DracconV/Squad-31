package br.com.seed.notificacoes.event;

import org.springframework.stereotype.Component;

@Component
public class NoOpNotificationEventPublisher implements NotificationEventPublisher {

    @Override
    public void publishCreated(NotificationCreatedEvent event) {
        // Kafka sera conectado em uma etapa futura.
    }
}

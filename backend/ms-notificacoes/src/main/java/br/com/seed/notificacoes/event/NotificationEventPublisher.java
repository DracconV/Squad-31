package br.com.seed.notificacoes.event;

public interface NotificationEventPublisher {

    void publishCreated(NotificationCreatedEvent event);
}

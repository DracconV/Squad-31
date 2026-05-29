package br.com.seed.notificacoes.exception;

import java.util.UUID;

public class NotificationNotFoundException extends RuntimeException {

    public NotificationNotFoundException(UUID id) {
        super("Notificacao nao encontrada: " + id);
    }
}

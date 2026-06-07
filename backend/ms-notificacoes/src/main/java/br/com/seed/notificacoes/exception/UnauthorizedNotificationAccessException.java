package br.com.seed.notificacoes.exception;

public class UnauthorizedNotificationAccessException extends RuntimeException {

    public UnauthorizedNotificationAccessException() {
        super("Usuario nao autorizado a alterar esta notificacao");
    }
}

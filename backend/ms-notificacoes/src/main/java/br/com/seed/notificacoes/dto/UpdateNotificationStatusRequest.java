package br.com.seed.notificacoes.dto;

import br.com.seed.notificacoes.domain.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateNotificationStatusRequest(@NotNull NotificationStatus status) {
}

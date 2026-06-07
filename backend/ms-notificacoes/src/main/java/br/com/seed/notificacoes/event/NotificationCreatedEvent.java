package br.com.seed.notificacoes.event;

import br.com.seed.notificacoes.domain.enums.NotificationChannel;
import br.com.seed.notificacoes.domain.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationCreatedEvent(
        UUID notificationId,
        UUID userId,
        String title,
        NotificationType type,
        NotificationChannel channel,
        UUID referenceId,
        String referenceType,
        LocalDateTime createdAt
) {
}

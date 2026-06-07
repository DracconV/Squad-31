package br.com.seed.notificacoes.dto;

import br.com.seed.notificacoes.domain.enums.NotificationChannel;
import br.com.seed.notificacoes.domain.enums.NotificationStatus;
import br.com.seed.notificacoes.domain.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        String title,
        String message,
        NotificationType type,
        NotificationChannel channel,
        NotificationStatus status,
        UUID referenceId,
        String referenceType,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}

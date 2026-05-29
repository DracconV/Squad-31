package br.com.seed.notificacoes.dto;

import br.com.seed.notificacoes.domain.enums.NotificationChannel;
import br.com.seed.notificacoes.domain.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateTargetedNotificationsRequest(
        @NotEmpty List<@NotNull UUID> userIds,
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 500) String message,
        @NotNull NotificationType type,
        @NotNull NotificationChannel channel,
        UUID referenceId,
        @Size(max = 80) String referenceType
) {
}

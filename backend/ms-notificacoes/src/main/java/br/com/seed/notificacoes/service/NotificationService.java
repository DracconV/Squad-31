package br.com.seed.notificacoes.service;

import br.com.seed.notificacoes.domain.entity.Notification;
import br.com.seed.notificacoes.domain.enums.NotificationStatus;
import br.com.seed.notificacoes.dto.CreateNotificationRequest;
import br.com.seed.notificacoes.dto.CreateTargetedNotificationsRequest;
import br.com.seed.notificacoes.dto.NotificationResponse;
import br.com.seed.notificacoes.dto.NotificationSummaryResponse;
import br.com.seed.notificacoes.event.NotificationCreatedEvent;
import br.com.seed.notificacoes.event.NotificationEventPublisher;
import br.com.seed.notificacoes.exception.NotificationNotFoundException;
import br.com.seed.notificacoes.exception.UnauthorizedNotificationAccessException;
import br.com.seed.notificacoes.repository.NotificationRepository;
import br.com.seed.notificacoes.security.SecurityContextHelper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final SecurityContextHelper securityContextHelper;
    private final NotificationEventPublisher eventPublisher;

    public NotificationService(
            NotificationRepository repository,
            SecurityContextHelper securityContextHelper,
            NotificationEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.securityContextHelper = securityContextHelper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setType(request.type());
        notification.setChannel(request.channel());
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setReferenceId(request.referenceId());
        notification.setReferenceType(request.referenceType());

        Notification saved = repository.save(notification);
        eventPublisher.publishCreated(toCreatedEvent(saved));
        return toResponse(saved);
    }

    @Transactional
    public List<NotificationResponse> createForTargets(CreateTargetedNotificationsRequest request) {
        return request.userIds().stream()
                .distinct()
                .map(userId -> create(toSingleNotificationRequest(userId, request)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAuthenticatedUserNotifications() {
        UUID userId = securityContextHelper.getAuthenticatedUserId();
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listAuthenticatedUserUnreadNotifications() {
        UUID userId = securityContextHelper.getAuthenticatedUserId();
        return repository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.UNREAD).stream()
                .map(NotificationService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse countAuthenticatedUserUnreadNotifications() {
        UUID userId = securityContextHelper.getAuthenticatedUserId();
        return new NotificationSummaryResponse(repository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD));
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = findAndAuthorizeChange(id);
        notification.setStatus(NotificationStatus.READ);
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse archive(UUID id) {
        Notification notification = findAndAuthorizeChange(id);
        notification.setStatus(NotificationStatus.ARCHIVED);
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse updateStatus(UUID id, NotificationStatus status) {
        Notification notification = findAndAuthorizeChange(id);
        notification.setStatus(status);
        if (status == NotificationStatus.READ && notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return toResponse(notification);
    }

    private Notification findAndAuthorizeChange(UUID id) {
        Notification notification = repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));

        UUID currentUserId = securityContextHelper.getAuthenticatedUserId();
        if (!notification.getUserId().equals(currentUserId) && !securityContextHelper.isInstitutionalOperator()) {
            throw new UnauthorizedNotificationAccessException();
        }

        return notification;
    }

    private static CreateNotificationRequest toSingleNotificationRequest(
            UUID userId,
            CreateTargetedNotificationsRequest request
    ) {
        return new CreateNotificationRequest(
                userId,
                request.title(),
                request.message(),
                request.type(),
                request.channel(),
                request.referenceId(),
                request.referenceType()
        );
    }

    private static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getChannel(),
                notification.getStatus(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private static NotificationCreatedEvent toCreatedEvent(Notification notification) {
        return new NotificationCreatedEvent(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getType(),
                notification.getChannel(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.getCreatedAt()
        );
    }
}

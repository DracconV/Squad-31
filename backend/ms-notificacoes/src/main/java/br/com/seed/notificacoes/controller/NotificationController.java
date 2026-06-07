package br.com.seed.notificacoes.controller;

import br.com.seed.notificacoes.dto.CreateNotificationRequest;
import br.com.seed.notificacoes.dto.CreateTargetedNotificationsRequest;
import br.com.seed.notificacoes.dto.NotificationResponse;
import br.com.seed.notificacoes.dto.NotificationSummaryResponse;
import br.com.seed.notificacoes.dto.UpdateNotificationStatusRequest;
import br.com.seed.notificacoes.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notificacoes")
@Tag(name = "Notificacoes", description = "Notificacoes institucionais sobre provas, simulados, resultados e operacao da rede")
@SecurityRequirement(name = "BearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Lista notificacoes do usuario autenticado")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> list() {
        return ResponseEntity.ok(notificationService.listAuthenticatedUserNotifications());
    }

    @Operation(summary = "Lista notificacoes nao lidas do usuario autenticado")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> listUnread() {
        return ResponseEntity.ok(notificationService.listAuthenticatedUserUnreadNotifications());
    }

    @Operation(summary = "Conta notificacoes nao lidas do usuario autenticado")
    @GetMapping("/unread/count")
    public ResponseEntity<NotificationSummaryResponse> countUnread() {
        return ResponseEntity.ok(notificationService.countAuthenticatedUserUnreadNotifications());
    }

    @Operation(summary = "Cria notificacao institucional")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }

    @Operation(summary = "Cria notificacoes institucionais para destinatarios especificos")
    @PostMapping("/destino")
    @PreAuthorize("hasAnyRole('ADMIN_ESCOLA', 'ADMIN_SEED')")
    public ResponseEntity<List<NotificationResponse>> createForTargets(
            @Valid @RequestBody CreateTargetedNotificationsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.createForTargets(request));
    }

    @Operation(summary = "Marca uma notificacao como lida")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @Operation(summary = "Arquiva uma notificacao")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<NotificationResponse> archive(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.archive(id));
    }

    @Operation(summary = "Atualiza o status de uma notificacao")
    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNotificationStatusRequest request
    ) {
        return ResponseEntity.ok(notificationService.updateStatus(id, request.status()));
    }
}

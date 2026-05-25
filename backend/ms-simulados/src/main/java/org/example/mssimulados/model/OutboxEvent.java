package org.example.mssimulados.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String aggregate;
    private String eventType;

    @Column(columnDefinition = "text")
    private String payload;

    private boolean publicado = false;
    private LocalDateTime criadoEm = LocalDateTime.now();
}
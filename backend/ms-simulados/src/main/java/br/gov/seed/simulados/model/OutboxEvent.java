package br.gov.seed.simulados.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapeado à tabela outbox_event criada pela migration V7 do ms-autenticacao.
 * Colunas: id, tipo, payload (jsonb), status, tentativas, criado_em, enviado_em, erro_msg
 */
@Data
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Tipo do evento, ex: TEMPO_ESGOTADO, SIMULADO_FINALIZADO */
    @Column(nullable = false)
    private String tipo;

    /** Payload JSON serializado. Coluna JSONB no DB aceita string via JDBC. */
    @Column(columnDefinition = "text", nullable = false)
    private String payload;

    /** PENDENTE | ENVIADO | ERRO */
    @Column(nullable = false)
    private String status = "PENDENTE";

    private int tentativas = 0;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(name = "erro_msg")
    private String erroMsg;
}

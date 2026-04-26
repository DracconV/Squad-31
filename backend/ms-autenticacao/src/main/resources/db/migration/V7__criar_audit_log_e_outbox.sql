-- Audit log — registra todas as ações incluindo inferências de IA
CREATE TABLE audit_log (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id   UUID REFERENCES usuario(id),
    acao         VARCHAR(100) NOT NULL,
    entidade     VARCHAR(100),
    entidade_id  UUID,
    detalhes     JSONB,
    ip_origem    VARCHAR(45),
    timestamp    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_usuario ON audit_log(usuario_id, timestamp DESC);
CREATE INDEX idx_audit_entidade ON audit_log(entidade, entidade_id);

-- Outbox Pattern — garante entrega de eventos ao Kafka mesmo em falhas
CREATE TABLE outbox_event (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo         VARCHAR(100) NOT NULL,
    payload      JSONB NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                     CHECK (status IN ('PENDENTE','ENVIADO','ERRO')),
    tentativas   INTEGER NOT NULL DEFAULT 0,
    criado_em    TIMESTAMP NOT NULL DEFAULT NOW(),
    enviado_em   TIMESTAMP,
    erro_msg     TEXT
);

CREATE INDEX idx_outbox_status ON outbox_event(status, criado_em);

CREATE TABLE reset_token (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id  UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token       VARCHAR(64)  NOT NULL UNIQUE,
    expira_em   TIMESTAMP    NOT NULL,
    usado       BOOLEAN      NOT NULL DEFAULT false,
    criado_em   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_reset_token_token ON reset_token(token);

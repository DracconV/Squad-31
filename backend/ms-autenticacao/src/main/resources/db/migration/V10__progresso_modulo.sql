-- Progresso do aluno por módulo do curso
CREATE TABLE progresso_modulo (
    aluno_id    UUID NOT NULL REFERENCES usuario(id),
    modulo_id   UUID NOT NULL REFERENCES modulo(id),
    concluido_em TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (aluno_id, modulo_id)
);

CREATE INDEX idx_progresso_aluno ON progresso_modulo(aluno_id);
CREATE INDEX idx_progresso_modulo ON progresso_modulo(modulo_id);

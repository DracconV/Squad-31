-- Frequência / faltas por aluno e disciplina (controle de presença)
CREATE TABLE IF NOT EXISTS frequencia (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id      UUID         NOT NULL,
    turma_id      UUID         NOT NULL,
    disciplina    VARCHAR(100) NOT NULL,
    total_aulas   INTEGER      NOT NULL DEFAULT 0,
    faltas        INTEGER      NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_frequencia_aluno_disciplina UNIQUE (aluno_id, disciplina)
);

CREATE INDEX IF NOT EXISTS idx_frequencia_aluno ON frequencia(aluno_id);
CREATE INDEX IF NOT EXISTS idx_frequencia_turma ON frequencia(turma_id);

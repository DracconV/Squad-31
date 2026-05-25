-- Histórico de todas as questões respondidas — base do diagnóstico de IA
CREATE TABLE historico_questao_aluno (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id      UUID NOT NULL REFERENCES usuario(id),
    questao_id    UUID NOT NULL REFERENCES questao(id),
    acertou       BOOLEAN NOT NULL,
    respondido_em TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice composto para queries de diagnóstico sem full table scan
CREATE INDEX idx_historico_aluno_disciplina
    ON historico_questao_aluno(aluno_id, respondido_em DESC);

-- Diagnóstico adaptativo gerado pela IA
CREATE TABLE diagnostico_aluno (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id      UUID NOT NULL REFERENCES usuario(id),
    disciplina_id UUID NOT NULL REFERENCES disciplina(id),
    payload       JSONB NOT NULL,
    gerado_em     TIMESTAMP NOT NULL DEFAULT NOW(),
    versao_modelo VARCHAR(50)
);

CREATE INDEX idx_diagnostico_aluno ON diagnostico_aluno(aluno_id, gerado_em DESC);

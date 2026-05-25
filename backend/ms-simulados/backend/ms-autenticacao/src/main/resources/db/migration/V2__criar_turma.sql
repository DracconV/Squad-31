-- Turmas vinculadas às instituições
CREATE TABLE turma (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome           VARCHAR(100) NOT NULL,
    ano            INTEGER NOT NULL,
    modalidade     VARCHAR(20) NOT NULL CHECK (modalidade IN ('MEDIO','EJA','PROFISSIONALIZANTE')),
    instituicao_id UUID NOT NULL REFERENCES instituicao(id),
    ativo          BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Relacionamento aluno <-> turma
CREATE TABLE aluno_turma (
    aluno_id  UUID NOT NULL REFERENCES usuario(id),
    turma_id  UUID NOT NULL REFERENCES turma(id),
    PRIMARY KEY (aluno_id, turma_id)
);

CREATE INDEX idx_turma_instituicao ON turma(instituicao_id);

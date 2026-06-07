-- Tabela de desempenho do aluno
CREATE TABLE IF NOT EXISTS desempenho_aluno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL,
    turma_id UUID NOT NULL,
    disciplina VARCHAR(100) NOT NULL,
    nota_media DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    questoes_acertadas INTEGER NOT NULL DEFAULT 0,
    questoes_total INTEGER NOT NULL DEFAULT 0,
    taxa_acerto FLOAT NOT NULL DEFAULT 0.0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_aluno_disciplina UNIQUE (aluno_id, disciplina)
);

CREATE INDEX IF NOT EXISTS idx_desempenho_aluno_aluno_id ON desempenho_aluno(aluno_id);
CREATE INDEX IF NOT EXISTS idx_desempenho_aluno_turma_id ON desempenho_aluno(turma_id);
CREATE INDEX IF NOT EXISTS idx_desempenho_aluno_disciplina ON desempenho_aluno(disciplina);

-- Tabela de desempenho agregado da turma
CREATE TABLE IF NOT EXISTS desempenho_turma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    turma_id UUID NOT NULL UNIQUE,
    media_turma DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    mediana_turma DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    maior_nota DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    menor_nota DECIMAL(5, 2) NOT NULL DEFAULT 10.00,
    taxa_conclusao FLOAT NOT NULL DEFAULT 0.0,
    alunos_ativos INTEGER NOT NULL DEFAULT 0,
    total_alunos INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_desempenho_turma_turma_id ON desempenho_turma(turma_id);


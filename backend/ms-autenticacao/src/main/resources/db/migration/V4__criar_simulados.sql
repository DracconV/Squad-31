                                                                                                                                                                                                                        -- Simulados criados por professores
CREATE TABLE simulado (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo        VARCHAR(255) NOT NULL,
    professor_id  UUID NOT NULL REFERENCES usuario(id),
    turma_id      UUID REFERENCES turma(id),
    tempo_minutos INTEGER NOT NULL DEFAULT 60,
    pontuado      BOOLEAN NOT NULL DEFAULT TRUE,
    data_inicio   TIMESTAMP,
    data_fim      TIMESTAMP,
    criado_em     TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Questões de cada simulado
CREATE TABLE simulado_questao (
    simulado_id UUID NOT NULL REFERENCES simulado(id) ON DELETE CASCADE,
    questao_id  UUID NOT NULL REFERENCES questao(id),
    ordem       INTEGER NOT NULL,
    PRIMARY KEY (simulado_id, questao_id)
);

-- Tentativas dos alunos
CREATE TABLE tentativa_simulado (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id    UUID NOT NULL REFERENCES usuario(id),
    simulado_id UUID NOT NULL REFERENCES simulado(id),
    nota        DECIMAL(5,2),
    iniciado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    finalizado_em TIMESTAMP,
    tempo_gasto_segundos INTEGER
);

-- Respostas individuais de cada tentativa
CREATE TABLE resposta_tentativa (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tentativa_id    UUID NOT NULL REFERENCES tentativa_simulado(id) ON DELETE CASCADE,
    questao_id      UUID NOT NULL REFERENCES questao(id),
    alternativa_id  UUID REFERENCES alternativa(id),
    respondido_em   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_simulado_professor ON simulado(professor_id);
CREATE INDEX idx_simulado_turma ON simulado(turma_id);
CREATE INDEX idx_tentativa_aluno ON tentativa_simulado(aluno_id);
CREATE INDEX idx_tentativa_simulado ON tentativa_simulado(simulado_id);

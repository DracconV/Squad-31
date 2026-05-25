-- Hierarquia de conteúdo: disciplina > assunto
CREATE TABLE disciplina (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE assunto (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome          VARCHAR(150) NOT NULL,
    disciplina_id UUID NOT NULL REFERENCES disciplina(id),
    UNIQUE (nome, disciplina_id)
);

-- Banco de questões
CREATE TABLE questao (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enunciado   TEXT NOT NULL,
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('MULTIPLA_ESCOLHA','VERDADEIRO_FALSO')),
    dificuldade VARCHAR(10) NOT NULL CHECK (dificuldade IN ('FACIL','MEDIO','DIFICIL')),
    tipo_uso    VARCHAR(10) NOT NULL CHECK (tipo_uso IN ('TREINO','SIMULADO','AMBOS')),
    disciplina_id UUID NOT NULL REFERENCES disciplina(id),
    assunto_id    UUID REFERENCES assunto(id),
    criado_por    UUID NOT NULL REFERENCES usuario(id),
    ativa         BOOLEAN NOT NULL DEFAULT TRUE,
    tags          TEXT[],
    criado_em     TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE alternativa (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    texto     TEXT NOT NULL,
    correta   BOOLEAN NOT NULL DEFAULT FALSE,
    ordem     INTEGER NOT NULL,
    questao_id UUID NOT NULL REFERENCES questao(id) ON DELETE CASCADE
);

CREATE INDEX idx_questao_disciplina ON questao(disciplina_id);
CREATE INDEX idx_questao_tipo_uso ON questao(tipo_uso);
CREATE INDEX idx_questao_dificuldade ON questao(dificuldade);
CREATE INDEX idx_alternativa_questao ON alternativa(questao_id);

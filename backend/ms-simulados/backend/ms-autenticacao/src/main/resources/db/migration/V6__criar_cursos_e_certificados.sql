-- Cursos profissionalizantes
CREATE TABLE curso (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome      VARCHAR(255) NOT NULL,
    descricao TEXT,
    ativo     BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Módulos com pré-requisito
CREATE TABLE modulo (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                  VARCHAR(255) NOT NULL,
    ordem                 INTEGER NOT NULL,
    curso_id              UUID NOT NULL REFERENCES curso(id) ON DELETE CASCADE,
    prerequisito_modulo_id UUID REFERENCES modulo(id)
);

-- Inscrições de alunos em cursos
CREATE TABLE inscricao_curso (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id       UUID NOT NULL REFERENCES usuario(id),
    curso_id       UUID NOT NULL REFERENCES curso(id),
    data_inscricao TIMESTAMP NOT NULL DEFAULT NOW(),
    concluido      BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (aluno_id, curso_id)
);

-- Slots de prova prática com controle de vagas
CREATE TABLE slot_prova_pratica (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    modulo_id      UUID NOT NULL REFERENCES modulo(id),
    data           TIMESTAMP NOT NULL,
    local          VARCHAR(255) NOT NULL,
    vagas_totais   INTEGER NOT NULL,
    vagas_ocupadas INTEGER NOT NULL DEFAULT 0
);

-- Agendamentos de alunos em slots
CREATE TABLE agendamento_prova (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL REFERENCES usuario(id),
    slot_id  UUID NOT NULL REFERENCES slot_prova_pratica(id),
    UNIQUE (aluno_id, slot_id)
);

-- Certificados com QR Code verificável
CREATE TABLE certificado (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id   UUID NOT NULL REFERENCES usuario(id),
    curso_id   UUID NOT NULL REFERENCES curso(id),
    qr_code    VARCHAR(255) NOT NULL UNIQUE,
    url_pdf    VARCHAR(500),
    emitido_em TIMESTAMP NOT NULL DEFAULT NOW(),
    valido     BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (aluno_id, curso_id)
);

CREATE INDEX idx_certificado_qr ON certificado(qr_code);
CREATE INDEX idx_certificado_aluno ON certificado(aluno_id);

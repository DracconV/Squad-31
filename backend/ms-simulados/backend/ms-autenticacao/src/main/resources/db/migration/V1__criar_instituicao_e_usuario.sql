-- Instituições da rede pública estadual de Sergipe
CREATE TABLE instituicao (
                             id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             nome        VARCHAR(255) NOT NULL,
                             municipio   VARCHAR(100) NOT NULL,
                             codigo_inep VARCHAR(20) UNIQUE,
                             ativo       BOOLEAN NOT NULL DEFAULT TRUE,
                             criado_em   TIMESTAMP NOT NULL DEFAULT NOW(),
                             atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Usuários do sistema — alunos, professores e admins
CREATE TABLE usuario (
                         id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         nome           VARCHAR(255) NOT NULL,
                         cpf            VARCHAR(11) UNIQUE,
                         matricula      VARCHAR(50) NOT NULL,
                         email          VARCHAR(255),
                         senha_hash     VARCHAR(255) NOT NULL,
                         perfil         VARCHAR(30) NOT NULL CHECK (perfil IN ('ALUNO_EM','ALUNO_EJA','ALUNO_PROF','PROFESSOR','ADMIN_ESCOLA','ADMIN_SEED')),
                         primeiro_acesso BOOLEAN NOT NULL DEFAULT TRUE,
                         ativo          BOOLEAN NOT NULL DEFAULT TRUE,
                         instituicao_id UUID REFERENCES instituicao(id),
                         criado_em      TIMESTAMP NOT NULL DEFAULT NOW(),
                         atualizado_em  TIMESTAMP NOT NULL DEFAULT NOW(),
                         UNIQUE (matricula, instituicao_id)
);

CREATE INDEX idx_usuario_matricula ON usuario(matricula);
CREATE INDEX idx_usuario_perfil ON usuario(perfil);
CREATE INDEX idx_usuario_instituicao ON usuario(instituicao_id);
-- Adiciona nivel_ensino à tabela questao
ALTER TABLE questao
    ADD COLUMN nivel_ensino VARCHAR(20) NOT NULL DEFAULT 'MEDIO'
    CHECK (nivel_ensino IN ('FUNDAMENTAL', 'MEDIO', 'PROFISSIONALIZANTE'));

-- Todas as questões ENEM existentes são de nível médio
UPDATE questao SET nivel_ensino = 'MEDIO';

CREATE INDEX idx_questao_nivel_ensino ON questao(nivel_ensino);

-- Disciplinas para o ensino fundamental (não existiam antes)
INSERT INTO disciplina (id, nome) VALUES
    (gen_random_uuid(), 'Ciências'),
    (gen_random_uuid(), 'Matemática Fundamental'),
    (gen_random_uuid(), 'Português Fundamental');

-- Disciplinas para o ensino profissionalizante
INSERT INTO disciplina (id, nome) VALUES
    (gen_random_uuid(), 'Informática Básica'),
    (gen_random_uuid(), 'Administração e Empreendedorismo'),
    (gen_random_uuid(), 'Saúde e Segurança do Trabalho'),
    (gen_random_uuid(), 'Legislação Trabalhista');

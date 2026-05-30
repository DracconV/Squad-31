-- Explicação/comentário da questão (feedback pós-simulado e estudo dirigido)
ALTER TABLE questao ADD COLUMN IF NOT EXISTS explicacao TEXT;

-- Questões favoritadas/marcadas para revisão pelo aluno
CREATE TABLE IF NOT EXISTS questao_favorita (
    aluno_id   UUID      NOT NULL,
    questao_id UUID      NOT NULL,
    criado_em  TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (aluno_id, questao_id)
);

CREATE INDEX IF NOT EXISTS idx_questao_favorita_aluno ON questao_favorita(aluno_id);

-- Vincula o professor responsável à turma
-- Nullable para não quebrar dados existentes
ALTER TABLE turma ADD COLUMN IF NOT EXISTS professor_id UUID REFERENCES usuario(id);

CREATE INDEX IF NOT EXISTS idx_turma_professor ON turma(professor_id);

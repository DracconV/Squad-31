-- Simulado avulso (sem turma) também gera desempenho do aluno
ALTER TABLE desempenho_aluno ALTER COLUMN turma_id DROP NOT NULL;

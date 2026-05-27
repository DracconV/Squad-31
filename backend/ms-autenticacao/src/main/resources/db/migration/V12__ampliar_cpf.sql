-- Amplia CPF para aceitar formato com máscara (000.000.000-00 = 14 chars)
ALTER TABLE usuario ALTER COLUMN cpf TYPE VARCHAR(14);

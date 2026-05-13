-- Disciplinas base do currículo do ensino médio
INSERT INTO disciplina (id, nome) VALUES
    (gen_random_uuid(), 'Matemática'),
    (gen_random_uuid(), 'Língua Portuguesa'),
    (gen_random_uuid(), 'História'),
    (gen_random_uuid(), 'Geografia'),
    (gen_random_uuid(), 'Ciências da Natureza'),
    (gen_random_uuid(), 'Física'),
    (gen_random_uuid(), 'Química'),
    (gen_random_uuid(), 'Biologia'),
    (gen_random_uuid(), 'Filosofia'),
    (gen_random_uuid(), 'Sociologia'),
    (gen_random_uuid(), 'Artes'),
    (gen_random_uuid(), 'Educação Física'),
    (gen_random_uuid(), 'Língua Inglesa'),
    (gen_random_uuid(), 'Redação');

-- Instituição padrão para desenvolvimento
INSERT INTO instituicao (id, nome, municipio, codigo_inep) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Escola Estadual de Desenvolvimento', 'Aracaju', '28000001');

-- Admin padrão — senha: admin123 (trocar em produção)
INSERT INTO usuario (id, nome, matricula, senha_hash, perfil, primeiro_acesso, instituicao_id) VALUES
    (
        gen_random_uuid(),
        'Administrador SEED',
        'admin',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
        'ADMIN_SEED',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    );

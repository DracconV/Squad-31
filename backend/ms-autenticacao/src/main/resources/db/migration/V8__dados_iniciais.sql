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

-- Cursos de desenvolvimento
INSERT INTO curso (id, nome, descricao, ativo) VALUES
    (gen_random_uuid(), 'Matematica Basica',  'Fundamentos de matematica para o ensino medio: algebra, geometria e estatistica.', true),
    (gen_random_uuid(), 'Lingua Portuguesa',  'Interpretacao de texto, gramatica e redacao para o ENEM.', true),
    (gen_random_uuid(), 'Historia do Brasil', 'Do periodo colonial a republica contemporanea.', true),
    (gen_random_uuid(), 'Biologia Celular',   'Celula, metabolismo, genetica e evolucao.', true),
    (gen_random_uuid(), 'Fisica Mecanica',    'Cinematica, dinamica, energia e ondas.', true),
    (gen_random_uuid(), 'Quimica Organica',   'Funcoes organicas, reacoes e aplicacoes do cotidiano.', true),
    (gen_random_uuid(), 'Ingles Basico',      'Vocabulario, gramatica e conversacao para iniciantes.', true),
    (gen_random_uuid(), 'Redacao ENEM',       'Tecnicas de dissertacao-argumentativa e repertorio sociocultural.', true);

-- Usuários de desenvolvimento — todos com senha: seed@2025
INSERT INTO usuario (id, nome, matricula, senha_hash, perfil, primeiro_acesso, instituicao_id) VALUES
    (
        gen_random_uuid(),
        'Administrador SEED',
        'admin',
        '$2a$10$9fSHviWJrtFArkp.ONGFnORmpu//IIcPj2GNW/xECl2K1YkMHLzAm',
        'ADMIN_SEED',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    ),
    (
        gen_random_uuid(),
        'Admin Escola Teste',
        'adminescola',
        '$2a$10$9fSHviWJrtFArkp.ONGFnORmpu//IIcPj2GNW/xECl2K1YkMHLzAm',
        'ADMIN_ESCOLA',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    ),
    (
        gen_random_uuid(),
        'Professor Teste',
        'professor01',
        '$2a$10$9fSHviWJrtFArkp.ONGFnORmpu//IIcPj2GNW/xECl2K1YkMHLzAm',
        'PROFESSOR',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    ),
    (
        gen_random_uuid(),
        'Aluno Ensino Medio Teste',
        'aluno-em',
        '$2a$10$9fSHviWJrtFArkp.ONGFnORmpu//IIcPj2GNW/xECl2K1YkMHLzAm',
        'ALUNO_EM',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    ),
    (
        gen_random_uuid(),
        'Aluno EJA Teste',
        'aluno-eja',
        '$2a$10$9fSHviWJrtFArkp.ONGFnORmpu//IIcPj2GNW/xECl2K1YkMHLzAm',
        'ALUNO_EJA',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    ),
    (
        gen_random_uuid(),
        'Aluno Profissional Teste',
        'aluno-prof',
        '$2a$10$9fSHviWJrtFArkp.ONGFnORmpu//IIcPj2GNW/xECl2K1YkMHLzAm',
        'ALUNO_PROF',
        FALSE,
        '00000000-0000-0000-0000-000000000001'
    );

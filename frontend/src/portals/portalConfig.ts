import type { Perfil } from '../lib/auth'

export interface PanelConfig {
  titulo: string
  itens: string[]
}

export interface PortalConfig {
  titulo: string
  subtitulo: string
  gridCols: string
  panels: PanelConfig[]
}

export const PORTAL_CONFIG: Record<Perfil, PortalConfig> = {
  ALUNO_EM: {
    titulo: 'Aluno · Ensino Médio',
    subtitulo: 'Estude, simule e acompanhe seu desempenho',
    gridCols: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3',
    panels: [
      {
        titulo: 'Banco de questões',
        itens: [
          'Filtrar por disciplina e assunto',
          'Resolver com correção imediata',
          'Histórico de acertos por tópico',
        ],
      },
      {
        titulo: 'Simulados',
        itens: [
          'Lista de simulados disponíveis',
          'Cronômetro e auto-save',
          'Resultado e gabarito comentado',
        ],
      },
      {
        titulo: 'Diagnóstico adaptativo',
        itens: [
          'Mapa de lacunas por disciplina',
          'Sugestão de questões personalizadas',
          'Evolução semanal',
        ],
      },
    ],
  },

  ALUNO_EJA: {
    titulo: 'Aluno · EJA',
    subtitulo: 'PWA leve, com auto-save e foco em mobile',
    gridCols: 'grid gap-6 sm:grid-cols-2',
    panels: [
      {
        titulo: 'Continuar de onde parei',
        itens: [
          'Auto-save de respostas no Redis',
          'Notificações contextuais',
          'Modo offline (PWA)',
        ],
      },
      {
        titulo: 'Atividades da semana',
        itens: [
          'Lista priorizada por professor',
          'Foco em vídeos curtos e questões',
          'Recompensa por sequência de dias',
        ],
      },
    ],
  },

  ALUNO_PROF: {
    titulo: 'Aluno · Profissionalizante',
    subtitulo: 'Cursos técnicos, módulos e provas práticas',
    gridCols: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3',
    panels: [
      {
        titulo: 'Meus cursos',
        itens: [
          'Listagem de inscrições ativas',
          'Progresso por módulo',
          'Pré-requisitos desbloqueados',
        ],
      },
      {
        titulo: 'Agendar prova prática',
        itens: [
          'Slots por unidade e horário',
          'Controle de vagas',
          'Confirmação por e-mail',
        ],
      },
      {
        titulo: 'Certificados',
        itens: [
          'Download em PDF',
          'QR Code de validação pública',
          'Compartilhar link',
        ],
      },
    ],
  },

  PROFESSOR: {
    titulo: 'Professor',
    subtitulo: 'Gestão de questões, simulados e turmas',
    gridCols: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3',
    panels: [
      {
        titulo: 'Banco de questões',
        itens: [
          'Cadastrar e revisar questões',
          'Categorização automática (IA)',
          'Importar de planilha',
        ],
      },
      {
        titulo: 'Simulados',
        itens: [
          'Montar simulado por tópico',
          'Embaralhamento inteligente',
          'Liberar para uma ou várias turmas',
        ],
      },
      {
        titulo: 'Acompanhamento de turmas',
        itens: [
          'Desempenho agregado',
          'Aluno em atenção',
          'Comparativo entre turmas',
        ],
      },
    ],
  },

  ADMIN_ESCOLA: {
    titulo: 'Administração · Escola',
    subtitulo: 'Gestão institucional da unidade',
    gridCols: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3',
    panels: [
      {
        titulo: 'Importar alunos',
        itens: [
          'Upload de CSV em lote',
          'Validação de matrícula e CPF',
          'Relatório de erros',
        ],
      },
      {
        titulo: 'Turmas e professores',
        itens: [
          'Criar e editar turmas',
          'Vincular professores',
          'Listar alunos por turma',
        ],
      },
      {
        titulo: 'Painel da escola',
        itens: [
          'Adesão à plataforma',
          'Desempenho geral',
          'Comparativo entre turmas',
        ],
      },
    ],
  },

  ADMIN_SEED: {
    titulo: 'Administração · SEED',
    subtitulo: 'Painel macro de inteligência educacional',
    gridCols: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-3',
    panels: [
      {
        titulo: 'Visão por município',
        itens: [
          'Comparativo entre municípios',
          'Adesão e desempenho',
          'Mapa de calor por disciplina',
        ],
      },
      {
        titulo: 'Diagnóstico estadual',
        itens: [
          'Lacunas mais frequentes',
          'Tendências por série',
          'Exportar relatório PDF',
        ],
      },
      {
        titulo: 'Auditoria',
        itens: [
          'Logs de acesso',
          'Inferências de IA',
          'Conformidade LGPD',
        ],
      },
    ],
  },
}

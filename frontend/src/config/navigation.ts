import type { Perfil } from '../lib/auth'

export interface NavItem {
  label: string
  path: string
  /** caminhos alternativos que também ativam o item no menu */
  aliases?: string[]
}

export interface NavConfig {
  defaultPath: string
  items: NavItem[]
}

const NAV: Record<Perfil, NavConfig> = {
  ALUNO_EM: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard' },
      { label: 'Simulados',         path: '/simulados', aliases: ['/simulados/'] },
      { label: 'Banco de questões', path: '/banco-questoes' },
      { label: 'Histórico',         path: '/historico' },
      { label: 'Desempenho',        path: '/desempenho' },
      { label: 'Notas',             path: '/notas' },
      { label: 'Local de prova',    path: '/local-prova' },
      { label: 'Certificados',      path: '/certificados' },
      { label: 'Perfil',            path: '/perfil' },
    ],
  },

  ALUNO_EJA: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard' },
      { label: 'Simulados',         path: '/simulados', aliases: ['/simulados/'] },
      { label: 'Banco de questões', path: '/banco-questoes' },
      { label: 'Histórico',         path: '/historico' },
      { label: 'Desempenho',        path: '/desempenho' },
      { label: 'Notas',             path: '/notas' },
      { label: 'Local de prova',    path: '/local-prova' },
      { label: 'Certificados',      path: '/certificados' },
      { label: 'Perfil',            path: '/perfil' },
    ],
  },

  ALUNO_PROF: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard' },
      { label: 'Simulados',         path: '/simulados', aliases: ['/simulados/'] },
      { label: 'Banco de questões', path: '/banco-questoes' },
      { label: 'Histórico',         path: '/historico' },
      { label: 'Desempenho',        path: '/desempenho' },
      { label: 'Notas',             path: '/notas' },
      { label: 'Local de prova',    path: '/local-prova' },
      { label: 'Certificados',      path: '/certificados' },
      { label: 'Perfil',            path: '/perfil' },
    ],
  },

  PROFESSOR: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard' },
      { label: 'Provas',            path: '/provas' },
      { label: 'Criar prova',       path: '/criar-prova' },
      { label: 'Banco de questões', path: '/banco-questoes' },
      { label: 'Turmas',            path: '/turmas' },
      { label: 'Notas',             path: '/notas' },
      { label: 'Relatórios',        path: '/relatorios' },
      { label: 'Perfil',            path: '/perfil' },
    ],
  },

  ADMIN_ESCOLA: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard' },
      { label: 'Turmas',            path: '/turmas' },
      { label: 'Usuários',          path: '/gestao-usuarios' },
      { label: 'Relatórios',        path: '/relatorios' },
      { label: 'Avaliações',        path: '/avaliacoes' },
      { label: 'Perfil',            path: '/perfil' },
    ],
  },

  ADMIN_SEED: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard' },
      { label: 'Escolas',           path: '/escolas' },
      { label: 'Usuários',          path: '/gestao-usuarios' },
      { label: 'Relatórios',        path: '/relatorios' },
      { label: 'Avaliações',        path: '/avaliacoes' },
      { label: 'Perfil',            path: '/perfil' },
    ],
  },
}

export function getNavConfig(perfil: Perfil): NavConfig {
  return NAV[perfil]
}

export function isPathAllowed(perfil: Perfil, pathname: string): boolean {
  const { items } = NAV[perfil]
  return items.some(
    (item) =>
      pathname === item.path ||
      pathname.startsWith(item.path + '/') ||
      item.aliases?.some((a) => pathname.startsWith(a)),
  )
}

import type { Perfil } from '../lib/auth'
import {
  Home, BookOpen, FileText, Database, History, TrendingUp, Award,
  CalendarCheck, MapPin, BadgeCheck, User, ClipboardList, FilePlus,
  Users, BarChart3, UserCog, Building2, ClipboardCheck,
  type LucideIcon,
} from 'lucide-react'

export interface NavItem {
  label: string
  path: string
  icon: LucideIcon
  /** caminhos alternativos que também ativam o item no menu */
  aliases?: string[]
}

export interface NavConfig {
  defaultPath: string
  items: NavItem[]
}

const ITENS_ALUNO: NavItem[] = [
  { label: 'Início',            path: '/dashboard',      icon: Home },
  { label: 'Cursos',            path: '/cursos',         icon: BookOpen },
  { label: 'Simulados',         path: '/simulados',      icon: FileText, aliases: ['/simulados/'] },
  { label: 'Banco de questões', path: '/banco-questoes', icon: Database },
  { label: 'Histórico',         path: '/historico',      icon: History },
  { label: 'Desempenho',        path: '/desempenho',     icon: TrendingUp },
  { label: 'Notas',             path: '/notas',          icon: Award },
  { label: 'Faltas',            path: '/faltas',         icon: CalendarCheck },
  { label: 'Local de prova',    path: '/local-prova',    icon: MapPin },
  { label: 'Certificados',      path: '/certificados',   icon: BadgeCheck },
  { label: 'Perfil',            path: '/perfil',         icon: User },
]

const NAV: Record<Perfil, NavConfig> = {
  ALUNO_EM:   { defaultPath: '/dashboard', items: ITENS_ALUNO },
  ALUNO_EJA:  { defaultPath: '/dashboard', items: ITENS_ALUNO },
  ALUNO_PROF: { defaultPath: '/dashboard', items: ITENS_ALUNO },

  PROFESSOR: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',            path: '/dashboard',      icon: Home },
      { label: 'Cursos',            path: '/cursos',         icon: BookOpen },
      { label: 'Provas',            path: '/provas',         icon: ClipboardList },
      { label: 'Criar prova',       path: '/criar-prova',    icon: FilePlus },
      { label: 'Banco de questões', path: '/banco-questoes', icon: Database },
      { label: 'Turmas',            path: '/turmas',         icon: Users },
      { label: 'Notas',             path: '/notas',          icon: Award },
      { label: 'Faltas',            path: '/faltas',         icon: CalendarCheck },
      { label: 'Relatórios',        path: '/relatorios',     icon: BarChart3 },
      { label: 'Perfil',            path: '/perfil',         icon: User },
    ],
  },

  ADMIN_ESCOLA: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',     path: '/dashboard',       icon: Home },
      { label: 'Turmas',     path: '/turmas',          icon: Users },
      { label: 'Usuários',   path: '/gestao-usuarios', icon: UserCog },
      { label: 'Relatórios', path: '/relatorios',      icon: BarChart3 },
      { label: 'Avaliações', path: '/avaliacoes',      icon: ClipboardCheck },
      { label: 'Perfil',     path: '/perfil',          icon: User },
    ],
  },

  ADMIN_SEED: {
    defaultPath: '/dashboard',
    items: [
      { label: 'Início',     path: '/dashboard',       icon: Home },
      { label: 'Escolas',    path: '/escolas',         icon: Building2 },
      { label: 'Usuários',   path: '/gestao-usuarios', icon: UserCog },
      { label: 'Relatórios', path: '/relatorios',      icon: BarChart3 },
      { label: 'Avaliações', path: '/avaliacoes',      icon: ClipboardCheck },
      { label: 'Perfil',     path: '/perfil',          icon: User },
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

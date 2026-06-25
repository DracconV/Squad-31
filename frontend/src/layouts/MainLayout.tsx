import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { LogOut, Star } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { getNavConfig } from '../config/navigation'
import type { Perfil } from '../lib/auth'
import NotificationBell from '../components/NotificationBell'

const PAGE_TITLES: Record<string, string> = {
  '/dashboard':      'Início',
  '/simulados':      'Simulados',
  '/banco-questoes': 'Banco de Questões',
  '/historico':      'Histórico',
  '/desempenho':     'Desempenho',
  '/notas':          'Notas',
  '/faltas':         'Faltas',
  '/local-prova':    'Local de Prova',
  '/certificados':   'Certificados',
  '/provas':         'Provas',
  '/criar-prova':    'Criar Prova',
  '/turmas':         'Turmas',
  '/relatorios':     'Relatórios',
  '/gestao-usuarios':'Gestão de Usuários',
  '/escolas':        'Escolas',
  '/avaliacoes':     'Avaliações',
  '/cursos':         'Cursos',
  '/perfil':         'Meu Perfil',
}

const PERFIL_LABEL: Record<string, string> = {
  ALUNO_EM: 'Aluno · Ensino Médio',
  ALUNO_EJA: 'Aluno · EJA',
  ALUNO_PROF: 'Aluno · Profissionalizante',
  PROFESSOR: 'Professor',
  ADMIN_ESCOLA: 'Admin · Escola',
  ADMIN_SEED: 'Admin · SEED',
}

function getPageTitle(pathname: string): string {
  const exact = PAGE_TITLES[pathname]
  if (exact) return exact
  const match = Object.keys(PAGE_TITLES).find((k) => pathname.startsWith(k + '/'))
  return match ? PAGE_TITLES[match] : 'SEED Educa'
}

export default function MainLayout() {
  const { user, signOut } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  if (!user) return null

  const { items } = getNavConfig(user.perfil as Perfil)
  const title = getPageTitle(location.pathname)
  const inicial = user.nome?.charAt(0).toUpperCase() ?? 'U'

  function handleLogout() {
    signOut()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex h-screen overflow-hidden bg-[#f6f8f7]">

      {/* Skip-to-content */}
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-[100]
                   focus:px-4 focus:py-2 focus:bg-brand-700 focus:text-white focus:rounded-lg focus:text-sm focus:font-medium"
      >
        Ir para o conteúdo principal
      </a>

      {/* ── Sidebar (desktop) ───────────────────────────────── */}
      <aside
        aria-label="Menu de navegação principal"
        className="hidden lg:flex flex-col w-[264px] shrink-0 bg-gradient-to-b from-brand-700 to-brand-900 text-brand-50"
      >
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 h-16 border-b border-white/10">
          <div className="w-9 h-9 rounded-xl bg-gold-400 text-brand-900 flex items-center justify-center shadow-sm">
            <Star size={18} fill="currentColor" />
          </div>
          <div className="leading-tight">
            <span className="block font-extrabold text-white text-[15px] tracking-tight">SEED Educa</span>
            <span className="block text-[11px] text-brand-100/80">Rede pública · Sergipe</span>
          </div>
        </div>

        {/* Nav */}
        <nav aria-label="Navegação principal" className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {items.map((item) => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.path}
                to={item.path}
                aria-label={item.label}
                className={({ isActive }) =>
                  `group flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-colors relative ${
                    isActive
                      ? 'bg-white/15 text-white'
                      : 'text-brand-50/85 hover:bg-white/10 hover:text-white'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {isActive && <span className="absolute left-0 top-1.5 bottom-1.5 w-1 rounded-full bg-gold-400" />}
                    <Icon size={18} className={isActive ? 'text-gold-300' : ''} />
                    {item.label}
                  </>
                )}
              </NavLink>
            )
          })}
        </nav>

        {/* Usuário + sair */}
        <div className="px-3 py-3 border-t border-white/10">
          <div className="flex items-center gap-3 px-2.5 py-2 rounded-xl">
            <div className="w-9 h-9 rounded-full bg-gold-400 text-brand-900 flex items-center justify-center text-sm font-bold shrink-0">
              {inicial}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-white truncate">{user.nome}</p>
              <p className="text-[11px] text-brand-100/70 truncate">{PERFIL_LABEL[user.perfil] ?? user.perfil}</p>
            </div>
            <button
              onClick={handleLogout}
              aria-label="Sair da conta"
              title="Sair"
              className="p-2 rounded-lg text-brand-100/80 hover:bg-white/10 hover:text-white"
            >
              <LogOut size={17} />
            </button>
          </div>
        </div>
      </aside>

      {/* ── Conteúdo ────────────────────────────────────────── */}
      <div className="flex flex-col flex-1 overflow-hidden">

        {/* Header */}
        <header
          role="banner"
          className="h-16 bg-white/80 backdrop-blur border-b border-gray-200/70 px-5 sm:px-7 flex items-center justify-between shrink-0"
        >
          <h1 className="text-lg font-bold text-gray-900 tracking-tight">{title}</h1>
          <div className="flex items-center gap-2 sm:gap-3" aria-label={`Sessão de ${user.nome}`}>
            <NotificationBell />
            <div className="hidden sm:flex items-center gap-2.5 pl-2 sm:border-l border-gray-200">
              <div className="text-right leading-tight">
                <p className="text-sm font-semibold text-gray-800">{user.nome}</p>
                <p className="text-[11px] text-gray-400">{PERFIL_LABEL[user.perfil] ?? user.perfil}</p>
              </div>
              <div className="w-9 h-9 rounded-full bg-brand-600 flex items-center justify-center text-white text-sm font-bold">
                {inicial}
              </div>
            </div>
          </div>
        </header>

        {/* Página */}
        <main id="main-content" role="main" className="flex-1 overflow-y-auto p-5 sm:p-7">
          <Outlet />
        </main>

        {/* ── Bottom nav (mobile) ─────────────────────────── */}
        <nav aria-label="Navegação rápida" className="lg:hidden flex border-t border-gray-200 bg-white">
          {items.slice(0, 5).map((item) => {
            const Icon = item.icon
            return (
              <NavLink
                key={item.path}
                to={item.path}
                aria-label={item.label}
                className={({ isActive }) =>
                  `flex-1 flex flex-col items-center py-2 text-[10px] font-medium transition-colors ${
                    isActive ? 'text-brand-700' : 'text-gray-500'
                  }`
                }
              >
                <Icon size={20} className="mb-0.5" />
                {item.label}
              </NavLink>
            )
          })}
        </nav>

      </div>
    </div>
  )
}

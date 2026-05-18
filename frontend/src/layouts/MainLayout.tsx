import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { getNavConfig } from '../config/navigation'
import type { Perfil } from '../lib/auth'

const PAGE_TITLES: Record<string, string> = {
  '/dashboard':      'Início',
  '/simulados':      'Simulados',
  '/banco-questoes': 'Banco de Questões',
  '/historico':      'Histórico',
  '/desempenho':     'Desempenho',
  '/notas':          'Notas',
  '/local-prova':    'Local de Prova',
  '/certificados':   'Certificados',
  '/provas':         'Provas',
  '/criar-prova':    'Criar Prova',
  '/turmas':         'Turmas',
  '/relatorios':     'Relatórios',
  '/gestao-usuarios':'Gestão de Usuários',
  '/escolas':        'Escolas',
  '/avaliacoes':     'Avaliações',
  '/perfil':         'Meu Perfil',
}

function getPageTitle(pathname: string): string {
  const exact = PAGE_TITLES[pathname]
  if (exact) return exact
  const match = Object.keys(PAGE_TITLES).find((k) => pathname.startsWith(k + '/'))
  return match ? PAGE_TITLES[match] : 'SEED Educa'
}

export default function MainLayout() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()

  if (!user) return null

  const { items } = getNavConfig(user.perfil as Perfil)
  const title = getPageTitle(location.pathname)

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="flex h-screen overflow-hidden bg-[#F4F6F9]">

      {/* ── Sidebar (desktop) ───────────────────────── */}
      <aside
        className="hidden lg:flex flex-col w-[260px] shrink-0 text-[#CBD5E1]"
        style={{ backgroundColor: 'var(--sidebar-bg)' }}
      >
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-white/10">
          <div className="w-8 h-8 rounded-lg bg-blue-500 flex items-center justify-center text-white font-bold text-sm">
            S
          </div>
          <span className="font-semibold text-white text-sm">SEED Educa</span>
        </div>

        {/* Nav items */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {items.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-300 hover:bg-white/10 hover:text-white'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* User info + logout */}
        <div className="px-4 py-4 border-t border-white/10">
          <p className="text-xs text-slate-400 truncate mb-1">{user.nome}</p>
          <p className="text-[11px] text-slate-500 truncate mb-3">{user.perfil}</p>
          <button
            onClick={handleLogout}
            className="w-full text-xs text-slate-400 hover:text-white py-1.5 px-3 rounded hover:bg-white/10 transition-colors text-left"
          >
            Sair
          </button>
        </div>
      </aside>

      {/* ── Main content ────────────────────────────── */}
      <div className="flex flex-col flex-1 overflow-hidden">

        {/* Header */}
        <header className="h-14 bg-white border-b border-gray-200 px-6 flex items-center justify-between shrink-0 shadow-sm">
          <h1 className="text-base font-semibold text-gray-800">{title}</h1>
          <div className="flex items-center gap-3">
            <span className="hidden sm:block text-sm text-gray-500">{user.nome}</span>
            <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white text-xs font-bold">
              {user.nome?.charAt(0).toUpperCase() ?? 'U'}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>

        {/* ── Bottom nav (mobile) ─────────────────── */}
        <nav className="lg:hidden flex border-t border-gray-200 bg-white">
          {items.slice(0, 5).map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `flex-1 flex flex-col items-center py-2 text-[10px] font-medium transition-colors ${
                  isActive ? 'text-blue-600' : 'text-gray-500'
                }`
              }
            >
              <span className="text-lg mb-0.5">·</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

      </div>
    </div>
  )
}

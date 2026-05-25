import type { ReactNode } from 'react'
import { useAuth } from '../contexts/AuthContext'

interface PortalLayoutProps {
  titulo: string
  subtitulo?: string
  children?: ReactNode
}

/**
 * Layout simples compartilhado pelos placeholders dos portais.
 * Conforme cada portal evoluir, ele pode trocar este layout
 * por um próprio com menu lateral, breadcrumbs etc.
 */
export default function PortalLayout({
  titulo,
  subtitulo,
  children,
}: PortalLayoutProps) {
  const { user, signOut } = useAuth()

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <div>
            <h1 className="text-lg font-semibold text-slate-900">
              SEED Educa · {titulo}
            </h1>
            {subtitulo && (
              <p className="text-sm text-slate-500">{subtitulo}</p>
            )}
          </div>
          <div className="flex items-center gap-4">
            {user?.nome && (
              <span className="hidden text-sm text-slate-600 sm:inline">
                {user.nome}
              </span>
            )}
            <button
              type="button"
              onClick={signOut}
              className="rounded-md border border-slate-300 bg-white px-3 py-1.5 text-sm text-slate-700 shadow-sm transition hover:bg-slate-100"
            >
              Sair
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-6 py-8">{children}</main>
    </div>
  )
}

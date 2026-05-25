import { Navigate, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from '../contexts/AuthContext'
import type { Perfil } from '../lib/auth'

interface ProtectedRouteProps {
  children: ReactNode
  /**
   * Restringe o acesso a um conjunto de perfis. Se omitido,
   * basta estar autenticado.
   */
  perfis?: Perfil[]
}

export function ProtectedRoute({ children, perfis }: ProtectedRouteProps) {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center text-slate-500">
        Carregando…
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (perfis && !perfis.includes(user.perfil)) {
    return <Navigate to="/sem-acesso" replace />
  }

  return <>{children}</>
}

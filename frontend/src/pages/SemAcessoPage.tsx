import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { ROTA_POR_PERFIL } from '../lib/auth'

export default function SemAcessoPage() {
  const { user } = useAuth()
  const destino = user ? (ROTA_POR_PERFIL[user.perfil] ?? '/') : '/login'

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="max-w-md text-center">
        <h1 className="mb-2 text-2xl font-bold text-slate-900">
          Sem permissão
        </h1>
        <p className="mb-6 text-sm text-slate-600">
          Você não tem acesso a esta área da plataforma.
        </p>
        <Link
          to={destino}
          className="inline-block rounded-lg bg-brand-700 px-4 py-2 font-medium text-white shadow transition hover:bg-brand-800"
        >
          Voltar para o início
        </Link>
      </div>
    </div>
  )
}

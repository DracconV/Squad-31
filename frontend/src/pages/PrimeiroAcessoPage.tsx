import { useState, type FormEvent } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { AxiosError } from 'axios'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { ROTA_POR_PERFIL } from '../lib/auth'

interface LocationState {
  matricula: string
  senhaTemporaria: string
  perfil: string
}

export default function PrimeiroAcessoPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { signIn } = useAuth()

  const state = location.state as LocationState | null
  const matricula = state?.matricula ?? ''
  const senhaTemporaria = state?.senhaTemporaria ?? ''

  const [novaSenha, setNovaSenha] = useState('')
  const [confirmar, setConfirmar] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setErro(null)

    if (novaSenha.length < 6) {
      setErro('A nova senha deve ter pelo menos 6 caracteres.')
      return
    }
    if (novaSenha !== confirmar) {
      setErro('As senhas não coincidem.')
      return
    }

    setLoading(true)
    try {
      await api.post('/auth/primeiro-acesso', {
        matricula,
        senhaTemporaria,
        novaSenha,
      })

      // Faz login automático com a nova senha
      const u = await signIn({ matricula, senha: novaSenha })
      const destino = ROTA_POR_PERFIL[u.perfil] ?? '/'
      navigate(destino, { replace: true })
    } catch (err) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 400) {
          setErro('Senha temporária incorreta ou dados inválidos.')
        } else {
          setErro('Erro ao trocar a senha. Tente novamente.')
        }
      } else {
        setErro('Erro inesperado.')
      }
    } finally {
      setLoading(false)
    }
  }

  // Segurança: se não veio do login, volta para lá
  if (!matricula || !senhaTemporaria) {
    navigate('/login', { replace: true })
    return null
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl ring-1 ring-slate-200">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-blue-100">
            <span className="text-2xl">🔐</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-900">Primeiro acesso</h1>
          <p className="mt-1 text-sm text-slate-500">
            Crie uma senha pessoal para continuar.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Nova senha
            </label>
            <input
              type="password"
              required
              minLength={6}
              value={novaSenha}
              onChange={(e) => setNovaSenha(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
              placeholder="Mínimo 6 caracteres"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">
              Confirmar nova senha
            </label>
            <input
              type="password"
              required
              value={confirmar}
              onChange={(e) => setConfirmar(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
              placeholder="Repita a nova senha"
            />
          </div>

          {erro && (
            <div
              role="alert"
              className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700"
            >
              {erro}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-blue-700 px-4 py-2 font-medium text-white shadow transition hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-300 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {loading ? 'Salvando...' : 'Criar senha e entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}

import { useState, type FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { AxiosError } from 'axios'
import { useAuth } from '../contexts/AuthContext'
import { ROTA_POR_PERFIL } from '../lib/auth'

export default function LoginPage() {
  const { signIn } = useAuth()
  const navigate = useNavigate()

  const [matricula, setMatricula] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErro(null)
    setLoading(true)
    try {
      const u = await signIn({ matricula: matricula.trim(), senha })

      // Se for o primeiro acesso, redireciona para troca de senha
      if (u.primeiroAcesso) {
        navigate('/primeiro-acesso', {
          replace: true,
          state: { matricula: matricula.trim(), senhaTemporaria: senha, perfil: u.perfil },
        })
        return
      }

      const destino = ROTA_POR_PERFIL[u.perfil] ?? '/'
      navigate(destino, { replace: true })
    } catch (err) {
      if (err instanceof AxiosError) {
        if (err.response?.status === 401) {
          setErro('Matrícula ou senha incorretas.')
        } else if (err.code === 'ECONNABORTED' || !err.response) {
          setErro('Não foi possível conectar ao servidor. Tente novamente.')
        } else {
          setErro('Erro inesperado ao fazer login. Tente novamente.')
        }
      } else {
        setErro('Erro inesperado ao fazer login.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl ring-1 ring-slate-200">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-900">SEED Educa</h1>
          <p className="mt-1 text-sm text-slate-500">
            Plataforma educacional da rede pública de Sergipe
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label
              htmlFor="matricula"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Matrícula
            </label>
            <input
              id="matricula"
              type="text"
              autoComplete="username"
              required
              value={matricula}
              onChange={(e) => setMatricula(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
              placeholder="Sua matrícula"
            />
          </div>

          <div>
            <label
              htmlFor="senha"
              className="mb-1 block text-sm font-medium text-slate-700"
            >
              Senha
            </label>
            <input
              id="senha"
              type="password"
              autoComplete="current-password"
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
              placeholder="Sua senha"
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
            {loading ? 'Entrando…' : 'Entrar'}
          </button>
        </form>

        <div className="mt-6 text-center space-y-2">
          <Link
            to="/redefinir-senha"
            className="block text-xs text-blue-600 hover:underline focus:outline-none focus:ring-2 focus:ring-blue-300 rounded"
          >
            Esqueceu a senha? Redefinir agora
          </Link>
          <p className="text-xs text-slate-400">
            Ou procure a secretaria da sua escola.
          </p>
        </div>
      </div>
    </div>
  )
}

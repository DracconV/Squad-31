import { useState, type FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { AxiosError } from 'axios'
import { Star, GraduationCap, LogIn } from 'lucide-react'
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
        if (err.response?.status === 401) setErro('Matrícula ou senha incorretas.')
        else if (err.code === 'ECONNABORTED' || !err.response) setErro('Não foi possível conectar ao servidor. Tente novamente.')
        else setErro('Erro inesperado ao fazer login. Tente novamente.')
      } else {
        setErro('Erro inesperado ao fazer login.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-[#f6f8f7]">
      {/* ── Painel de marca (Sergipe) ──────────────────────── */}
      <aside className="relative hidden lg:flex flex-col justify-between p-12 text-white overflow-hidden bg-gradient-to-br from-brand-600 via-brand-700 to-brand-900">
        {/* estrelas decorativas (bandeira de Sergipe) */}
        <div className="absolute inset-0 opacity-[0.12] pointer-events-none">
          {[[10, 18], [78, 10], [40, 35], [85, 55], [20, 70], [60, 80], [90, 88]].map(([x, y], i) => (
            <Star key={i} size={28 + (i % 3) * 14} fill="currentColor"
              className="absolute text-gold-300" style={{ left: `${x}%`, top: `${y}%` }} />
          ))}
        </div>

        <div className="relative flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-gold-400 text-brand-900 flex items-center justify-center shadow-lg">
            <Star size={22} fill="currentColor" />
          </div>
          <span className="font-extrabold text-xl tracking-tight">SEED Educa</span>
        </div>

        <div className="relative">
          <h2 className="text-4xl font-extrabold leading-tight tracking-tight">
            A educação de Sergipe,<br />na palma da mão.
          </h2>
          <p className="mt-4 max-w-md text-brand-50/80 leading-relaxed">
            Simulados, banco de questões, desempenho e certificados — tudo num só lugar,
            para alunos, professores e gestores da rede pública estadual.
          </p>
        </div>

        <p className="relative text-xs text-brand-50/60">
          Secretaria de Estado da Educação · Governo de Sergipe
        </p>
      </aside>

      {/* ── Formulário ─────────────────────────────────────── */}
      <main className="flex items-center justify-center px-5 py-12">
        <div className="w-full max-w-sm">
          {/* logo mobile */}
          <div className="lg:hidden mb-8 flex flex-col items-center text-center">
            <div className="w-12 h-12 rounded-2xl bg-brand-600 text-white flex items-center justify-center shadow-md">
              <GraduationCap size={24} />
            </div>
            <h1 className="mt-3 text-xl font-extrabold text-gray-900">SEED Educa</h1>
            <p className="text-sm text-gray-500">Rede pública de Sergipe</p>
          </div>

          <div className="mb-7 hidden lg:block">
            <h1 className="text-2xl font-extrabold text-gray-900 tracking-tight">Bem-vindo de volta</h1>
            <p className="mt-1 text-sm text-gray-500">Entre com sua matrícula para continuar.</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label htmlFor="matricula" className="mb-1.5 block text-sm font-medium text-gray-700">Matrícula</label>
              <input
                id="matricula" type="text" autoComplete="username" required
                value={matricula} onChange={(e) => setMatricula(e.target.value)}
                className="w-full rounded-xl border border-gray-300 bg-white px-3.5 py-2.5 text-gray-900 shadow-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-200"
                placeholder="Sua matrícula"
              />
            </div>

            <div>
              <label htmlFor="senha" className="mb-1.5 block text-sm font-medium text-gray-700">Senha</label>
              <input
                id="senha" type="password" autoComplete="current-password" required
                value={senha} onChange={(e) => setSenha(e.target.value)}
                className="w-full rounded-xl border border-gray-300 bg-white px-3.5 py-2.5 text-gray-900 shadow-sm outline-none focus:border-brand-500 focus:ring-2 focus:ring-brand-200"
                placeholder="Sua senha"
              />
            </div>

            {erro && (
              <div role="alert" className="rounded-xl border border-red-200 bg-red-50 px-3.5 py-2.5 text-sm text-red-700">
                {erro}
              </div>
            )}

            <button
              type="submit" disabled={loading}
              className="w-full flex items-center justify-center gap-2 rounded-xl bg-brand-600 px-4 py-2.5 font-semibold text-white shadow-sm hover:bg-brand-700 focus:outline-none focus:ring-2 focus:ring-brand-300 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <LogIn size={18} />
              {loading ? 'Entrando…' : 'Entrar'}
            </button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <Link to="/redefinir-senha" className="block text-sm font-medium text-brand-700 hover:text-brand-800 hover:underline rounded">
              Esqueceu a senha? Redefinir agora
            </Link>
            <p className="text-xs text-gray-400">Ou procure a secretaria da sua escola.</p>
          </div>
        </div>
      </main>
    </div>
  )
}

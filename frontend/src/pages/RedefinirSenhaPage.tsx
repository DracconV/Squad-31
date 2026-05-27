import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { solicitarRedefinicao, redefinirSenha, type RedefinicaoResponse } from '../lib/api'

type Etapa = 'solicitar' | 'redefinir' | 'sucesso'

export default function RedefinirSenhaPage() {
  const [etapa, setEtapa] = useState<Etapa>('solicitar')
  const [matricula, setMatricula] = useState('')
  const [token, setToken] = useState('')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmarSenha, setConfirmarSenha] = useState('')
  const [resultado, setResultado] = useState<RedefinicaoResponse | null>(null)
  const [erro, setErro] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSolicitar(e: FormEvent) {
    e.preventDefault()
    if (!matricula.trim()) return setErro('Informe sua matrícula.')
    setErro('')
    setLoading(true)
    try {
      const res = await solicitarRedefinicao(matricula.trim())
      setResultado(res)
      setEtapa('redefinir')
    } catch {
      setErro('Matrícula não encontrada. Verifique e tente novamente.')
    } finally {
      setLoading(false)
    }
  }

  async function handleRedefinir(e: FormEvent) {
    e.preventDefault()
    if (novaSenha.length < 6) return setErro('A senha deve ter no mínimo 6 caracteres.')
    if (novaSenha !== confirmarSenha) return setErro('As senhas não coincidem.')
    setErro('')
    setLoading(true)
    try {
      await redefinirSenha(token || resultado!.token, novaSenha)
      setEtapa('sucesso')
    } catch {
      setErro('Token inválido ou expirado. Solicite um novo token.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl ring-1 ring-slate-200">

        {/* Header */}
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-slate-900">Redefinir Senha</h1>
          <p className="mt-1 text-sm text-slate-500">SEED Educa — rede pública de Sergipe</p>
        </div>

        {/* ── Etapa 1: Solicitar token ──────────────────── */}
        {etapa === 'solicitar' && (
          <form onSubmit={handleSolicitar} className="space-y-5" noValidate>
            <div>
              <label htmlFor="matricula" className="mb-1 block text-sm font-medium text-slate-700">
                Matrícula
              </label>
              <input
                id="matricula"
                type="text"
                autoComplete="username"
                required
                value={matricula}
                onChange={(e) => setMatricula(e.target.value)}
                aria-describedby={erro ? 'erro-msg' : undefined}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                placeholder="Sua matrícula"
              />
            </div>

            {erro && (
              <div id="erro-msg" role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {erro}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              aria-busy={loading}
              className="w-full rounded-lg bg-blue-700 px-4 py-2 font-medium text-white shadow transition hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-300 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? 'Gerando token…' : 'Gerar token de redefinição'}
            </button>
          </form>
        )}

        {/* ── Etapa 2: Usar token e nova senha ─────────── */}
        {etapa === 'redefinir' && (
          <form onSubmit={handleRedefinir} className="space-y-5" noValidate>
            {resultado && (
              <div role="status" className="rounded-lg bg-blue-50 border border-blue-200 p-4 text-sm text-blue-800">
                <p className="font-semibold mb-1">Token gerado com sucesso!</p>
                <p className="font-mono text-xs break-all bg-white rounded p-2 mt-1 select-all border border-blue-100">
                  {resultado.token}
                </p>
                <p className="text-xs text-blue-500 mt-2">
                  Válido até: {new Date(resultado.expiraEm).toLocaleString('pt-BR')}
                </p>
              </div>
            )}

            <div>
              <label htmlFor="token" className="mb-1 block text-sm font-medium text-slate-700">
                Token de redefinição
              </label>
              <input
                id="token"
                type="text"
                required
                value={token || resultado?.token || ''}
                onChange={(e) => setToken(e.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 text-xs font-mono shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                placeholder="Cole o token aqui"
              />
            </div>

            <div>
              <label htmlFor="nova-senha" className="mb-1 block text-sm font-medium text-slate-700">
                Nova senha
              </label>
              <input
                id="nova-senha"
                type="password"
                autoComplete="new-password"
                required
                minLength={6}
                value={novaSenha}
                onChange={(e) => setNovaSenha(e.target.value)}
                aria-describedby="senha-hint"
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                placeholder="Mínimo 6 caracteres"
              />
              <p id="senha-hint" className="text-xs text-slate-400 mt-1">
                A senha deve ter no mínimo 6 caracteres.
              </p>
            </div>

            <div>
              <label htmlFor="confirmar-senha" className="mb-1 block text-sm font-medium text-slate-700">
                Confirmar nova senha
              </label>
              <input
                id="confirmar-senha"
                type="password"
                autoComplete="new-password"
                required
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                className="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 shadow-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-200"
                placeholder="Repita a nova senha"
              />
            </div>

            {erro && (
              <div role="alert" className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {erro}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              aria-busy={loading}
              className="w-full rounded-lg bg-blue-700 px-4 py-2 font-medium text-white shadow transition hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-300 disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? 'Redefinindo…' : 'Redefinir senha'}
            </button>
          </form>
        )}

        {/* ── Etapa 3: Sucesso ──────────────────────────── */}
        {etapa === 'sucesso' && (
          <div className="text-center space-y-4">
            <div role="status" aria-live="polite" className="text-green-600 text-5xl">✓</div>
            <p className="text-slate-700 font-medium">Senha redefinida com sucesso!</p>
            <p className="text-sm text-slate-500">Você já pode fazer login com sua nova senha.</p>
          </div>
        )}

        <div className="mt-6 text-center">
          <Link
            to="/login"
            className="text-xs text-blue-600 hover:underline focus:outline-none focus:ring-2 focus:ring-blue-300 rounded"
          >
            ← Voltar para o login
          </Link>
        </div>
      </div>
    </div>
  )
}

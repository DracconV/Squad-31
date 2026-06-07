import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { listarUsuarios, criarUsuario, desativarUsuario, reativarUsuario, importarAlunos, listarInstituicoes, type Usuario, type ImportacaoResult } from '../lib/api'

const PERFIS = ['ALUNO_EM', 'ALUNO_EJA', 'ALUNO_PROF', 'PROFESSOR', 'ADMIN_ESCOLA', 'ADMIN_SEED']

function NovoUsuarioModal({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({
    nome: '', matricula: '', cpf: '', email: '',
    senhaTemporaria: '', perfil: 'ALUNO_EM', instituicaoId: '',
  })
  const [erro, setErro] = useState('')

  const { data: instituicoes = [] } = useQuery({
    queryKey: ['instituicoes'],
    queryFn: listarInstituicoes,
  })

  const mutation = useMutation({
    mutationFn: criarUsuario,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['usuarios'] }); onClose() },
    onError: (e: unknown) => {
      const msg = (e as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem
      setErro(msg ?? 'Erro ao criar usuário.')
    },
  })

  const set = (field: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [field]: e.target.value }))

  const handleSubmit = (ev: React.FormEvent) => {
    ev.preventDefault()
    if (!form.nome.trim() || !form.matricula.trim() || !form.senhaTemporaria.trim()) {
      return setErro('Nome, matrícula e senha temporária são obrigatórios.')
    }

    // Valida CPF: remove máscara e verifica 11 dígitos
    let cpfFinal: string | undefined = undefined
    if (form.cpf.trim()) {
      const soDigitos = form.cpf.replace(/\D/g, '')
      if (soDigitos.length !== 11) {
        return setErro('CPF inválido — informe 11 dígitos (com ou sem máscara).')
      }
      // Envia formatado 000.000.000-00
      cpfFinal = `${soDigitos.slice(0,3)}.${soDigitos.slice(3,6)}.${soDigitos.slice(6,9)}-${soDigitos.slice(9)}`
    }

    setErro('')
    mutation.mutate({
      ...form,
      cpf: cpfFinal,
      email: form.email || undefined,
      instituicaoId: form.instituicaoId || undefined,
    })
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-5 border-b sticky top-0 bg-white">
          <h2 className="font-bold text-gray-800">Novo usuário</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
        </div>
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {erro && <p className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">{erro}</p>}
          <div className="grid grid-cols-2 gap-3">
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Nome completo *</label>
              <input className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.nome} onChange={set('nome')} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Matrícula *</label>
              <input className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.matricula} onChange={set('matricula')} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Perfil *</label>
              <select className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.perfil} onChange={set('perfil')}>
                {PERFIS.map((p) => <option key={p} value={p}>{p}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">CPF</label>
              <input className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.cpf} onChange={set('cpf')} placeholder="000.000.000-00" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">E-mail</label>
              <input type="email" className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.email} onChange={set('email')} />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Senha temporária *</label>
              <input type="password" className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.senhaTemporaria} onChange={set('senhaTemporaria')} />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">Instituição</label>
              <select className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={form.instituicaoId} onChange={set('instituicaoId')}>
                <option value="">Sem instituição (opcional)</option>
                {instituicoes.map((i) => (
                  <option key={i.id} value={i.id}>{i.nome} — {i.municipio}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm hover:bg-gray-200">Cancelar</button>
            <button type="submit" disabled={mutation.isPending}
              className="flex-1 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50">
              {mutation.isPending ? 'Criando...' : 'Criar usuário'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

const perfilBadge: Record<string, string> = {
  ALUNO_EM: 'bg-blue-50 text-blue-700',
  ALUNO_EJA: 'bg-purple-50 text-purple-700',
  ALUNO_PROF: 'bg-indigo-50 text-indigo-700',
  PROFESSOR: 'bg-amber-50 text-amber-700',
  ADMIN_ESCOLA: 'bg-orange-50 text-orange-700',
  ADMIN_SEED: 'bg-red-50 text-red-700',
}

function ImportacaoResultModal({ resultado, onClose }: { resultado: ImportacaoResult; onClose: () => void }) {
  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4" role="dialog" aria-modal="true" aria-labelledby="importacao-titulo">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between p-5 border-b sticky top-0 bg-white">
          <h2 id="importacao-titulo" className="font-bold text-gray-800">Resultado da Importação</h2>
          <button onClick={onClose} aria-label="Fechar resultado" className="text-gray-400 hover:text-gray-600 text-xl">✕</button>
        </div>
        <div className="p-5 space-y-4">
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-gray-50 rounded-lg p-3 text-center">
              <p className="text-2xl font-bold text-gray-800">{resultado.total}</p>
              <p className="text-xs text-gray-500">Total de linhas</p>
            </div>
            <div className="bg-green-50 rounded-lg p-3 text-center">
              <p className="text-2xl font-bold text-green-700">{resultado.importados}</p>
              <p className="text-xs text-green-600">Importados</p>
            </div>
            <div className="bg-red-50 rounded-lg p-3 text-center">
              <p className="text-2xl font-bold text-red-700">{resultado.erros}</p>
              <p className="text-xs text-red-600">Erros</p>
            </div>
          </div>
          {resultado.detalhes.length > 0 && (
            <div className="border rounded-lg overflow-hidden">
              <table className="w-full text-xs" aria-label="Detalhes da importação">
                <thead className="bg-gray-50 text-gray-500">
                  <tr>
                    <th scope="col" className="px-3 py-2 text-left font-medium">Linha</th>
                    <th scope="col" className="px-3 py-2 text-left font-medium">Matrícula</th>
                    <th scope="col" className="px-3 py-2 text-left font-medium">Status</th>
                    <th scope="col" className="px-3 py-2 text-left font-medium">Mensagem</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {resultado.detalhes.map((d, i) => (
                    <tr key={i} className={d.status === 'erro' ? 'bg-red-50' : ''}>
                      <td className="px-3 py-2 text-gray-500">{d.linha}</td>
                      <td className="px-3 py-2 font-mono text-gray-700">{d.matricula || '—'}</td>
                      <td className="px-3 py-2">
                        <span className={`px-1.5 py-0.5 rounded-full font-medium ${d.status === 'ok' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                          {d.status}
                        </span>
                      </td>
                      <td className="px-3 py-2 text-gray-500">{d.mensagem}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <button onClick={onClose} className="w-full py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700">
            Fechar
          </button>
        </div>
      </div>
    </div>
  )
}

export default function GestaoUsuariosPage() {
  const qc = useQueryClient()
  const [busca, setBusca] = useState('')
  const [novoUsuario, setNovoUsuario] = useState(false)
  const [importacaoResultado, setImportacaoResultado] = useState<ImportacaoResult | null>(null)
  const [importando, setImportando] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const { data: usuarios = [], isLoading } = useQuery({
    queryKey: ['usuarios'],
    queryFn: () => listarUsuarios(),
  })

  const desativar = useMutation({
    mutationFn: desativarUsuario,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['usuarios'] }),
  })
  const reativar = useMutation({
    mutationFn: reativarUsuario,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['usuarios'] }),
  })

  async function handleImportarCsv(e: React.ChangeEvent<HTMLInputElement>) {
    const arquivo = e.target.files?.[0]
    if (!arquivo) return
    setImportando(true)
    try {
      const resultado = await importarAlunos(arquivo)
      setImportacaoResultado(resultado)
      qc.invalidateQueries({ queryKey: ['usuarios'] })
    } catch {
      alert('Erro ao importar CSV. Verifique o formato do arquivo.')
    } finally {
      setImportando(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const filtrados = usuarios.filter((u: Usuario) =>
    busca === '' ||
    u.nome.toLowerCase().includes(busca.toLowerCase()) ||
    u.matricula.toLowerCase().includes(busca.toLowerCase()) ||
    u.perfil.toLowerCase().includes(busca.toLowerCase())
  )

  return (
    <div className="space-y-6">
      {novoUsuario && <NovoUsuarioModal onClose={() => setNovoUsuario(false)} />}
      {importacaoResultado && (
        <ImportacaoResultModal resultado={importacaoResultado} onClose={() => setImportacaoResultado(null)} />
      )}

      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-xl font-bold text-gray-800">Gestão de Usuários</h1>
          <p className="text-sm text-gray-500 mt-0.5">{usuarios.length} usuários cadastrados</p>
        </div>
        <div className="flex gap-2 flex-wrap">
          <input
            aria-label="Buscar usuários por nome ou matrícula"
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-56"
            placeholder="Buscar por nome ou matrícula..."
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
          {/* Input file oculto para CSV */}
          <input
            ref={fileInputRef}
            type="file"
            accept=".csv,text/csv"
            className="hidden"
            aria-hidden="true"
            onChange={handleImportarCsv}
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={importando}
            aria-label="Importar usuários via arquivo CSV"
            title="Formato: nome,matricula,cpf,email,perfil,senhaTemporaria,instituicaoId"
            className="px-4 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm font-medium hover:bg-gray-200 transition whitespace-nowrap disabled:opacity-50"
          >
            {importando ? 'Importando…' : '↑ Importar CSV'}
          </button>
          <button
            onClick={() => setNovoUsuario(true)}
            aria-label="Criar novo usuário"
            className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition whitespace-nowrap"
          >
            + Novo usuário
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[1,2,3,4,5].map((i) => <div key={i} className="h-14 bg-white rounded-xl border animate-pulse" />)}
        </div>
      ) : filtrados.length === 0 ? (
        <div className="bg-white rounded-xl p-10 text-center border border-gray-100">
          <p className="text-gray-400 text-sm">Nenhum usuário encontrado.</p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-100 overflow-hidden shadow-sm">
          <table className="w-full text-sm" aria-label="Lista de usuários">
            <thead className="bg-gray-50 text-gray-500 text-left">
              <tr>
                <th scope="col" className="px-5 py-3 font-medium">Nome</th>
                <th scope="col" className="px-5 py-3 font-medium">Matrícula</th>
                <th scope="col" className="px-5 py-3 font-medium">Perfil</th>
                <th scope="col" className="px-5 py-3 font-medium">Instituição</th>
                <th scope="col" className="px-5 py-3 font-medium">Status</th>
                <th scope="col" className="px-5 py-3 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {filtrados.map((u: Usuario) => (
                <tr key={u.id} className="hover:bg-gray-50 transition">
                  <td className="px-5 py-3">
                    <div className="font-medium text-gray-800">{u.nome}</div>
                    {u.email && <div className="text-xs text-gray-400">{u.email}</div>}
                  </td>
                  <td className="px-5 py-3 text-gray-500">{u.matricula}</td>
                  <td className="px-5 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${perfilBadge[u.perfil] ?? 'bg-gray-100 text-gray-600'}`}>
                      {u.perfil}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-500 text-xs truncate max-w-[160px]">
                    {u.nomeInstituicao ?? '—'}
                  </td>
                  <td className="px-5 py-3">
                    <div className="flex flex-col gap-0.5">
                      <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium w-fit ${
                        u.ativo ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
                      }`} aria-label={u.ativo ? 'Usuário ativo' : 'Usuário inativo'}>
                        {u.ativo ? 'Ativo' : 'Inativo'}
                      </span>
                      {u.primeiroAcesso && (
                        <span className="inline-block px-2 py-0.5 rounded-full text-xs bg-yellow-100 text-yellow-700 w-fit"
                              aria-label="Usuário aguardando primeiro acesso">
                          Aguardando 1º acesso
                        </span>
                      )}
                    </div>
                  </td>
                  <td className="px-5 py-3">
                    {u.ativo ? (
                      <button
                        onClick={() => desativar.mutate(u.id)}
                        disabled={desativar.isPending}
                        aria-label={`Desativar usuário ${u.nome}`}
                        className="text-red-500 hover:underline text-xs disabled:opacity-40 focus:outline-none focus:ring-2 focus:ring-red-300 rounded"
                      >
                        Desativar
                      </button>
                    ) : (
                      <button
                        onClick={() => reativar.mutate(u.id)}
                        disabled={reativar.isPending}
                        aria-label={`Reativar usuário ${u.nome}`}
                        className="text-green-600 hover:underline text-xs disabled:opacity-40 focus:outline-none focus:ring-2 focus:ring-green-300 rounded"
                      >
                        Reativar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

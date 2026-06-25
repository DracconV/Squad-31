import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../contexts/AuthContext'
import { Plus, X, Users, Eye } from 'lucide-react'
import {
  listarMinhasTurmas,
  listarTurmas,
  listarAlunosDaTurma,
  criarTurma,
  listarInstituicoes,
  type Turma,
  type AlunoTurma,
} from '../lib/api'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

function AlunosModal({ turma, onClose }: { turma: Turma; onClose: () => void }) {
  const { data: alunos = [], isLoading } = useQuery({
    queryKey: ['turma-alunos', turma.id],
    queryFn: () => listarAlunosDaTurma(turma.id),
  })

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-bold text-gray-800">Alunos — {turma.nome}</h2>
          <button onClick={onClose} aria-label="Fechar" className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
        </div>
        <div className="p-5 max-h-80 overflow-y-auto">
          {isLoading ? (
            <div className="space-y-2">
              {[1, 2, 3].map((i) => <div key={i} className="h-10 bg-gray-100 rounded animate-pulse" />)}
            </div>
          ) : alunos.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-4">Nenhum aluno nesta turma.</p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b">
                  <th className="pb-2">Nome</th>
                  <th className="pb-2">Matrícula</th>
                  <th className="pb-2">Perfil</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {alunos.map((a: AlunoTurma) => (
                  <tr key={a.alunoId} className="hover:bg-gray-50">
                    <td className="py-2 font-medium text-gray-800">{a.nome}</td>
                    <td className="py-2 text-gray-500">{a.matricula}</td>
                    <td className="py-2 text-gray-400 text-xs">{a.perfil}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        <div className="p-5 border-t flex justify-end">
          <button onClick={onClose} className="px-4 py-2 text-sm rounded-lg bg-gray-100 hover:bg-gray-200 text-gray-700">
            Fechar
          </button>
        </div>
      </div>
    </div>
  )
}

function NovaTurmaModal({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({ nome: '', ano: new Date().getFullYear(), modalidade: 'MEDIO', instituicaoId: '' })
  const [erro, setErro] = useState('')

  const { data: instituicoes = [] } = useQuery({
    queryKey: ['instituicoes'],
    queryFn: listarInstituicoes,
  })

  const mutation = useMutation({
    mutationFn: criarTurma,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['turmas'] })
      onClose()
    },
    onError: (e: unknown) => {
      const msg = (e as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem
      setErro(msg ?? 'Erro ao criar turma.')
    },
  })

  const handleSubmit = (ev: React.FormEvent) => {
    ev.preventDefault()
    if (!form.nome.trim()) return setErro('Nome obrigatório.')
    if (!form.instituicaoId) return setErro('Selecione a instituição.')
    setErro('')
    mutation.mutate(form)
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-bold text-gray-800">Nova turma</h2>
          <button onClick={onClose} aria-label="Fechar" className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {erro && <p className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">{erro}</p>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nome</label>
            <input
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={form.nome}
              onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))}
              placeholder="Ex: 3ª série A"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Ano letivo</label>
              <input
                type="number"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={form.ano}
                onChange={(e) => setForm((f) => ({ ...f, ano: Number(e.target.value) }))}
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Modalidade</label>
              <select
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={form.modalidade}
                onChange={(e) => setForm((f) => ({ ...f, modalidade: e.target.value }))}
              >
                <option value="MEDIO">Ensino Médio</option>
                <option value="EJA">EJA</option>
                <option value="PROFISSIONALIZANTE">Profissionalizante</option>
              </select>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Instituição</label>
            <select
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={form.instituicaoId}
              onChange={(e) => setForm((f) => ({ ...f, instituicaoId: e.target.value }))}
            >
              <option value="">Selecione a instituição…</option>
              {instituicoes.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome} — {i.municipio}
                </option>
              ))}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm hover:bg-gray-200">
              Cancelar
            </button>
            <button
              type="submit"
              disabled={mutation.isPending}
              className="flex-1 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 disabled:opacity-50"
            >
              {mutation.isPending ? 'Criando...' : 'Criar turma'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function TurmasPage() {
  const { user } = useAuth()
  const perfil = user?.perfil
  const isProfessor = perfil === 'PROFESSOR'
  const isAdmin = perfil === 'ADMIN_ESCOLA' || perfil === 'ADMIN_SEED'

  const { data: turmas = [], isLoading } = useQuery<Turma[]>({
    queryKey: ['turmas'],
    queryFn: isProfessor ? listarMinhasTurmas : () => listarTurmas(),
  })

  const [turmaAlunos, setTurmaAlunos] = useState<Turma | null>(null)
  const [novaTurma, setNovaTurma] = useState(false)

  const modalidadeLabel: Record<string, string> = {
    MEDIO: 'Ens. Médio',
    EJA: 'EJA',
    PROFISSIONALIZANTE: 'Profis.',
  }

  return (
    <div className="space-y-6">
      {turmaAlunos && <AlunosModal turma={turmaAlunos} onClose={() => setTurmaAlunos(null)} />}
      {novaTurma && <NovaTurmaModal onClose={() => setNovaTurma(false)} />}

      <Card className="p-5 flex items-center justify-between gap-3">
        <p className="text-sm text-gray-600">{isProfessor ? 'Suas turmas e alunos.' : 'Turmas da rede estadual.'}</p>
        {(isProfessor || isAdmin) && (
          <button
            onClick={() => setNovaTurma(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 transition shrink-0"
          >
            <Plus size={16} /> Nova turma
          </button>
        )}
      </Card>

      {isLoading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => <div key={i} className="h-16 bg-white rounded-2xl border animate-pulse" />)}
        </div>
      ) : turmas.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<Users size={30} strokeWidth={1.75} />}
            title="Nenhuma turma encontrada"
            description={isProfessor ? 'Você ainda não tem turmas atribuídas.' : 'Crie a primeira turma da rede.'}
            action={(isProfessor || isAdmin) ? { label: 'Nova turma', onClick: () => setNovaTurma(true) } : undefined}
          />
        </Card>
      ) : (
        <Card className="overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-left">
              <tr>
                <th className="px-5 py-3 font-medium">Turma</th>
                <th className="px-5 py-3 font-medium">Ano</th>
                <th className="px-5 py-3 font-medium">Modalidade</th>
                <th className="px-5 py-3 font-medium">Instituição</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {turmas.map((t: Turma) => (
                <tr key={t.id} className="hover:bg-gray-50 transition">
                  <td className="px-5 py-3 font-medium text-gray-800">{t.nome}</td>
                  <td className="px-5 py-3 text-gray-500">{t.ano}</td>
                  <td className="px-5 py-3">
                    <span className="px-2 py-0.5 rounded-full text-xs bg-brand-50 text-brand-700">
                      {modalidadeLabel[t.modalidade] ?? t.modalidade}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-500 truncate max-w-[180px]">{t.nomeInstituicao}</td>
                  <td className="px-5 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      t.ativo ? 'bg-brand-50 text-brand-700' : 'bg-gray-100 text-gray-500'
                    }`}>
                      {t.ativo ? 'Ativa' : 'Inativa'}
                    </span>
                  </td>
                  <td className="px-5 py-3">
                    <button
                      onClick={() => setTurmaAlunos(t)}
                      className="inline-flex items-center gap-1 text-brand-600 hover:underline text-sm"
                    >
                      <Eye size={14} /> Ver alunos
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}

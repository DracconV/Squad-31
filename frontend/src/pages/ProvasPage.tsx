import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Plus, Pencil, Ban, X } from 'lucide-react'
import {
  listarMeusSimulados,
  atualizarSimulado,
  desativarSimulado,
  type Simulado,
} from '../lib/api'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

function EditarModal({ simulado, onClose }: { simulado: Simulado; onClose: () => void }) {
  const qc = useQueryClient()
  const [titulo, setTitulo] = useState(simulado.titulo)
  const [tempoMinutos, setTempoMinutos] = useState(simulado.tempoMinutos)
  const [pontuado, setPontuado] = useState(simulado.pontuado)
  const [erro, setErro] = useState('')

  const mutation = useMutation({
    mutationFn: () => atualizarSimulado(simulado.id, { titulo: titulo.trim(), tempoMinutos, pontuado }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['meus-simulados'] })
      onClose()
    },
    onError: () => setErro('Erro ao salvar.'),
  })

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-bold text-gray-800">Editar simulado</h2>
          <button onClick={onClose} aria-label="Fechar" className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
        </div>
        <div className="p-5 space-y-4">
          {erro && <p className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">{erro}</p>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Título</label>
            <input className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={titulo} onChange={(e) => setTitulo(e.target.value)} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tempo (min)</label>
              <input type="number" min={1} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={tempoMinutos} onChange={(e) => setTempoMinutos(Number(e.target.value))} />
            </div>
            <label className="flex items-center gap-2 text-sm text-gray-700 mt-6">
              <input type="checkbox" checked={pontuado} onChange={(e) => setPontuado(e.target.checked)} />
              Pontuado
            </label>
          </div>
          <div className="flex gap-3 pt-2">
            <button onClick={onClose} className="flex-1 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm hover:bg-gray-200">Cancelar</button>
            <button onClick={() => mutation.mutate()} disabled={mutation.isPending}
              className="flex-1 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 disabled:opacity-50">
              {mutation.isPending ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default function ProvasPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [editar, setEditar] = useState<Simulado | null>(null)

  const { data: simulados = [], isLoading } = useQuery<Simulado[]>({
    queryKey: ['meus-simulados'],
    queryFn: listarMeusSimulados,
  })

  const desativar = useMutation({
    mutationFn: desativarSimulado,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['meus-simulados'] }),
  })

  function encerrado(s: Simulado) {
    return s.dataFim && new Date(s.dataFim) < new Date()
  }

  return (
    <div className="space-y-6">
      {editar && <EditarModal simulado={editar} onClose={() => setEditar(null)} />}

      <Card className="p-5 flex items-center justify-between gap-3">
        <p className="text-sm text-gray-600">Gerencie os simulados que você criou.</p>
        <button onClick={() => navigate('/criar-prova')}
          className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 transition shrink-0">
          <Plus size={16} /> Nova prova
        </button>
      </Card>

      {isLoading ? (
        <div className="space-y-2">
          {[1, 2, 3].map((i) => <div key={i} className="h-16 bg-white rounded-2xl border animate-pulse" />)}
        </div>
      ) : simulados.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<Plus size={30} strokeWidth={1.75} />}
            title="Nenhuma prova criada"
            description="Crie sua primeira prova para disponibilizar aos alunos."
            action={{ label: 'Criar prova', onClick: () => navigate('/criar-prova') }}
          />
        </Card>
      ) : (
        <Card className="overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-left">
              <tr>
                <th className="px-5 py-3 font-medium">Título</th>
                <th className="px-5 py-3 font-medium">Tempo</th>
                <th className="px-5 py-3 font-medium">Tipo</th>
                <th className="px-5 py-3 font-medium">Questões</th>
                <th className="px-5 py-3 font-medium">Status</th>
                <th className="px-5 py-3 font-medium">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {simulados.map((s) => (
                <tr key={s.id} className="hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-800">{s.titulo}</td>
                  <td className="px-5 py-3 text-gray-500">{s.tempoMinutos} min</td>
                  <td className="px-5 py-3 text-gray-500">{s.pontuado ? 'Pontuado' : 'Treino'}</td>
                  <td className="px-5 py-3 text-gray-500">{s.questaoIds?.length ?? '—'}</td>
                  <td className="px-5 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                      encerrado(s) ? 'bg-gray-100 text-gray-500' : 'bg-brand-50 text-brand-700'
                    }`}>
                      {encerrado(s) ? 'Encerrado' : 'Ativo'}
                    </span>
                  </td>
                  <td className="px-5 py-3 whitespace-nowrap">
                    <div className="flex items-center gap-3">
                      <button onClick={() => setEditar(s)} className="inline-flex items-center gap-1 text-brand-600 hover:underline text-xs">
                        <Pencil size={13} /> Editar
                      </button>
                      {!encerrado(s) && (
                        <button onClick={() => { if (confirm('Desativar este simulado?')) desativar.mutate(s.id) }}
                          disabled={desativar.isPending}
                          className="inline-flex items-center gap-1 text-red-500 hover:underline text-xs disabled:opacity-40">
                          <Ban size={13} /> Desativar
                        </button>
                      )}
                    </div>
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

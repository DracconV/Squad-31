import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Clock, BarChart3, ListChecks, Plus, FileText, Play, RotateCcw, Eye } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'
import { listarSimulados, listarMeusSimulados, listarMinhasTentativas, type Simulado } from '../lib/api'

function badgeStatus(s: Simulado) {
  const agora = new Date()
  if (s.dataFim && new Date(s.dataFim) < agora)
    return <span className="text-xs px-2.5 py-1 rounded-full bg-gray-100 text-gray-500 font-medium">Encerrado</span>
  if (s.dataInicio && new Date(s.dataInicio) > agora)
    return <span className="text-xs px-2.5 py-1 rounded-full bg-gold-400/20 text-gold-600 font-medium">Agendado</span>
  return <span className="text-xs px-2.5 py-1 rounded-full bg-brand-50 text-brand-700 font-medium">Disponível</span>
}

function SimuladoCard({ simulado, tentativas }: { simulado: Simulado; tentativas: string[] }) {
  const navigate = useNavigate()
  const jaFez = tentativas.includes(simulado.id)
  const encerrado = simulado.dataFim && new Date(simulado.dataFim) < new Date()

  return (
    <Card hover className="p-5 flex flex-col gap-4">
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-start gap-3 min-w-0">
          <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
            <FileText size={20} />
          </span>
          <h3 className="font-semibold text-gray-800 leading-snug pt-1.5 truncate">{simulado.titulo}</h3>
        </div>
        {badgeStatus(simulado)}
      </div>

      <div className="flex flex-wrap gap-x-4 gap-y-1.5 text-sm text-gray-500">
        <span className="inline-flex items-center gap-1.5"><Clock size={15} /> {simulado.tempoMinutos} min</span>
        <span className="inline-flex items-center gap-1.5">
          <BarChart3 size={15} /> {simulado.pontuado ? 'Pontuado' : 'Treino'}
        </span>
        {simulado.questaoIds && (
          <span className="inline-flex items-center gap-1.5"><ListChecks size={15} /> {simulado.questaoIds.length} questões</span>
        )}
      </div>

      {simulado.dataInicio && (
        <p className="text-xs text-gray-400 -mt-1">
          Início: {new Date(simulado.dataInicio).toLocaleString('pt-BR')}
          {simulado.dataFim && ` · Fim: ${new Date(simulado.dataFim).toLocaleString('pt-BR')}`}
        </p>
      )}

      <div className="flex gap-2 mt-auto pt-1">
        {!encerrado && (
          <button
            onClick={() => navigate(`/simulados/${simulado.id}`)}
            className="flex-1 inline-flex items-center justify-center gap-1.5 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 transition"
          >
            {jaFez ? <><RotateCcw size={15} /> Refazer</> : <><Play size={15} /> Iniciar</>}
          </button>
        )}
        {jaFez && (
          <button
            onClick={() => navigate(`/historico`)}
            className="flex-1 inline-flex items-center justify-center gap-1.5 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm font-medium hover:bg-gray-200 transition"
          >
            <Eye size={15} /> Resultado
          </button>
        )}
      </div>
    </Card>
  )
}

export default function SimuladosPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const perfil = user?.perfil
  const isProfessor = perfil === 'PROFESSOR' || perfil === 'ADMIN_ESCOLA' || perfil === 'ADMIN_SEED'
  const [aba, setAba] = useState<'disponiveis' | 'meus'>(isProfessor ? 'meus' : 'disponiveis')

  const { data: simulados = [], isLoading } = useQuery<Simulado[]>({
    queryKey: ['simulados', aba],
    queryFn: aba === 'meus' ? listarMeusSimulados : () => listarSimulados(),
  })

  const { data: tentativas = [] } = useQuery({
    queryKey: ['tentativas'],
    queryFn: listarMinhasTentativas,
    enabled: !isProfessor,
  })

  const tentativaIds = tentativas.map((t) => t.simuladoId)

  return (
    <div className="space-y-6">
      <Card className="p-5 flex items-center justify-between gap-3">
        <p className="text-sm text-gray-600">
          {isProfessor ? 'Gerencie e acompanhe seus simulados.' : 'Pratique com simulados e acompanhe seu desempenho.'}
        </p>
        {isProfessor && (
          <button
            onClick={() => navigate('/criar-prova')}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 transition shrink-0"
          >
            <Plus size={16} /> Novo simulado
          </button>
        )}
      </Card>

      {isProfessor && (
        <div className="flex gap-2">
          {(['meus', 'disponiveis'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setAba(tab)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                aba === tab
                  ? 'bg-brand-600 text-white shadow-sm'
                  : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              {tab === 'meus' ? 'Meus simulados' : 'Todos disponíveis'}
            </button>
          ))}
        </div>
      )}

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-44 bg-white rounded-2xl border border-gray-100 animate-pulse" />
          ))}
        </div>
      ) : simulados.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<FileText size={30} strokeWidth={1.75} />}
            title={isProfessor ? 'Nenhum simulado criado' : 'Nenhum simulado disponível'}
            description={
              isProfessor
                ? 'Crie seu primeiro simulado para disponibilizar aos alunos.'
                : 'Quando houver simulados liberados, eles aparecerão aqui.'
            }
            action={isProfessor ? { label: 'Criar simulado', onClick: () => navigate('/criar-prova') } : undefined}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {simulados.map((s) => (
            <SimuladoCard key={s.id} simulado={s} tentativas={tentativaIds} />
          ))}
        </div>
      )}
    </div>
  )
}

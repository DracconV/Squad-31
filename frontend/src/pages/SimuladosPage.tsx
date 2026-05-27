import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { listarSimulados, listarMeusSimulados, listarMinhasTentativas, type Simulado } from '../lib/api'

function badgeStatus(s: Simulado) {
  const agora = new Date()
  if (s.dataFim && new Date(s.dataFim) < agora)
    return <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">Encerrado</span>
  if (s.dataInicio && new Date(s.dataInicio) > agora)
    return <span className="text-xs px-2 py-0.5 rounded-full bg-yellow-100 text-yellow-700">Agendado</span>
  return <span className="text-xs px-2 py-0.5 rounded-full bg-green-100 text-green-700">Disponível</span>
}

function SimuladoCard({ simulado, tentativas }: { simulado: Simulado; tentativas: string[] }) {
  const navigate = useNavigate()
  const jaFez = tentativas.includes(simulado.id)
  const encerrado = simulado.dataFim && new Date(simulado.dataFim) < new Date()

  return (
    <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-gray-800 leading-snug">{simulado.titulo}</h3>
        {badgeStatus(simulado)}
      </div>

      <div className="flex gap-4 text-sm text-gray-500">
        <span>⏱ {simulado.tempoMinutos} min</span>
        <span>{simulado.pontuado ? '📊 Pontuado' : '📝 Treino'}</span>
        {simulado.questaoIds && <span>📋 {simulado.questaoIds.length} questões</span>}
      </div>

      {simulado.dataInicio && (
        <p className="text-xs text-gray-400">
          Início: {new Date(simulado.dataInicio).toLocaleString('pt-BR')}
          {simulado.dataFim && ` · Fim: ${new Date(simulado.dataFim).toLocaleString('pt-BR')}`}
        </p>
      )}

      <div className="flex gap-2 mt-1">
        {!encerrado && (
          <button
            onClick={() => navigate(`/simulados/${simulado.id}`)}
            className="flex-1 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition"
          >
            {jaFez ? 'Refazer' : 'Iniciar'}
          </button>
        )}
        {jaFez && (
          <button
            onClick={() => navigate(`/historico`)}
            className="flex-1 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm font-medium hover:bg-gray-200 transition"
          >
            Ver resultado
          </button>
        )}
      </div>
    </div>
  )
}

export default function SimuladosPage() {
  const { user } = useAuth()
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
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-800">Simulados</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {isProfessor ? 'Gerencie seus simulados' : 'Pratique com simulados disponíveis'}
          </p>
        </div>
        {isProfessor && (
          <button
            onClick={() => window.location.assign('/criar-prova')}
            className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition"
          >
            + Novo simulado
          </button>
        )}
      </div>

      {isProfessor && (
        <div className="flex gap-2">
          {(['meus', 'disponiveis'] as const).map((tab) => (
            <button
              key={tab}
              onClick={() => setAba(tab)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition ${
                aba === tab
                  ? 'bg-blue-600 text-white'
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
            <div key={i} className="h-40 bg-white rounded-xl border border-gray-100 animate-pulse" />
          ))}
        </div>
      ) : simulados.length === 0 ? (
        <div className="bg-white rounded-xl p-10 text-center border border-gray-100">
          <p className="text-gray-400 text-sm">
            {isProfessor ? 'Nenhum simulado criado ainda.' : 'Nenhum simulado disponível no momento.'}
          </p>
        </div>
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

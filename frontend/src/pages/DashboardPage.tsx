import type { ReactElement } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../contexts/AuthContext'
import { StatCard } from '../components/StatCard'
import { ProgressBar } from '../components/ProgressBar'
import { StatusBanner } from '../components/StatusBanner'
import {
  getStats,
  listarMinhasInscricoes,
  type StatsAluno,
  type StatsAdmin,
  type StatsProfessor,
  type Inscricao,
} from '../lib/api'
import type { Perfil } from '../lib/auth'

/* ── Skeleton de loading ────────────────────────────────── */

function StatsSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="bg-white rounded-xl p-5 border border-gray-100 animate-pulse h-24" />
      ))}
    </div>
  )
}

/* ── Dashboard do Aluno ─────────────────────────────────── */

function DashboardAluno() {
  const { data: stats, isLoading: loadingStats } = useQuery<StatsAluno>({
    queryKey: ['stats'],
    queryFn: getStats as () => Promise<StatsAluno>,
  })

  const { data: inscricoes = [], isLoading: loadingInscricoes } = useQuery<Inscricao[]>({
    queryKey: ['inscricoes-minhas'],
    queryFn: listarMinhasInscricoes,
  })

  const proximas = inscricoes.filter((i) => !i.concluido).slice(0, 3)

  return (
    <>
      {loadingStats ? (
        <StatsSkeleton />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard title="Cursos em andamento"  value={stats?.cursosAtivos ?? 0} />
          <StatCard title="Cursos concluídos"    value={stats?.cursosConcluidos ?? 0} trend="up" trendValue="parabéns!" />
          <StatCard title="Certificados emitidos" value={stats?.certificados ?? 0} />
          <StatCard title="Cursos disponíveis"   value={stats?.totalCursos ?? 0} />
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Cursos em andamento</h2>
          {loadingInscricoes ? (
            <div className="space-y-3">
              {[1, 2].map((i) => <div key={i} className="h-8 bg-gray-100 rounded animate-pulse" />)}
            </div>
          ) : proximas.length === 0 ? (
            <p className="text-sm text-gray-400">Nenhum curso em andamento. Inscreva-se em um curso!</p>
          ) : (
            <div className="space-y-4">
              {proximas.map((inscricao) => (
                <div key={inscricao.id}>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-700 font-medium truncate">{inscricao.nomeCurso}</span>
                    <span className="text-xs text-gray-400 shrink-0 ml-2">
                      {new Date(inscricao.dataInscricao).toLocaleDateString('pt-BR')}
                    </span>
                  </div>
                  <ProgressBar value={inscricao.concluido ? 100 : 30} color={inscricao.concluido ? 'green' : 'blue'} showPercent={false} />
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Progresso geral</h2>
          {!loadingStats && stats && (() => {
            const ativos     = (stats as StatsAluno).cursosAtivos     ?? 0
            const concluidos = (stats as StatsAluno).cursosConcluidos ?? 0
            const total      = ativos + concluidos
            const pct        = total > 0 ? Math.round((concluidos / total) * 100) : 0
            return (
              <>
                <ProgressBar label="Taxa de conclusão" value={pct} color="green" />
                <p className="text-xs text-gray-400 mt-3">
                  {concluidos} de {total} cursos concluídos
                </p>
              </>
            )
          })()}
        </div>
      </div>
    </>
  )
}

/* ── Dashboard do Professor ─────────────────────────────── */

function DashboardProfessor() {
  const { data: stats, isLoading } = useQuery<StatsProfessor>({
    queryKey: ['stats'],
    queryFn: getStats as () => Promise<StatsProfessor>,
  })

  return (
    <>
      {isLoading ? (
        <StatsSkeleton />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard title="Cursos ativos"         value={stats?.totalCursos ?? 0} />
          <StatCard title="Alunos acompanhados"   value={stats?.alunosAtivos ?? 0} />
          <StatCard title="Total de inscrições"   value={stats?.totalInscricoes ?? 0} />
          <StatCard title="Cursos concluídos"     value={stats?.totalConcluidos ?? 0} trend="up" />
        </div>
      )}

      <div className="mt-6 bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <h2 className="font-semibold text-gray-700 mb-4">Taxa de conclusão geral</h2>
        {!isLoading && stats && (
          <>
            <ProgressBar
              label="Alunos que concluíram"
              value={
                stats.totalInscricoes > 0
                  ? Math.round((stats.totalConcluidos / stats.totalInscricoes) * 100)
                  : 0
              }
              color="green"
            />
            <p className="text-xs text-gray-400 mt-2">
              {stats.totalConcluidos} de {stats.totalInscricoes} inscrições concluídas
            </p>
          </>
        )}
      </div>
    </>
  )
}

/* ── Dashboard Admin ────────────────────────────────────── */

function DashboardAdmin() {
  const { data: stats, isLoading } = useQuery<StatsAdmin>({
    queryKey: ['stats'],
    queryFn: getStats as () => Promise<StatsAdmin>,
  })

  return (
    <>
      {isLoading ? (
        <StatsSkeleton />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard title="Total de alunos"       value={stats?.totalAlunos ?? 0} />
          <StatCard title="Cursos ativos"         value={stats?.totalCursos ?? 0} />
          <StatCard title="Total de inscrições"   value={stats?.totalInscricoes ?? 0} />
          <StatCard title="Conclusões"            value={stats?.totalConcluidos ?? 0} trend="up" />
        </div>
      )}

      {!isLoading && stats && (
        <div className="mt-6 bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Taxa de conclusão da plataforma</h2>
          <ProgressBar
            label="Conclusões"
            value={
              stats.totalInscricoes > 0
                ? Math.round((stats.totalConcluidos / stats.totalInscricoes) * 100)
                : 0
            }
            color="green"
          />
          <StatusBanner variant="info" >
            {stats.totalAlunos} alunos ativos em {stats.totalCursos} cursos disponíveis.
          </StatusBanner>
        </div>
      )}
    </>
  )
}

/* ── Dispatcher ─────────────────────────────────────────── */

const DASHBOARD_MAP: Partial<Record<Perfil, () => ReactElement>> = {
  ALUNO_EM:    DashboardAluno,
  ALUNO_EJA:   DashboardAluno,
  ALUNO_PROF:  DashboardAluno,
  PROFESSOR:   DashboardProfessor,
  ADMIN_ESCOLA: DashboardAdmin,
  ADMIN_SEED:  DashboardAdmin,
}

export default function DashboardPage() {
  const { user } = useAuth()
  const Component = user ? DASHBOARD_MAP[user.perfil as Perfil] : null
  if (!Component) return null
  return <Component />
}

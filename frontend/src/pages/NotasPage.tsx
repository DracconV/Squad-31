import { useMemo, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { useAuth } from '../contexts/AuthContext'
import {
  getAlunosBaixoDesempenho,
  getDesempenhoTurma,
  getHistoricoDesempenhoAluno,
  listarMinhasTurmas,
  listarTurmas,
  type DesempenhoAluno,
  type DesempenhoTurma,
  type Turma,
} from '../lib/api'

type DisciplinaNota = {
  disciplina: string
  nota: number
  acertos: number
  total: number
  taxa: number
  faltas: number
  aulas: number
  atualizadoEm: string
}

const DEMO_NOTAS: DisciplinaNota[] = [
  { disciplina: 'Matemática', nota: 8.4, acertos: 42, total: 50, taxa: 84, faltas: 2, aulas: 32, atualizadoEm: '2026-05-30T12:00:00' },
  { disciplina: 'Linguagens', nota: 7.6, acertos: 38, total: 50, taxa: 76, faltas: 1, aulas: 30, atualizadoEm: '2026-05-30T12:00:00' },
  { disciplina: 'Ciências Humanas', nota: 6.8, acertos: 34, total: 50, taxa: 68, faltas: 4, aulas: 28, atualizadoEm: '2026-05-29T10:20:00' },
  { disciplina: 'Ciências da Natureza', nota: 5.9, acertos: 29, total: 50, taxa: 58, faltas: 3, aulas: 30, atualizadoEm: '2026-05-28T16:45:00' },
  { disciplina: 'Redação', nota: 8.9, acertos: 45, total: 50, taxa: 90, faltas: 0, aulas: 12, atualizadoEm: '2026-05-27T09:15:00' },
]

const DEMO_TURMAS: Turma[] = [
  {
    id: 'demo-3a-em',
    nome: '3ª série A',
    ano: 2026,
    modalidade: 'Ensino Médio',
    instituicaoId: 'seed-demo',
    nomeInstituicao: 'Colégio Estadual Governador Valadares',
    ativo: true,
    criadoEm: '2026-01-20T08:00:00',
  },
  {
    id: 'demo-2b-em',
    nome: '2ª série B',
    ano: 2026,
    modalidade: 'Ensino Médio',
    instituicaoId: 'seed-demo',
    nomeInstituicao: 'Colégio Estadual Governador Valadares',
    ativo: true,
    criadoEm: '2026-01-20T08:00:00',
  },
  {
    id: 'demo-eja-noite',
    nome: 'EJA Noturno',
    ano: 2026,
    modalidade: 'Educação de Jovens e Adultos',
    instituicaoId: 'seed-demo',
    nomeInstituicao: 'Centro Estadual de Educação',
    ativo: true,
    criadoEm: '2026-01-20T08:00:00',
  },
]

const DEMO_TURMA_DATA: Record<string, DesempenhoTurma> = {
  'demo-3a-em': {
    id: 'demo-3a-em',
    turmaId: 'demo-3a-em',
    media_turma: 7.2,
    mediana_turma: 7.4,
    maior_nota: 9.6,
    menor_nota: 4.8,
    taxa_conclusao: 78,
    alunos_ativos: 34,
    total_alunos: 38,
    atualizado_em: '2026-05-30T12:00:00',
  },
  'demo-2b-em': {
    id: 'demo-2b-em',
    turmaId: 'demo-2b-em',
    media_turma: 6.6,
    mediana_turma: 6.8,
    maior_nota: 8.9,
    menor_nota: 4.2,
    taxa_conclusao: 71,
    alunos_ativos: 29,
    total_alunos: 33,
    atualizado_em: '2026-05-30T12:00:00',
  },
  'demo-eja-noite': {
    id: 'demo-eja-noite',
    turmaId: 'demo-eja-noite',
    media_turma: 7.8,
    mediana_turma: 8.0,
    maior_nota: 9.7,
    menor_nota: 5.6,
    taxa_conclusao: 84,
    alunos_ativos: 22,
    total_alunos: 25,
    atualizado_em: '2026-05-30T12:00:00',
  },
}

const DEMO_BAIXO_DESEMPENHO: Record<string, DesempenhoAluno[]> = {
  'demo-3a-em': [
    { id: '1', alunoId: 'AL-018', turmaId: 'demo-3a-em', disciplina: 'Ciências da Natureza', nota_media: 5.4, questoes_acertadas: 27, questoes_total: 50, taxa_acerto: 54, atualizado_em: '2026-05-30T12:00:00' },
    { id: '2', alunoId: 'AL-027', turmaId: 'demo-3a-em', disciplina: 'Matemática', nota_media: 5.8, questoes_acertadas: 29, questoes_total: 50, taxa_acerto: 58, atualizado_em: '2026-05-29T15:30:00' },
    { id: '3', alunoId: 'AL-031', turmaId: 'demo-3a-em', disciplina: 'Linguagens', nota_media: 5.1, questoes_acertadas: 25, questoes_total: 50, taxa_acerto: 51, atualizado_em: '2026-05-28T11:00:00' },
  ],
  'demo-2b-em': [
    { id: '4', alunoId: 'AL-044', turmaId: 'demo-2b-em', disciplina: 'Matemática', nota_media: 4.9, questoes_acertadas: 22, questoes_total: 50, taxa_acerto: 44, atualizado_em: '2026-05-30T12:00:00' },
    { id: '5', alunoId: 'AL-052', turmaId: 'demo-2b-em', disciplina: 'Redação', nota_media: 5.7, questoes_acertadas: 28, questoes_total: 50, taxa_acerto: 56, atualizado_em: '2026-05-29T13:40:00' },
    { id: '6', alunoId: 'AL-063', turmaId: 'demo-2b-em', disciplina: 'Ciências Humanas', nota_media: 5.3, questoes_acertadas: 26, questoes_total: 50, taxa_acerto: 52, atualizado_em: '2026-05-28T10:10:00' },
    { id: '7', alunoId: 'AL-071', turmaId: 'demo-2b-em', disciplina: 'Linguagens', nota_media: 5.9, questoes_acertadas: 30, questoes_total: 50, taxa_acerto: 60, atualizado_em: '2026-05-27T09:00:00' },
  ],
  'demo-eja-noite': [
    { id: '8', alunoId: 'EJA-012', turmaId: 'demo-eja-noite', disciplina: 'Matemática', nota_media: 5.6, questoes_acertadas: 28, questoes_total: 50, taxa_acerto: 56, atualizado_em: '2026-05-30T12:00:00' },
    { id: '9', alunoId: 'EJA-019', turmaId: 'demo-eja-noite', disciplina: 'Ciências da Natureza', nota_media: 5.8, questoes_acertadas: 29, questoes_total: 50, taxa_acerto: 58, atualizado_em: '2026-05-29T20:30:00' },
  ],
}

function toNumber(value: unknown, fallback = 0): number {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

function formatNumber(value: number, digits = 1): string {
  return value.toLocaleString('pt-BR', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  })
}

function formatDate(value?: string): string {
  if (!value) return 'Atualização pendente'
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}

function normalizeHistorico(data?: DesempenhoAluno[]): DisciplinaNota[] {
  if (!data?.length) return DEMO_NOTAS
  return data.map((item, index) => {
    const total = toNumber(item.questoes_total, 1)
    const faltasDemo = [1, 2, 0, 3, 4][index % 5]
    const aulasDemo = [30, 32, 28, 30, 12][index % 5]

    return {
      disciplina: item.disciplina,
      nota: toNumber(item.nota_media),
      acertos: toNumber(item.questoes_acertadas),
      total,
      taxa: toNumber(item.taxa_acerto),
      faltas: faltasDemo,
      aulas: aulasDemo,
      atualizadoEm: item.atualizado_em,
    }
  })
}

function statusFromNota(nota: number): { label: string; className: string } {
  if (nota >= 7) return { label: 'Adequado', className: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200' }
  if (nota >= 6) return { label: 'Atenção', className: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200' }
  return { label: 'Intervenção', className: 'bg-rose-50 text-rose-700 ring-1 ring-rose-200' }
}

function MetricCard({
  label,
  value,
  detail,
  tone,
}: {
  label: string
  value: string
  detail: string
  tone: 'blue' | 'green' | 'yellow' | 'red'
}) {
  const tones = {
    blue: 'from-brand-500 to-brand-700',
    green: 'from-emerald-500 to-teal-400',
    yellow: 'from-amber-400 to-yellow-300',
    red: 'from-rose-500 to-pink-500',
  }

  return (
    <article className="min-w-0 overflow-hidden rounded-lg border border-slate-100 bg-white shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
      <div className={`h-1.5 bg-gradient-to-r ${tones[tone]}`} />
      <div className="p-5">
        <p className="text-sm font-medium text-slate-500">{label}</p>
        <p className="mt-3 text-3xl font-bold tracking-normal text-slate-950">{value}</p>
        <p className="mt-2 break-words text-xs font-semibold uppercase text-slate-400">{detail}</p>
      </div>
    </article>
  )
}

function HeroPanel({ mode }: { mode: 'aluno' | 'gestao' }) {
  return (
    <section className="overflow-hidden rounded-lg border border-brand-100 bg-white shadow-[0_24px_70px_rgba(28,63,110,0.12)]">
      <div className="grid min-h-[270px] grid-cols-1 lg:grid-cols-[minmax(0,1.05fr)_minmax(320px,0.95fr)]">
        <div className="min-w-0 p-7 sm:p-9">
          <span className="inline-flex rounded-full bg-brand-600 px-4 py-1.5 text-xs font-bold uppercase text-white shadow-lg shadow-brand-500/20">
            {mode === 'aluno' ? 'Portal do aluno' : 'Gestão pedagógica'}
          </span>
          <h2 className="mt-6 max-w-3xl break-words text-3xl font-bold leading-tight tracking-normal text-slate-950 sm:text-4xl">
            {mode === 'aluno'
              ? 'Boletim vivo para acompanhar notas, progresso e frequência'
              : 'Notas da turma transformadas em prioridade de intervenção'}
          </h2>
          <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
            {mode === 'aluno'
              ? 'Acompanhe seu desempenho por disciplina, veja onde avançou e identifique os conteúdos que precisam de revisão antes dos próximos simulados.'
              : 'Monitore média, conclusão e alunos em atenção para orientar intervenções com mais clareza.'}
          </p>
        </div>
        <div className="relative min-h-[230px] overflow-hidden bg-[#2f63df]">
          <div className="absolute inset-0 bg-[linear-gradient(135deg,#e5ff25_0_42%,#2f63df_42%_100%)]" />
          <div className="absolute right-8 top-10 h-24 w-24 rotate-12 rounded-[28px] bg-[#ff3345] shadow-[0_18px_35px_rgba(239,68,68,0.35)] ring-4 ring-white/70" />
          <div className="absolute bottom-8 left-10 rounded-lg bg-slate-950 p-5 text-white shadow-[0_22px_55px_rgba(15,23,42,0.42)] ring-8 ring-white/75">
            <p className="text-xs font-bold uppercase text-brand-200">SEED Educa</p>
            <p className="mt-4 text-4xl font-bold">7,4</p>
            <p className="text-sm text-slate-200">média consolidada</p>
            <div className="mt-5 flex gap-2">
              {[72, 84, 66].map((height) => (
                <span key={height} className="flex h-16 w-8 items-end rounded-full bg-white/10 p-1">
                  <span
                    className="w-full rounded-full bg-gradient-to-t from-brand-500 via-cyan-200 to-yellow-300"
                    style={{ height: `${height}%` }}
                  />
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function AlunoNotas() {
  const { user } = useAuth()
  const historicoQuery = useQuery({
    queryKey: ['desempenho-aluno', user?.id],
    queryFn: () => getHistoricoDesempenhoAluno(user?.id ?? ''),
    enabled: Boolean(user?.id),
    retry: 1,
  })

  const notas = useMemo(() => normalizeHistorico(historicoQuery.data), [historicoQuery.data])
  const media = notas.reduce((sum, item) => sum + item.nota, 0) / notas.length
  const presencaMedia =
    notas.reduce((sum, item) => sum + ((item.aulas - item.faltas) / item.aulas) * 100, 0) / notas.length
  const prioridades = [...notas].sort((a, b) => a.nota - b.nota).slice(0, 3)

  return (
    <div className="space-y-7">
      <HeroPanel mode="aluno" />

      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Média geral" value={formatNumber(media)} detail="escala de 0 a 10" tone="blue" />
        <MetricCard label="Presença média" value={`${formatNumber(presencaMedia, 0)}%`} detail="faltas consolidadas" tone="green" />
        <MetricCard label="Disciplinas" value={String(notas.length)} detail="com acompanhamento" tone="yellow" />
        <MetricCard label="Prioridades" value={String(prioridades.length)} detail="revisar primeiro" tone="red" />
      </section>

      <section className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1.35fr)_minmax(320px,0.65fr)]">
        <article className="min-w-0 rounded-lg border border-slate-100 bg-white p-5 shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h3 className="text-xl font-bold text-slate-950">Notas por disciplina</h3>
              <p className="mt-1 text-sm text-slate-500">Visão consolidada a partir do desempenho por disciplina.</p>
            </div>
            <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-600">
              Atualizado em {formatDate(notas[0]?.atualizadoEm)}
            </span>
          </div>
          <div className="mt-5 h-72 min-w-0">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={notas} margin={{ top: 12, right: 8, left: -18, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="disciplina" tick={{ fontSize: 11 }} interval={0} height={54} />
                <YAxis domain={[0, 10]} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => formatNumber(Number(value))} />
                <Bar dataKey="nota" radius={[7, 7, 0, 0]} fill="#2563EB" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="min-w-0 rounded-lg border border-slate-100 bg-white p-5 shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
          <h3 className="text-xl font-bold text-slate-950">Plano de revisão</h3>
          <p className="mt-1 text-sm text-slate-500">Comece pelas menores notas para recuperar rapidamente a média.</p>
          <div className="mt-5 space-y-3">
            {prioridades.map((item) => {
              const status = statusFromNota(item.nota)
              return (
                <div key={item.disciplina} className="rounded-lg border border-slate-100 bg-slate-50 p-4">
                  <div className="flex min-w-0 items-center justify-between gap-3">
                    <p className="break-words font-semibold text-slate-900">{item.disciplina}</p>
                    <span className={`shrink-0 rounded-full px-2.5 py-1 text-xs font-bold ${status.className}`}>
                      {status.label}
                    </span>
                  </div>
                  <div className="mt-3 h-2 overflow-hidden rounded-full bg-white">
                    <div className="h-full rounded-full bg-gradient-to-r from-rose-500 via-amber-400 to-emerald-500" style={{ width: `${item.taxa}%` }} />
                  </div>
                  <p className="mt-2 text-sm text-slate-500">
                    {item.acertos} acertos em {item.total} questões.
                  </p>
                </div>
              )
            })}
          </div>
        </article>
      </section>

      <section className="overflow-hidden rounded-lg border border-slate-100 bg-white shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
        <div className="border-b border-slate-100 p-5">
          <h3 className="text-xl font-bold text-slate-950">Boletim detalhado</h3>
          <p className="mt-1 text-sm text-slate-500">Notas, frequência estimada e status pedagógico por componente curricular.</p>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-[760px] w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-5 py-3">Disciplina</th>
                <th className="px-5 py-3">Nota</th>
                <th className="px-5 py-3">Acertos</th>
                <th className="px-5 py-3">Frequência</th>
                <th className="px-5 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {notas.map((item) => {
                const status = statusFromNota(item.nota)
                const presenca = ((item.aulas - item.faltas) / item.aulas) * 100
                return (
                  <tr key={item.disciplina} className="hover:bg-slate-50">
                    <td className="px-5 py-4 font-semibold text-slate-900">{item.disciplina}</td>
                    <td className="px-5 py-4 text-slate-700">{formatNumber(item.nota)}</td>
                    <td className="px-5 py-4 text-slate-700">{item.acertos}/{item.total}</td>
                    <td className="px-5 py-4 text-slate-700">{formatNumber(presenca, 0)}% · {item.faltas} falta(s)</td>
                    <td className="px-5 py-4">
                      <span className={`rounded-full px-3 py-1 text-xs font-bold ${status.className}`}>{status.label}</span>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}

function GestaoNotas() {
  const { user } = useAuth()
  const [selectedTurmaId, setSelectedTurmaId] = useState('')
  const isProfessor = user?.perfil === 'PROFESSOR'

  const turmasQuery = useQuery({
    queryKey: ['turmas-notas', user?.perfil],
    queryFn: () => (isProfessor ? listarMinhasTurmas() : listarTurmas()),
    retry: 1,
  })

  const turmas: Turma[] = turmasQuery.data?.length ? turmasQuery.data : DEMO_TURMAS
  const turmaId = selectedTurmaId || turmas[0]?.id || ''
  const isDemoTurma = turmaId.startsWith('demo-')

  const desempenhoQuery = useQuery({
    queryKey: ['desempenho-turma', turmaId],
    queryFn: () => getDesempenhoTurma(turmaId),
    enabled: Boolean(turmaId) && !isDemoTurma,
    retry: 1,
  })

  const baixoDesempenhoQuery = useQuery({
    queryKey: ['baixo-desempenho', turmaId],
    queryFn: () => getAlunosBaixoDesempenho(turmaId),
    enabled: Boolean(turmaId) && !isDemoTurma,
    retry: 1,
  })

  const turma = desempenhoQuery.data ?? DEMO_TURMA_DATA[turmaId] ?? DEMO_TURMA_DATA['demo-3a-em']
  const alunosBaixo = baixoDesempenhoQuery.data?.length
    ? baixoDesempenhoQuery.data
    : DEMO_BAIXO_DESEMPENHO[turmaId] ?? DEMO_BAIXO_DESEMPENHO['demo-3a-em']
  const chartData = [
    { nome: 'Menor', nota: toNumber(turma.menor_nota) },
    { nome: 'Mediana', nota: toNumber(turma.mediana_turma) },
    { nome: 'Média', nota: toNumber(turma.media_turma) },
    { nome: 'Maior', nota: toNumber(turma.maior_nota) },
  ]

  return (
    <div className="space-y-7">
      <HeroPanel mode="gestao" />

      <div className="flex flex-col gap-3 rounded-lg border border-slate-100 bg-white p-4 shadow-sm md:flex-row md:items-center md:justify-between">
        <div className="min-w-0">
          <p className="text-sm font-semibold text-slate-900">Turma em acompanhamento</p>
          <p className="text-sm text-slate-500">Selecione a turma para acompanhar indicadores e prioridades pedagógicas.</p>
        </div>
        <select
          value={turmaId}
          onChange={(event) => setSelectedTurmaId(event.target.value)}
          className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 shadow-sm outline-none focus:border-brand-500 md:w-80"
        >
          {turmas.map((turmaItem) => (
            <option key={turmaItem.id} value={turmaItem.id}>
              {turmaItem.nome} · {turmaItem.modalidade}
            </option>
          ))}
        </select>
      </div>

      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Média da turma" value={formatNumber(toNumber(turma.media_turma))} detail="nota consolidada" tone="blue" />
        <MetricCard label="Taxa de conclusão" value={`${formatNumber(toNumber(turma.taxa_conclusao), 0)}%`} detail="atividades avaliadas" tone="green" />
        <MetricCard label="Alunos ativos" value={`${turma.alunos_ativos}/${turma.total_alunos}`} detail="participação registrada" tone="yellow" />
        <MetricCard label="Abaixo da média" value={String(alunosBaixo.length)} detail="priorizar intervenção" tone="red" />
      </section>

      <section className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(340px,0.8fr)]">
        <article className="min-w-0 rounded-lg border border-slate-100 bg-white p-5 shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
          <h3 className="text-xl font-bold text-slate-950">Distribuição de notas</h3>
          <p className="mt-1 text-sm text-slate-500">Comparativo entre menor nota, mediana, média e maior nota.</p>
          <div className="mt-5 h-72 min-w-0">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData} margin={{ top: 12, right: 16, left: -18, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="nome" tick={{ fontSize: 12 }} />
                <YAxis domain={[0, 10]} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => formatNumber(Number(value))} />
                <Line type="monotone" dataKey="nota" stroke="#2563EB" strokeWidth={4} dot={{ r: 6, fill: '#E5FF25', stroke: '#1E40AF', strokeWidth: 2 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="min-w-0 rounded-lg border border-slate-100 bg-white p-5 shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
          <h3 className="text-xl font-bold text-slate-950">Alunos em atenção</h3>
          <p className="mt-1 text-sm text-slate-500">Estudantes com prioridade para acompanhamento individualizado.</p>
          <div className="mt-5 space-y-3">
            {alunosBaixo.map((item, index) => (
              <div key={`${item.alunoId}-${item.disciplina}-${index}`} className="rounded-lg border border-slate-100 bg-slate-50 p-4">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="break-words font-bold text-slate-900">Aluno {String(item.alunoId).slice(0, 8)}</p>
                    <p className="break-words text-sm text-slate-500">{item.disciplina}</p>
                  </div>
                  <span className="rounded-full bg-rose-50 px-3 py-1 text-xs font-bold text-rose-700 ring-1 ring-rose-200">
                    {formatNumber(toNumber(item.nota_media))}
                  </span>
                </div>
                <div className="mt-3 h-2 overflow-hidden rounded-full bg-white">
                  <div className="h-full rounded-full bg-rose-500" style={{ width: `${Math.min(100, toNumber(item.taxa_acerto))}%` }} />
                </div>
                <p className="mt-2 text-xs text-slate-500">
                  {item.questoes_acertadas}/{item.questoes_total} questões corretas · {formatDate(item.atualizado_em)}
                </p>
              </div>
            ))}
          </div>
        </article>
      </section>
    </div>
  )
}

export default function NotasPage() {
  const { user } = useAuth()
  const isAluno = user?.perfil?.startsWith('ALUNO')
  return isAluno ? <AlunoNotas /> : <GestaoNotas />
}

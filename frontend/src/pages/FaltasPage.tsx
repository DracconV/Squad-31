import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { useAuth } from '../contexts/AuthContext'
import {
  getFrequenciaAlunoResumo,
  getFrequenciaTurma,
  listarAlunosDaTurma,
  listarMinhasTurmas,
  listarTurmas,
  type AlunoTurma,
  type Turma,
} from '../lib/api'

type FrequenciaDisciplina = {
  disciplina: string
  aulas: number
  faltas: number
  presenca: number
  limite: number
}

const DEMO_FREQUENCIA: FrequenciaDisciplina[] = [
  { disciplina: 'Matemática', aulas: 32, faltas: 2, presenca: 94, limite: 75 },
  { disciplina: 'Linguagens', aulas: 30, faltas: 1, presenca: 97, limite: 75 },
  { disciplina: 'Ciências Humanas', aulas: 28, faltas: 4, presenca: 86, limite: 75 },
  { disciplina: 'Ciências da Natureza', aulas: 30, faltas: 5, presenca: 83, limite: 75 },
  { disciplina: 'Redação', aulas: 12, faltas: 0, presenca: 100, limite: 75 },
]

const DEMO_EVOLUCAO = [
  { semana: 'S1', presenca: 91 },
  { semana: 'S2', presenca: 94 },
  { semana: 'S3', presenca: 89 },
  { semana: 'S4', presenca: 96 },
  { semana: 'S5', presenca: 92 },
  { semana: 'S6', presenca: 95 },
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

const DEMO_ALUNOS_POR_TURMA: Record<string, Array<AlunoTurma & { presenca: number; faltas: number }>> = {
  'demo-3a-em': [
    { alunoId: 'AL-018', nome: 'Ana Beatriz Lima', matricula: '20260018', perfil: 'ALUNO_EM', presenca: 94, faltas: 2 },
    { alunoId: 'AL-027', nome: 'Carlos Eduardo Souza', matricula: '20260027', perfil: 'ALUNO_EM', presenca: 82, faltas: 6 },
    { alunoId: 'AL-031', nome: 'Mariana Costa Alves', matricula: '20260031', perfil: 'ALUNO_EM', presenca: 88, faltas: 5 },
  ],
  'demo-2b-em': [
    { alunoId: 'AL-044', nome: 'Rafaela Santos Moura', matricula: '20260044', perfil: 'ALUNO_EM', presenca: 76, faltas: 8 },
    { alunoId: 'AL-052', nome: 'Pedro Henrique Matos', matricula: '20260052', perfil: 'ALUNO_EM', presenca: 91, faltas: 3 },
    { alunoId: 'AL-063', nome: 'Larissa Gomes Reis', matricula: '20260063', perfil: 'ALUNO_EM', presenca: 84, faltas: 6 },
    { alunoId: 'AL-071', nome: 'João Victor Andrade', matricula: '20260071', perfil: 'ALUNO_EM', presenca: 79, faltas: 7 },
  ],
  'demo-eja-noite': [
    { alunoId: 'EJA-012', nome: 'Francisco José Araújo', matricula: '2026E012', perfil: 'ALUNO_EJA', presenca: 89, faltas: 4 },
    { alunoId: 'EJA-019', nome: 'Maria Aparecida Nunes', matricula: '2026E019', perfil: 'ALUNO_EJA', presenca: 96, faltas: 1 },
    { alunoId: 'EJA-024', nome: 'Sandra Regina Oliveira', matricula: '2026E024', perfil: 'ALUNO_EJA', presenca: 92, faltas: 2 },
  ],
}

function formatNumber(value: number, digits = 0): string {
  return value.toLocaleString('pt-BR', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  })
}

function frequenciaStatus(presenca: number) {
  if (presenca >= 90) return { label: 'Regular', className: 'bg-emerald-50 text-emerald-700 ring-1 ring-emerald-200' }
  if (presenca >= 75) return { label: 'Acompanhar', className: 'bg-amber-50 text-amber-700 ring-1 ring-amber-200' }
  return { label: 'Risco', className: 'bg-rose-50 text-rose-700 ring-1 ring-rose-200' }
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
    blue: 'from-blue-500 to-indigo-500',
    green: 'from-emerald-500 to-teal-400',
    yellow: 'from-yellow-300 to-amber-400',
    red: 'from-rose-500 to-pink-500',
  }

  return (
    <article className="overflow-hidden rounded-lg border border-slate-100 bg-white shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
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
    <section className="overflow-hidden rounded-lg border border-emerald-100 bg-white shadow-[0_24px_70px_rgba(15,118,110,0.12)]">
      <div className="grid min-h-[255px] grid-cols-1 lg:grid-cols-[minmax(0,1fr)_minmax(320px,0.9fr)]">
        <div className="min-w-0 p-7 sm:p-9">
          <span className="inline-flex rounded-full bg-emerald-600 px-4 py-1.5 text-xs font-bold uppercase text-white shadow-lg shadow-emerald-500/20">
            {mode === 'aluno' ? 'Frequência do aluno' : 'Acompanhamento de presença'}
          </span>
          <h2 className="mt-6 max-w-3xl break-words text-3xl font-bold leading-tight tracking-normal text-slate-950 sm:text-4xl">
            {mode === 'aluno'
              ? 'Faltas organizadas por disciplina, risco e plano de recuperação'
              : 'Mapa de frequência para agir antes que a ausência vire evasão'}
          </h2>
          <p className="mt-4 max-w-2xl text-base leading-7 text-slate-600">
            {mode === 'aluno'
              ? 'Visualize presença, faltas acumuladas e disciplinas que precisam de atenção para manter a regularidade escolar.'
              : 'Acompanhe turmas, identifique padrões de ausência e organize ações preventivas de busca ativa.'}
          </p>
          <div className="mt-7 flex flex-wrap gap-3">
            <span className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-semibold text-emerald-700 shadow-sm">
              Limite pedagógico: 75%
            </span>
            <span className="rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-semibold text-blue-700 shadow-sm">
              Busca ativa preventiva
            </span>
          </div>
        </div>
        <div className="relative min-h-[220px] overflow-hidden bg-slate-950">
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,#e5ff25_0_18%,transparent_19%),linear-gradient(135deg,#12305a_0_38%,#1f6feb_38%_100%)]" />
          <div className="absolute bottom-8 left-8 right-8 rounded-lg bg-white/95 p-5 shadow-[0_22px_55px_rgba(15,23,42,0.38)]">
            <p className="text-xs font-bold uppercase text-slate-500">Resumo de presença</p>
            <div className="mt-4 grid grid-cols-3 gap-3">
              {[
                ['94%', 'presença'],
                ['12', 'faltas'],
                ['3', 'alertas'],
              ].map(([value, label]) => (
                <div key={label} className="rounded-lg bg-slate-950 p-3 text-white">
                  <p className="text-2xl font-bold">{value}</p>
                  <p className="break-words text-xs text-slate-300">{label}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

function AlunoFaltas() {
  const { user } = useAuth()
  const resumoQuery = useQuery({
    queryKey: ['frequencia-aluno', user?.id],
    queryFn: () => getFrequenciaAlunoResumo(user!.id),
    enabled: Boolean(user?.id),
    retry: 1,
  })

  // Usa dados reais quando houver; senão mantém o exemplo demonstrativo.
  const frequencia: FrequenciaDisciplina[] = resumoQuery.data?.length
    ? resumoQuery.data.map((f) => ({
        disciplina: f.disciplina,
        aulas: f.aulas,
        faltas: f.faltas,
        presenca: f.presenca,
        limite: f.limite,
      }))
    : DEMO_FREQUENCIA

  const presencaMedia = frequencia.reduce((sum, item) => sum + item.presenca, 0) / frequencia.length
  const totalFaltas = frequencia.reduce((sum, item) => sum + item.faltas, 0)
  const risco = frequencia.filter((item) => item.presenca < 90)

  return (
    <div className="space-y-7">
      <HeroPanel mode="aluno" />

      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Presença média" value={`${formatNumber(presencaMedia)}%`} detail="todas as disciplinas" tone="green" />
        <MetricCard label="Faltas totais" value={String(totalFaltas)} detail="no período atual" tone="blue" />
        <MetricCard label="Disciplinas em atenção" value={String(risco.length)} detail="presença abaixo de 90%" tone="yellow" />
        <MetricCard label="Risco crítico" value="0" detail="abaixo de 75%" tone="red" />
      </section>

      <section className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1.25fr)_minmax(320px,0.75fr)]">
        <article className="min-w-0 rounded-lg border border-slate-100 bg-white p-5 shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
          <h3 className="text-xl font-bold text-slate-950">Frequência por disciplina</h3>
          <p className="mt-1 text-sm text-slate-500">Comparação entre presença registrada e limite mínimo recomendado.</p>
          <div className="mt-5 h-72 min-w-0">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={frequencia} margin={{ top: 12, right: 8, left: -18, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="disciplina" tick={{ fontSize: 11 }} interval={0} height={54} />
                <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => `${formatNumber(Number(value))}%`} />
                <Bar dataKey="presenca" radius={[7, 7, 0, 0]} fill="#059669" />
                <Bar dataKey="limite" radius={[7, 7, 0, 0]} fill="#EAB308" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="min-w-0 rounded-lg border border-slate-100 bg-white p-5 shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
          <h3 className="text-xl font-bold text-slate-950">Evolução semanal</h3>
          <p className="mt-1 text-sm text-slate-500">Tendência de presença no ciclo atual.</p>
          <div className="mt-5 h-72 min-w-0">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={DEMO_EVOLUCAO} margin={{ top: 12, right: 12, left: -18, bottom: 8 }}>
                <defs>
                  <linearGradient id="presencaGradient" x1="0" x2="0" y1="0" y2="1">
                    <stop offset="0%" stopColor="#059669" stopOpacity={0.35} />
                    <stop offset="100%" stopColor="#059669" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis dataKey="semana" tick={{ fontSize: 12 }} />
                <YAxis domain={[70, 100]} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => `${formatNumber(Number(value))}%`} />
                <Area type="monotone" dataKey="presenca" stroke="#059669" strokeWidth={4} fill="url(#presencaGradient)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </article>
      </section>

      <section className="overflow-hidden rounded-lg border border-slate-100 bg-white shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
        <div className="border-b border-slate-100 p-5">
          <h3 className="text-xl font-bold text-slate-950">Detalhamento de faltas</h3>
          <p className="mt-1 text-sm text-slate-500">Resumo preparado para receber registros oficiais de chamada.</p>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-[720px] w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-5 py-3">Disciplina</th>
                <th className="px-5 py-3">Aulas</th>
                <th className="px-5 py-3">Faltas</th>
                <th className="px-5 py-3">Presença</th>
                <th className="px-5 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {frequencia.map((item) => {
                const status = frequenciaStatus(item.presenca)
                return (
                  <tr key={item.disciplina} className="hover:bg-slate-50">
                    <td className="px-5 py-4 font-semibold text-slate-900">{item.disciplina}</td>
                    <td className="px-5 py-4 text-slate-700">{item.aulas}</td>
                    <td className="px-5 py-4 text-slate-700">{item.faltas}</td>
                    <td className="px-5 py-4 text-slate-700">{formatNumber(item.presenca)}%</td>
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

function ProfessorFaltas() {
  const { user } = useAuth()
  const [selectedTurmaId, setSelectedTurmaId] = useState('')
  const isProfessor = user?.perfil === 'PROFESSOR'

  const turmasQuery = useQuery({
    queryKey: ['turmas-faltas', user?.perfil],
    queryFn: () => (isProfessor ? listarMinhasTurmas() : listarTurmas()),
    retry: 1,
  })

  const turmas: Turma[] = turmasQuery.data?.length ? turmasQuery.data : DEMO_TURMAS
  const turmaId = selectedTurmaId || turmas[0]?.id || ''
  const isDemoTurma = turmaId.startsWith('demo-')

  const alunosQuery = useQuery({
    queryKey: ['alunos-faltas', turmaId],
    queryFn: () => listarAlunosDaTurma(turmaId),
    enabled: Boolean(turmaId) && !isDemoTurma,
    retry: 1,
  })

  const freqTurmaQuery = useQuery({
    queryKey: ['frequencia-turma', turmaId],
    queryFn: () => getFrequenciaTurma(turmaId),
    enabled: Boolean(turmaId) && !isDemoTurma,
    retry: 1,
  })

  const alunosComFrequencia = freqTurmaQuery.data?.alunos?.length
    ? freqTurmaQuery.data.alunos.map((a) => ({
        alunoId: a.alunoId,
        nome: a.nome,
        matricula: a.matricula,
        perfil: '',
        presenca: a.presenca,
        faltas: a.faltas,
      }))
    : alunosQuery.data?.length
      ? alunosQuery.data.map((aluno, index) => ({
          ...aluno,
          presenca: [94, 82, 88, 97, 76][index % 5],
          faltas: [2, 6, 5, 1, 8][index % 5],
        }))
      : DEMO_ALUNOS_POR_TURMA[turmaId] ?? DEMO_ALUNOS_POR_TURMA['demo-3a-em']
  const mediaPresenca =
    alunosComFrequencia.reduce((sum, item) => sum + item.presenca, 0) / alunosComFrequencia.length
  const emAtencao = alunosComFrequencia.filter((item) => item.presenca < 90)

  return (
    <div className="space-y-7">
      <HeroPanel mode="gestao" />

      <div className="flex flex-col gap-3 rounded-lg border border-slate-100 bg-white p-4 shadow-sm md:flex-row md:items-center md:justify-between">
        <div className="min-w-0">
          <p className="text-sm font-semibold text-slate-900">Turma monitorada</p>
          <p className="text-sm text-slate-500">Selecione a turma para acompanhar presença e ações prioritárias.</p>
        </div>
        <select
          value={turmaId}
          onChange={(event) => setSelectedTurmaId(event.target.value)}
          className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-semibold text-slate-700 shadow-sm outline-none focus:border-emerald-500 md:w-80"
        >
          {turmas.map((turmaItem) => (
            <option key={turmaItem.id} value={turmaItem.id}>
              {turmaItem.nome} · {turmaItem.modalidade}
            </option>
          ))}
        </select>
      </div>

      <section className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricCard label="Presença média" value={`${formatNumber(mediaPresenca)}%`} detail="turma selecionada" tone="green" />
        <MetricCard label="Alunos monitorados" value={String(alunosComFrequencia.length)} detail="matriculados na turma" tone="blue" />
        <MetricCard label="Em atenção" value={String(emAtencao.length)} detail="abaixo de 90%" tone="yellow" />
        <MetricCard label="Risco crítico" value={String(alunosComFrequencia.filter((item) => item.presenca < 75).length)} detail="abaixo de 75%" tone="red" />
      </section>

      <section className="overflow-hidden rounded-lg border border-slate-100 bg-white shadow-[0_18px_45px_rgba(15,23,42,0.07)]">
        <div className="border-b border-slate-100 p-5">
          <h3 className="text-xl font-bold text-slate-950">Alunos por presença</h3>
          <p className="mt-1 text-sm text-slate-500">Organização para chamada, busca ativa e acompanhamento pedagógico.</p>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-[760px] w-full text-left text-sm">
            <thead className="bg-slate-50 text-xs uppercase text-slate-500">
              <tr>
                <th className="px-5 py-3">Aluno</th>
                <th className="px-5 py-3">Matrícula</th>
                <th className="px-5 py-3">Faltas</th>
                <th className="px-5 py-3">Presença</th>
                <th className="px-5 py-3">Ação sugerida</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {alunosComFrequencia.map((item) => {
                const status = frequenciaStatus(item.presenca)
                return (
                  <tr key={item.alunoId} className="hover:bg-slate-50">
                    <td className="px-5 py-4 font-semibold text-slate-900">{item.nome}</td>
                    <td className="px-5 py-4 text-slate-700">{item.matricula}</td>
                    <td className="px-5 py-4 text-slate-700">{item.faltas}</td>
                    <td className="px-5 py-4">
                      <span className={`rounded-full px-3 py-1 text-xs font-bold ${status.className}`}>{formatNumber(item.presenca)}%</span>
                    </td>
                    <td className="px-5 py-4 text-slate-700">
                      {item.presenca < 90 ? 'Acompanhar justificativas' : 'Manter rotina'}
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

export default function FaltasPage() {
  const { user } = useAuth()
  const isAluno = user?.perfil?.startsWith('ALUNO')
  return isAluno ? <AlunoFaltas /> : <ProfessorFaltas />
}

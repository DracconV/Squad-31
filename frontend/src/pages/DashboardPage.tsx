import type { ReactElement } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { StatCard } from '../components/StatCard'
import { ProgressBar } from '../components/ProgressBar'
import { StatusBanner } from '../components/StatusBanner'
import type { Perfil } from '../lib/auth'

/* ── Dashboard por perfil ───────────────────────────────── */

function DashboardAlunoEM() {
  return (
    <>
      <StatusBanner variant="info">
        Seu próximo simulado está agendado para <strong>sexta-feira às 14h</strong>.
      </StatusBanner>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
        <StatCard title="Simulados realizados"  value={12} trend="up"      trendValue="+2 este mês" />
        <StatCard title="Média geral"           value="72%" trend="up"     trendValue="+5 pontos" />
        <StatCard title="Questões respondidas"  value={340} trend="neutral" trendValue="esta semana" />
        <StatCard title="Sequência de dias"     value="7 dias" trend="up"  trendValue="recorde!" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Desempenho por disciplina</h2>
          <div className="space-y-3">
            <ProgressBar label="Matemática"         value={78} color="blue" />
            <ProgressBar label="Língua Portuguesa"  value={65} color="blue" />
            <ProgressBar label="Ciências da Natureza" value={54} color="yellow" />
            <ProgressBar label="Ciências Humanas"   value={82} color="green" />
          </div>
        </div>

        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Últimos simulados</h2>
          <div className="space-y-2 text-sm">
            {[
              { nome: 'Simulado ENEM — Geral',     nota: 680, data: '12/05' },
              { nome: 'Simulado Matemática',        nota: 72,  data: '05/05' },
              { nome: 'Simulado Ciências Humanas',  nota: 84,  data: '28/04' },
            ].map((s) => (
              <div key={s.nome} className="flex justify-between items-center py-2 border-b border-gray-50">
                <span className="text-gray-700">{s.nome}</span>
                <span className="text-xs text-gray-400">{s.data}</span>
                <span className="font-semibold text-blue-600">{s.nota}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}

function DashboardAlunoEJA() {
  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <StatCard title="Atividades concluídas" value={8}    trend="up"  trendValue="esta semana" />
        <StatCard title="Sequência de dias"     value="5 dias" trend="up" trendValue="continue!" />
      </div>
      <div className="mt-6 bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <h2 className="font-semibold text-gray-700 mb-4">Progresso desta semana</h2>
        <ProgressBar label="Meta semanal" value={60} color="green" />
        <p className="text-xs text-gray-400 mt-3">Você está no caminho certo. Faltam 2 atividades para bater a meta!</p>
      </div>
    </>
  )
}

function DashboardAlunoProf() {
  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard title="Cursos em andamento"  value={2} />
        <StatCard title="Módulos concluídos"   value={7} trend="up" trendValue="esta semana" />
        <StatCard title="Certificados emitidos" value={1} />
      </div>
      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Progresso dos cursos</h2>
          <ProgressBar label="Técnico em Informática" value={68} color="blue" />
          <div className="mt-3">
            <ProgressBar label="Eletrotécnica"          value={35} color="yellow" />
          </div>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Próximas provas práticas</h2>
          <p className="text-sm text-gray-500">Informática — <strong>20/05 às 09h</strong> — Sala 3B</p>
        </div>
      </div>
    </>
  )
}

function DashboardProfessor() {
  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Turmas ativas"         value={4} />
        <StatCard title="Alunos acompanhados"   value={112} />
        <StatCard title="Provas criadas"         value={9}  trend="up" trendValue="+1 esta semana" />
        <StatCard title="Alunos em atenção"      value={6}  trend="down" trendValue="abaixo da média" />
      </div>
      <div className="mt-6 bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <h2 className="font-semibold text-gray-700 mb-4">Tópicos com maior taxa de erros</h2>
        <div className="space-y-3">
          <ProgressBar label="Funções do 2º grau"  value={68} color="red" />
          <ProgressBar label="Análise Combinatória" value={57} color="yellow" />
          <ProgressBar label="Geometria Espacial"   value={44} color="yellow" />
        </div>
        <p className="text-xs text-gray-400 mt-2">Percentual de erros por tópico nos últimos 30 dias.</p>
      </div>
    </>
  )
}

function DashboardAdminEscola() {
  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Alunos matriculados"  value={640} />
        <StatCard title="Turmas ativas"        value={22} />
        <StatCard title="Professores"          value={34} />
        <StatCard title="Média institucional"  value="68%" trend="up" trendValue="+3 pts vs. mês ant." />
      </div>
      <div className="mt-6 bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <h2 className="font-semibold text-gray-700 mb-4">Adesão à plataforma</h2>
        <ProgressBar label="Alunos com acesso ativo" value={81} color="green" />
        <div className="mt-3">
          <ProgressBar label="Professores com acesso ativo" value={94} color="green" />
        </div>
      </div>
    </>
  )
}

function DashboardAdminSeed() {
  return (
    <>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard title="Escolas na plataforma" value={182} trend="up"  trendValue="+8 este mês" />
        <StatCard title="Alunos ativos"          value="48 mil" />
        <StatCard title="Professores"            value="3.200" />
        <StatCard title="Média estadual"         value="64%" trend="up" trendValue="+2 pts" />
      </div>
      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Desempenho por regional</h2>
          <div className="space-y-3">
            <ProgressBar label="Regional Maceió"   value={72} color="green" />
            <ProgressBar label="Regional Arapiraca" value={61} color="blue" />
            <ProgressBar label="Regional Palmeira" value={55} color="yellow" />
          </div>
        </div>
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <h2 className="font-semibold text-gray-700 mb-4">Alertas operacionais</h2>
          <div className="space-y-2 text-sm text-gray-600">
            <p>⚠️ 3 escolas abaixo de 50% de adesão</p>
            <p>⚠️ Relatório mensal pendente de 7 escolas</p>
            <p>✅ Exportação de dados LGPD em dia</p>
          </div>
        </div>
      </div>
    </>
  )
}

/* ── Dispatcher ─────────────────────────────────────────── */

const DASHBOARD_MAP: Partial<Record<Perfil, () => ReactElement>> = {
  ALUNO_EM:    DashboardAlunoEM,
  ALUNO_EJA:   DashboardAlunoEJA,
  ALUNO_PROF:  DashboardAlunoProf,
  PROFESSOR:   DashboardProfessor,
  ADMIN_ESCOLA: DashboardAdminEscola,
  ADMIN_SEED:  DashboardAdminSeed,
}

export default function DashboardPage() {
  const { user } = useAuth()
  const Component = user ? DASHBOARD_MAP[user.perfil as Perfil] : null
  if (!Component) return null
  return <Component />
}

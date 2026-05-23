import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'

/* ── Tipos ──────────────────────────────────────────────────── */

interface Disciplina {
  id: string
  nome: string
}

interface Alternativa {
  id: string
  texto: string
  correta: boolean
  ordem: number
}

interface Questao {
  id: string
  enunciado: string
  tipo: string
  dificuldade: string
  tipoUso: string
  disciplina: string
  alternativas: Alternativa[]
}

interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

/* ── Helpers de API ─────────────────────────────────────────── */

async function fetchDisciplinas(): Promise<Disciplina[]> {
  const { data } = await api.get<Disciplina[]>('/disciplinas')
  return data
}

async function fetchQuestoes(params: {
  disciplinaId?: string
  dificuldade?: string
  page: number
  size: number
}): Promise<Page<Questao>> {
  const { data } = await api.get<Page<Questao>>('/questoes', { params })
  return data
}

async function fetchStats(): Promise<{ total: number }> {
  const { data } = await api.get<{ total: number }>('/questoes/stats')
  return data
}

/* ── Componente principal ───────────────────────────────────── */

export default function BancoQuestoesPage() {
  const { user } = useAuth()
  const [disciplinaId, setDisciplinaId] = useState('')
  const [dificuldade, setDificuldade] = useState('')
  const [page, setPage] = useState(0)
  const [expandida, setExpandida] = useState<string | null>(null)
  const [importando, setImportando] = useState(false)
  const [importMsg, setImportMsg] = useState('')

  const size = 20

  const { data: disciplinas = [] } = useQuery({
    queryKey: ['disciplinas'],
    queryFn: fetchDisciplinas,
  })

  const { data: stats } = useQuery({
    queryKey: ['questoes-stats'],
    queryFn: fetchStats,
    refetchInterval: importando ? 5000 : false,
  })

  const { data: pageData, isLoading, isError } = useQuery({
    queryKey: ['questoes', disciplinaId, dificuldade, page],
    queryFn: () =>
      fetchQuestoes({
        disciplinaId: disciplinaId || undefined,
        dificuldade: dificuldade || undefined,
        page,
        size,
      }),
  })

  const questoes = pageData?.content ?? []
  const totalPages = pageData?.totalPages ?? 0
  const totalElements = pageData?.totalElements ?? 0

  function handleFiltro() {
    setPage(0)
    setExpandida(null)
  }

  async function handleImportar() {
    if (!confirm('Isso vai (re)importar todas as questões do ENEM da API pública. Continuar?')) return
    setImportando(true)
    setImportMsg('')
    try {
      const { data } = await api.post<{ status: string }>('/questoes/importar')
      setImportMsg(data.status)
    } catch {
      setImportMsg('Erro ao iniciar importação. Verifique os logs.')
    }
  }

  const isAdmin = user?.perfil === 'ADMIN_SEED'

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-800">Banco de Questões</h1>
          <p className="text-sm text-gray-500 mt-0.5">
            {stats ? (
              <span>
                <strong>{stats.total.toLocaleString('pt-BR')}</strong> questões disponíveis
              </span>
            ) : (
              'Carregando estatísticas...'
            )}
          </p>
        </div>
        {isAdmin && (
          <button
            onClick={handleImportar}
            disabled={importando}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-60 transition-colors"
          >
            {importando ? '⏳ Importando...' : '⬇️ Importar ENEM'}
          </button>
        )}
      </div>

      {importMsg && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm text-blue-700">
          {importMsg}
        </div>
      )}

      {/* Filtros */}
      <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 flex flex-wrap gap-3 items-end">
        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Disciplina</label>
          <select
            value={disciplinaId}
            onChange={(e) => { setDisciplinaId(e.target.value); handleFiltro() }}
            className="text-sm border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Todas</option>
            {disciplinas.map((d) => (
              <option key={d.id} value={d.id}>{d.nome}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="block text-xs font-medium text-gray-600 mb-1">Dificuldade</label>
          <select
            value={dificuldade}
            onChange={(e) => { setDificuldade(e.target.value); handleFiltro() }}
            className="text-sm border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">Todas</option>
            <option value="FACIL">Fácil</option>
            <option value="MEDIA">Média</option>
            <option value="DIFICIL">Difícil</option>
          </select>
        </div>

        {(disciplinaId || dificuldade) && (
          <button
            onClick={() => { setDisciplinaId(''); setDificuldade(''); setPage(0) }}
            className="text-sm text-gray-500 hover:text-gray-700 underline"
          >
            Limpar filtros
          </button>
        )}

        <div className="ml-auto text-sm text-gray-500">
          {totalElements > 0 && `${totalElements.toLocaleString('pt-BR')} resultado(s)`}
        </div>
      </div>

      {/* Lista de questões */}
      {isLoading && (
        <div className="text-center py-12 text-gray-400">Carregando questões...</div>
      )}

      {isError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-600">
          Erro ao carregar questões. Verifique se o serviço está online.
        </div>
      )}

      {!isLoading && !isError && questoes.length === 0 && (
        <div className="bg-white rounded-xl p-10 shadow-sm border border-gray-100 text-center text-gray-400">
          <p className="text-4xl mb-3">📋</p>
          <p className="font-medium">Nenhuma questão encontrada</p>
          <p className="text-sm mt-1">
            {stats?.total === 0
              ? 'O banco ainda está vazio. Use o botão "Importar ENEM" para popular.'
              : 'Tente ajustar os filtros.'}
          </p>
        </div>
      )}

      <div className="space-y-3">
        {questoes.map((q, idx) => (
          <QuestaoCard
            key={q.id}
            questao={q}
            numero={page * size + idx + 1}
            expandida={expandida === q.id}
            onToggle={() => setExpandida(expandida === q.id ? null : q.id)}
            isAdmin={isAdmin}
          />
        ))}
      </div>

      {/* Paginação */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 disabled:opacity-40 hover:bg-gray-50"
          >
            ← Anterior
          </button>
          <span className="text-sm text-gray-600">
            Página {page + 1} de {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 disabled:opacity-40 hover:bg-gray-50"
          >
            Próxima →
          </button>
        </div>
      )}
    </div>
  )
}

/* ── Card de Questão ────────────────────────────────────────── */

function QuestaoCard({
  questao,
  numero,
  expandida,
  onToggle,
  isAdmin,
}: {
  questao: Questao
  numero: number
  expandida: boolean
  onToggle: () => void
  isAdmin: boolean
}) {
  const dificuldadeCor = {
    FACIL: 'bg-green-100 text-green-700',
    MEDIA: 'bg-yellow-100 text-yellow-700',
    DIFICIL: 'bg-red-100 text-red-700',
  }[questao.dificuldade] ?? 'bg-gray-100 text-gray-600'

  const letras = ['A', 'B', 'C', 'D', 'E']

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Header clicável */}
      <button
        onClick={onToggle}
        className="w-full text-left p-4 flex items-start gap-3 hover:bg-gray-50 transition-colors"
      >
        <span className="text-xs font-bold text-gray-400 mt-0.5 min-w-[2rem]">
          #{numero}
        </span>
        <div className="flex-1 min-w-0">
          <p className="text-sm text-gray-800 line-clamp-2">{questao.enunciado}</p>
          <div className="flex flex-wrap items-center gap-2 mt-2">
            {questao.disciplina && (
              <span className="text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full">
                {questao.disciplina}
              </span>
            )}
            <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${dificuldadeCor}`}>
              {questao.dificuldade}
            </span>
            <span className="text-xs bg-gray-100 text-gray-500 px-2 py-0.5 rounded-full">
              {questao.tipoUso}
            </span>
          </div>
        </div>
        <span className="text-gray-400 text-xs mt-0.5">{expandida ? '▲' : '▼'}</span>
      </button>

      {/* Conteúdo expandido */}
      {expandida && (
        <div className="border-t border-gray-100 p-4 space-y-3">
          <p className="text-sm text-gray-700 whitespace-pre-wrap">{questao.enunciado}</p>

          {questao.alternativas && questao.alternativas.length > 0 && (
            <div className="space-y-2 mt-3">
              {questao.alternativas
                .sort((a, b) => a.ordem - b.ordem)
                .map((alt, i) => (
                  <div
                    key={alt.id}
                    className={`flex gap-2 p-2.5 rounded-lg text-sm ${
                      isAdmin && alt.correta
                        ? 'bg-green-50 border border-green-200'
                        : 'bg-gray-50 border border-gray-100'
                    }`}
                  >
                    <span className={`font-bold min-w-[1.2rem] ${
                      isAdmin && alt.correta ? 'text-green-600' : 'text-gray-400'
                    }`}>
                      {letras[i] ?? alt.ordem})
                    </span>
                    <span className="text-gray-700">{alt.texto}</span>
                    {isAdmin && alt.correta && (
                      <span className="ml-auto text-green-600 font-medium text-xs">✓ Correta</span>
                    )}
                  </div>
                ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}

import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api, favoritarQuestao, desfavoritarQuestao, listarQuestoesFavoritas, getGabaritoQuestao, type Gabarito } from '../lib/api'
import { useAuth } from '../contexts/AuthContext'

/* ── Renderizador de Markdown simples ───────────────────────── */

function MarkdownText({ text }: { text: string }) {
  if (!text) return null

  // Divide o texto em segmentos: imagens, negrito, itálico e texto normal
  const segments: React.ReactNode[] = []
  let remaining = text
  let key = 0

  while (remaining.length > 0) {
    // Imagem: ![alt](url)
    const imgMatch = remaining.match(/^([\s\S]*?)!\[([^\]]*)\]\((https?:\/\/[^)]+)\)/)
    if (imgMatch) {
      if (imgMatch[1]) segments.push(<span key={key++}>{renderInline(imgMatch[1])}</span>)
      segments.push(
        <img
          key={key++}
          src={imgMatch[3]}
          alt={imgMatch[2] || 'imagem'}
          className="max-w-full rounded my-2 mx-auto block"
          loading="lazy"
          onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
        />
      )
      remaining = remaining.slice(imgMatch[0].length)
      continue
    }

    // Sem mais imagens — renderiza o resto
    segments.push(<span key={key++}>{renderInline(remaining)}</span>)
    break
  }

  return <>{segments}</>
}

// Renderiza negrito e itálico dentro de um trecho de texto
function renderInline(text: string): React.ReactNode[] {
  const parts: React.ReactNode[] = []
  const regex = /(\*\*([^*]+)\*\*)|(_([^_]+)_)/g
  let last = 0
  let match: RegExpExecArray | null

  while ((match = regex.exec(text)) !== null) {
    if (match.index > last) parts.push(text.slice(last, match.index))
    if (match[1]) parts.push(<strong key={match.index}>{match[2]}</strong>)
    else if (match[3]) parts.push(<em key={match.index}>{match[4]}</em>)
    last = match.index + match[0].length
  }
  if (last < text.length) parts.push(text.slice(last))
  return parts
}

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
  explicacao?: string | null
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
  const [favoritas, setFavoritas] = useState<Set<string>>(new Set())

  const size = 20

  const { data: disciplinas = [] } = useQuery({
    queryKey: ['disciplinas'],
    queryFn: fetchDisciplinas,
  })

  // Conjunto de IDs marcados para revisão pelo aluno
  useQuery({
    queryKey: ['questoes-favoritas-ids'],
    queryFn: async () => {
      const favs = await listarQuestoesFavoritas()
      setFavoritas(new Set(favs.map((f) => f.id)))
      return favs
    },
  })

  async function toggleFavorito(questaoId: string) {
    const jaFavorita = favoritas.has(questaoId)
    // Atualização otimista
    setFavoritas((prev) => {
      const next = new Set(prev)
      if (jaFavorita) next.delete(questaoId)
      else next.add(questaoId)
      return next
    })
    try {
      if (jaFavorita) await desfavoritarQuestao(questaoId)
      else await favoritarQuestao(questaoId)
    } catch {
      // Reverte em caso de falha
      setFavoritas((prev) => {
        const next = new Set(prev)
        if (jaFavorita) next.add(questaoId)
        else next.delete(questaoId)
        return next
      })
    }
  }

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
            <option value="MEDIO">Médio</option>
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
            favorita={favoritas.has(q.id)}
            onToggleFavorito={() => toggleFavorito(q.id)}
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
  favorita,
  onToggleFavorito,
}: {
  questao: Questao
  numero: number
  expandida: boolean
  onToggle: () => void
  favorita: boolean
  onToggleFavorito: () => void
}) {
  const dificuldadeCor = {
    FACIL: 'bg-green-100 text-green-700',
    MEDIA: 'bg-yellow-100 text-yellow-700',
    DIFICIL: 'bg-red-100 text-red-700',
  }[questao.dificuldade] ?? 'bg-gray-100 text-gray-600'

  const letras = ['A', 'B', 'C', 'D', 'E']

  // Modo praticar (todos os perfis): seleciona uma alternativa e clica em "Verificar".
  // Se o gabarito já veio no listing (professor/admin), verifica localmente; senão busca no backend.
  const corretaLocalId = questao.alternativas?.find((a) => a.correta === true)?.id ?? null
  const [selecionada, setSelecionada] = useState<string | null>(null)
  const [gabarito, setGabarito] = useState<Gabarito | null>(null)
  const [verificando, setVerificando] = useState(false)

  async function verificar() {
    if (!selecionada || gabarito) return
    if (corretaLocalId) {
      // Professor/admin já têm o gabarito nos dados — sem ida ao backend
      setGabarito({ questaoId: questao.id, alternativaCorretaId: corretaLocalId, explicacao: questao.explicacao ?? null })
      return
    }
    setVerificando(true)
    try {
      setGabarito(await getGabaritoQuestao(questao.id))
    } catch {
      /* ignora — usuário pode tentar de novo */
    } finally {
      setVerificando(false)
    }
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
      {/* Header clicável + botão de favoritar */}
      <div className="flex items-start">
        <button
          onClick={onToggle}
          className="flex-1 text-left p-4 flex items-start gap-3 hover:bg-gray-50 transition-colors min-w-0"
        >
          <span className="text-xs font-bold text-gray-400 mt-0.5 min-w-[2rem]">
            #{numero}
          </span>
          <div className="flex-1 min-w-0">
            <p className="text-sm text-gray-800 line-clamp-2 overflow-hidden">{questao.enunciado.replace(/!\[[^\]]*\]\([^)]+\)/g, '[imagem]')}</p>
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
        <button
          onClick={onToggleFavorito}
          aria-label={favorita ? 'Remover dos favoritos' : 'Marcar para revisão'}
          aria-pressed={favorita}
          title={favorita ? 'Remover da revisão' : 'Marcar para revisão'}
          className={`shrink-0 p-4 text-xl transition-colors ${favorita ? 'text-yellow-400' : 'text-gray-300 hover:text-yellow-400'}`}
        >
          {favorita ? '★' : '☆'}
        </button>
      </div>

      {/* Conteúdo expandido */}
      {expandida && (
        <div className="border-t border-gray-100 p-4 space-y-3">
          <div className="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">
            <MarkdownText text={questao.enunciado} />
          </div>

          {/* ── Modo praticar (todos): selecionar e verificar ───────── */}
          {questao.alternativas && questao.alternativas.length > 0 && (
            <div className="space-y-2 mt-3">
              {[...questao.alternativas].sort((a, b) => a.ordem - b.ordem).map((alt, i) => {
                const isCorreta = gabarito?.alternativaCorretaId === alt.id
                const isSelecionada = selecionada === alt.id
                const isSelecionadaErrada = gabarito != null && isSelecionada && !isCorreta
                let cor = 'bg-gray-50 border-gray-100 hover:border-blue-300'
                if (gabarito) {
                  if (isCorreta) cor = 'bg-green-50 border-green-300'
                  else if (isSelecionadaErrada) cor = 'bg-red-50 border-red-300'
                } else if (isSelecionada) {
                  cor = 'bg-blue-50 border-blue-400 ring-1 ring-blue-300'
                }
                return (
                  <button
                    key={alt.id}
                    type="button"
                    disabled={gabarito != null}
                    onClick={() => setSelecionada(alt.id)}
                    className={`w-full text-left flex gap-2 p-2.5 rounded-lg text-sm border transition disabled:cursor-default ${cor}`}
                  >
                    <span className={`font-bold min-w-[1.2rem] ${
                      isCorreta ? 'text-green-600' : isSelecionadaErrada ? 'text-red-500' : isSelecionada ? 'text-blue-600' : 'text-gray-400'
                    }`}>
                      {letras[i] ?? alt.ordem})
                    </span>
                    <span className="text-gray-700">{alt.texto}</span>
                    {gabarito && isCorreta && <span className="ml-auto text-green-600 font-medium text-xs">✓ Correta</span>}
                    {isSelecionadaErrada && <span className="ml-auto text-red-500 font-medium text-xs">Sua resposta</span>}
                  </button>
                )
              })}

              {!gabarito ? (
                <button
                  type="button"
                  onClick={verificar}
                  disabled={!selecionada || verificando}
                  className="mt-1 px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
                >
                  {verificando ? 'Verificando…' : 'Verificar resposta'}
                </button>
              ) : (
                <p className={`text-sm font-medium ${gabarito.alternativaCorretaId === selecionada ? 'text-green-600' : 'text-red-500'}`}>
                  {gabarito.alternativaCorretaId === selecionada ? '✓ Você acertou!' : '✗ Resposta incorreta.'}
                </p>
              )}
            </div>
          )}

          {/* Explicação: vem da questão (professor) ou do gabarito revelado (aluno) */}
          {(questao.explicacao || gabarito?.explicacao) && (
            <div className="bg-blue-50 border border-blue-100 rounded-lg p-3 text-sm text-blue-900 mt-3">
              <span className="font-semibold">💡 Explicação: </span>
              <span className="whitespace-pre-wrap">{questao.explicacao ?? gabarito?.explicacao}</span>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

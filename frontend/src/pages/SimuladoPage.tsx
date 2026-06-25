import { useEffect, useRef, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Timer, Check, X, Lightbulb, ChevronLeft, ChevronRight, Flag } from 'lucide-react'
import MarkdownText from '../components/MarkdownText'
import {
  buscarSimulado,
  iniciarSimulado,
  getQuestao,
  responderSimulado,
  finalizarSimulado,
  revisaoSimulado,
  type Simulado,
  type Questao,
  type ResultadoSimulado,
  type RevisaoQuestao,
} from '../lib/api'

type Fase = 'carregando' | 'executando' | 'resultado' | 'erro'

const LETRAS = ['A', 'B', 'C', 'D', 'E', 'F']

function formatarTempo(segundos: number): string {
  const m = Math.floor(segundos / 60)
  const s = segundos % 60
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export default function SimuladoPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const [fase, setFase] = useState<Fase>('carregando')
  const [erro, setErro] = useState('')
  const [simulado, setSimulado] = useState<Simulado | null>(null)
  const [questoes, setQuestoes] = useState<Questao[]>([])
  const [respostas, setRespostas] = useState<Record<number, string>>({})
  const [atual, setAtual] = useState(0)
  const [restante, setRestante] = useState<number | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [resultado, setResultado] = useState<ResultadoSimulado | null>(null)
  const [revisao, setRevisao] = useState<RevisaoQuestao[] | null>(null)

  const finalizadoRef = useRef(false)

  // ── Finalização (manual ou por tempo) ────────────────────────
  const finalizar = useCallback(async () => {
    if (!id || finalizadoRef.current) return
    finalizadoRef.current = true
    setEnviando(true)
    try {
      const res = await finalizarSimulado(id, respostas)
      localStorage.removeItem(`simulado_inicio_${id}`) // limpa p/ um próximo "Refazer" começar do zero
      setResultado(res)
      setFase('resultado')
    } catch {
      setErro('Erro ao finalizar o simulado. Tente novamente.')
      finalizadoRef.current = false
    } finally {
      setEnviando(false)
    }
  }, [id, respostas])

  // ── Carregamento inicial: detalhe + sessão + questões ─────────
  useEffect(() => {
    if (!id) return
    let cancelado = false

    ;(async () => {
      try {
        const detalhe = await buscarSimulado(id)
        const sessao = await iniciarSimulado(id)
        const ids = detalhe.questaoIds ?? []
        const qs = await Promise.all(ids.map((qid) => getQuestao(qid)))

        if (cancelado) return

        setSimulado(detalhe)
        setQuestoes(qs)

        // Restaura respostas previamente salvas na sessão (chaves vêm como string no JSON)
        const restauradas: Record<number, string> = {}
        Object.entries(sessao.respostas ?? {}).forEach(([k, v]) => {
          if (v) restauradas[Number(k)] = v as string
        })
        setRespostas(restauradas)

        // Início persistido no navegador para o contador NÃO reiniciar ao dar F5.
        // Usa o horário guardado localmente (1ª vez que este aluno abriu) — não o da sessão,
        // que pode reiniciar se o cookie de sessão não persistir através do gateway.
        const chaveInicio = `simulado_inicio_${id}`
        const salvo = localStorage.getItem(chaveInicio)
        let inicioMs: number
        if (salvo) {
          inicioMs = Number(salvo)
        } else {
          inicioMs = new Date(sessao.iniciadoEm).getTime() || Date.now()
          localStorage.setItem(chaveInicio, String(inicioMs))
        }

        const fimMs = inicioMs + detalhe.tempoMinutos * 60_000
        const segRestantes = Math.max(0, Math.round((fimMs - Date.now()) / 1000))
        setRestante(segRestantes)
        setFase('executando')
      } catch {
        if (!cancelado) {
          setErro('Não foi possível carregar o simulado.')
          setFase('erro')
        }
      }
    })()

    return () => { cancelado = true }
  }, [id])

  // ── Cronômetro ────────────────────────────────────────────────
  useEffect(() => {
    if (fase !== 'executando' || restante === null) return
    if (restante <= 0) { finalizar(); return }
    const t = setInterval(() => {
      setRestante((r) => (r === null ? r : r - 1))
    }, 1000)
    return () => clearInterval(t)
  }, [fase, restante, finalizar])

  // ── Seleção de resposta (auto-save) ───────────────────────────
  function responder(questaoIndex: number, alternativaId: string) {
    if (!id) return
    setRespostas((r) => ({ ...r, [questaoIndex]: alternativaId }))
    responderSimulado(id, questaoIndex, alternativaId).catch(() => {
      /* auto-save best-effort; resposta permanece em memória */
    })
  }

  async function abrirRevisao() {
    if (!id) return
    try {
      const r = await revisaoSimulado(id)
      setRevisao(r)
    } catch {
      setErro('Não foi possível carregar a revisão comentada.')
    }
  }

  // ── Render ────────────────────────────────────────────────────
  if (fase === 'carregando') {
    return <div className="text-center py-20 text-gray-400">Carregando simulado…</div>
  }

  if (fase === 'erro') {
    return (
      <div className="bg-white rounded-xl p-10 text-center border border-gray-100 space-y-3">
        <p className="text-red-600">{erro || 'Erro ao carregar.'}</p>
        <button onClick={() => navigate('/simulados')} className="text-brand-600 hover:underline text-sm">
          ← Voltar para simulados
        </button>
      </div>
    )
  }

  // ── Tela de resultado ─────────────────────────────────────────
  if (fase === 'resultado' && resultado) {
    const pct = resultado.total > 0 ? Math.round((resultado.acertos / resultado.total) * 100) : 0
    return (
      <div className="space-y-6">
        <div className="bg-white rounded-xl p-8 shadow-sm border border-gray-100 text-center space-y-3">
          <div className="text-5xl">{pct >= 60 ? '🎉' : '📝'}</div>
          <h1 className="text-xl font-bold text-gray-800">Simulado finalizado!</h1>
          <p className="text-4xl font-bold text-brand-600">{Number(resultado.nota).toFixed(1)}</p>
          <p className="text-sm text-gray-500">
            {resultado.acertos} de {resultado.total} questões corretas ({pct}%)
          </p>
          <div className="flex gap-3 justify-center pt-3">
            {!revisao && (
              <button
                onClick={abrirRevisao}
                className="px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700"
              >
                Ver gabarito comentado
              </button>
            )}
            <button
              onClick={() => navigate('/simulados')}
              className="px-4 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm font-medium hover:bg-gray-200"
            >
              Voltar para simulados
            </button>
          </div>
          {erro && <p className="text-sm text-red-500">{erro}</p>}
        </div>

        {revisao && <Revisao revisao={revisao} respostas={respostas} />}
      </div>
    )
  }

  // ── Tela de execução ──────────────────────────────────────────
  const questao = questoes[atual]
  const respondidas = Object.keys(respostas).length
  const tempoBaixo = restante !== null && restante <= 60

  return (
    <div className="space-y-4">
      {/* Barra superior: título + timer */}
      <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 flex items-center justify-between gap-3 sticky top-0 z-10">
        <div className="min-w-0">
          <h1 className="font-bold text-gray-800 truncate">{simulado?.titulo}</h1>
          <p className="text-xs text-gray-500">{respondidas}/{questoes.length} respondidas</p>
        </div>
        <div
          role="timer"
          aria-live="polite"
          className={`inline-flex items-center gap-1.5 text-lg font-mono font-bold px-3 py-1.5 rounded-lg ${
            tempoBaixo ? 'bg-red-50 text-red-600 animate-pulse' : 'bg-brand-50 text-brand-700'
          }`}
        >
          <Timer size={18} /> {restante !== null ? formatarTempo(restante) : '--:--'}
        </div>
      </div>

      {/* Navegador de questões (palette) */}
      <div className="bg-white rounded-xl p-3 shadow-sm border border-gray-100 flex flex-wrap gap-2">
        {questoes.map((_, i) => (
          <button
            key={i}
            onClick={() => setAtual(i)}
            aria-label={`Ir para questão ${i + 1}${respostas[i] ? ' (respondida)' : ''}`}
            className={`w-9 h-9 rounded-lg text-sm font-medium transition ${
              i === atual
                ? 'bg-brand-600 text-white'
                : respostas[i]
                  ? 'bg-green-100 text-green-700'
                  : 'bg-gray-100 text-gray-500 hover:bg-gray-200'
            }`}
          >
            {i + 1}
          </button>
        ))}
      </div>

      {/* Questão atual */}
      {questao && (
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold text-gray-400">Questão {atual + 1} de {questoes.length}</span>
            {questao.disciplina && (
              <span className="text-xs bg-brand-50 text-brand-600 px-2 py-0.5 rounded-full">{questao.disciplina}</span>
            )}
          </div>

          <div className="text-sm text-gray-800 leading-relaxed"><MarkdownText text={questao.enunciado} /></div>

          <div className="space-y-2">
            {[...questao.alternativas].sort((a, b) => a.ordem - b.ordem).map((alt, i) => {
              const selecionada = respostas[atual] === alt.id
              return (
                <button
                  key={alt.id}
                  onClick={() => responder(atual, alt.id)}
                  className={`w-full text-left flex gap-3 p-3 rounded-lg border text-sm transition ${
                    selecionada
                      ? 'bg-brand-50 border-brand-400 ring-1 ring-brand-300'
                      : 'bg-gray-50 border-gray-200 hover:border-brand-300'
                  }`}
                >
                  <span className={`font-bold ${selecionada ? 'text-brand-600' : 'text-gray-400'}`}>
                    {LETRAS[i] ?? i + 1})
                  </span>
                  <span className="text-gray-700">{alt.texto}</span>
                </button>
              )
            })}
          </div>

          {/* Navegação */}
          <div className="flex items-center justify-between pt-2">
            <button
              onClick={() => setAtual((a) => Math.max(0, a - 1))}
              disabled={atual === 0}
              className="inline-flex items-center gap-1 px-4 py-2 rounded-lg border border-gray-300 text-sm text-gray-600 disabled:opacity-40 hover:bg-gray-50"
            >
              <ChevronLeft size={16} /> Anterior
            </button>
            {atual < questoes.length - 1 ? (
              <button
                onClick={() => setAtual((a) => Math.min(questoes.length - 1, a + 1))}
                className="inline-flex items-center gap-1 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700"
              >
                Próxima <ChevronRight size={16} />
              </button>
            ) : (
              <button
                onClick={finalizar}
                disabled={enviando}
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 disabled:opacity-50"
              >
                <Flag size={15} /> {enviando ? 'Enviando…' : 'Finalizar simulado'}
              </button>
            )}
          </div>
        </div>
      )}

      {/* Botão finalizar sempre disponível */}
      <div className="text-center">
        <button
          onClick={finalizar}
          disabled={enviando}
          className="text-sm text-gray-500 hover:text-red-600 underline disabled:opacity-50"
        >
          Encerrar e finalizar agora
        </button>
      </div>
    </div>
  )
}

/* ── Revisão comentada ──────────────────────────────────────── */

function Revisao({
  revisao,
  respostas,
}: {
  revisao: RevisaoQuestao[]
  respostas: Record<number, string>
}) {
  return (
    <div className="space-y-4">
      <h2 className="text-base font-semibold text-gray-700">Gabarito comentado</h2>
      {revisao.map((q, idx) => {
        const minhaResposta = respostas[idx]
        const correta = q.alternativas.find((a) => a.correta)
        const acertou = minhaResposta && correta && minhaResposta === correta.id
        return (
          <div key={q.questaoId} className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-gray-400">Questão {q.ordem}</span>
              <span className={`inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full font-medium ${
                acertou ? 'bg-brand-50 text-brand-700' : 'bg-red-100 text-red-700'
              }`}>
                {acertou ? <><Check size={13} /> Você acertou</> : <><X size={13} /> Você errou</>}
              </span>
            </div>
            <div className="text-sm text-gray-800 leading-relaxed"><MarkdownText text={q.enunciado} /></div>
            <div className="space-y-2">
              {[...q.alternativas].sort((a, b) => a.ordem - b.ordem).map((alt, i) => {
                const isMinha = minhaResposta === alt.id
                return (
                  <div
                    key={alt.id}
                    className={`flex gap-2 items-center p-2.5 rounded-lg text-sm border ${
                      alt.correta
                        ? 'bg-brand-50 border-brand-300'
                        : isMinha
                          ? 'bg-red-50 border-red-300'
                          : 'bg-gray-50 border-gray-100'
                    }`}
                  >
                    <span className={`font-bold min-w-[1.2rem] ${alt.correta ? 'text-brand-600' : isMinha ? 'text-red-500' : 'text-gray-400'}`}>
                      {LETRAS[i] ?? i + 1})
                    </span>
                    <span className="text-gray-700">{alt.texto}</span>
                    {alt.correta && <span className="ml-auto inline-flex items-center gap-1 text-brand-600 text-xs font-medium"><Check size={13} /> Correta</span>}
                    {isMinha && !alt.correta && <span className="ml-auto text-red-500 text-xs font-medium">Sua resposta</span>}
                  </div>
                )
              })}
            </div>
            {q.explicacao && (
              <div className="bg-gold-400/10 border border-gold-400/30 rounded-lg p-3 text-sm text-gray-700">
                <span className="inline-flex items-center gap-1.5 font-semibold text-gold-600"><Lightbulb size={15} /> Explicação:</span>{' '}
                {q.explicacao}
              </div>
            )}
          </div>
        )
      })}
    </div>
  )
}

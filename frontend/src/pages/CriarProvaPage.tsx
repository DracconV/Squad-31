import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {
  api,
  criarSimulado,
  criarSimuladoAleatorio,
  adicionarQuestaoSimulado,
  listarMinhasTurmas,
  listarTurmas,
  type Turma,
  type Questao,
} from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { FilePlus, Shuffle, PencilLine, Check } from 'lucide-react'
import { Card } from '../components/Card'

interface Disciplina {
  id: string
  nome: string
}

interface PageQuestao {
  content: Questao[]
}

export default function CriarProvaPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const isProfessor = user?.perfil === 'PROFESSOR'

  const [titulo, setTitulo] = useState('')
  const [turmaId, setTurmaId] = useState('')
  const [tempoMinutos, setTempoMinutos] = useState(60)
  const [pontuado, setPontuado] = useState(true)
  const [modo, setModo] = useState<'aleatorio' | 'manual'>('aleatorio')
  const [quantidade, setQuantidade] = useState(10)
  const [disciplinaId, setDisciplinaId] = useState('')
  const [dificuldade, setDificuldade] = useState('')
  const [selecionadas, setSelecionadas] = useState<Set<string>>(new Set())
  const [erro, setErro] = useState('')

  const { data: turmas = [] } = useQuery<Turma[]>({
    queryKey: ['turmas', isProfessor],
    queryFn: isProfessor ? listarMinhasTurmas : () => listarTurmas(),
  })

  const { data: disciplinas = [] } = useQuery({
    queryKey: ['disciplinas'],
    queryFn: async () => (await api.get<Disciplina[]>('/disciplinas')).data,
  })

  // Questões para o modo manual (filtradas por disciplina)
  const { data: questoesPage, isLoading: loadingQuestoes } = useQuery({
    queryKey: ['questoes-picker', disciplinaId],
    queryFn: async () =>
      (await api.get<PageQuestao>('/questoes', {
        params: { disciplinaId: disciplinaId || undefined, size: 50 },
      })).data,
    enabled: modo === 'manual',
  })
  const questoes = questoesPage?.content ?? []

  function toggleQuestao(id: string) {
    setSelecionadas((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  const mutation = useMutation({
    mutationFn: async () => {
      const base = {
        titulo: titulo.trim(),
        turmaId: turmaId || null,
        tempoMinutos,
        pontuado,
      }
      if (modo === 'aleatorio') {
        return criarSimuladoAleatorio({
          ...base,
          quantidade,
          disciplinaId: disciplinaId || undefined,
          dificuldade: dificuldade || undefined,
        })
      }
      // Manual: cria o simulado e adiciona as questões selecionadas (em ordem)
      const sim = await criarSimulado({ ...base, turmaId: turmaId || undefined })
      for (const qid of selecionadas) {
        await adicionarQuestaoSimulado(sim.id, qid)
      }
      return sim
    },
    onSuccess: () => navigate('/provas'),
    onError: (e: unknown) => {
      const msg = (e as { response?: { data?: { mensagem?: string; erro?: string } } })?.response?.data
      setErro(msg?.mensagem ?? msg?.erro ?? 'Erro ao criar a prova.')
    },
  })

  function handleSubmit(ev: React.FormEvent) {
    ev.preventDefault()
    if (!titulo.trim()) return setErro('Informe o título.')
    if (tempoMinutos < 1) return setErro('Tempo deve ser no mínimo 1 minuto.')
    if (modo === 'aleatorio' && quantidade < 1) return setErro('Informe ao menos 1 questão.')
    setErro('')
    mutation.mutate()
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <Card className="p-5 flex items-center gap-3">
        <span className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
          <FilePlus size={22} />
        </span>
        <div>
          <h2 className="font-bold text-gray-800 leading-tight">Criar prova / simulado</h2>
          <p className="text-sm text-gray-500">Monte um simulado para sua turma.</p>
        </div>
      </Card>

      <Card className="p-5"><form onSubmit={handleSubmit} className="space-y-4">
        {erro && <p className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">{erro}</p>}

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Título *</label>
          <input
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Ex: Simulado ENEM — Matemática"
          />
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Turma (opcional)</label>
            <select
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={turmaId}
              onChange={(e) => setTurmaId(e.target.value)}
            >
              <option value="">Sem turma (geral)</option>
              {turmas.map((t) => (
                <option key={t.id} value={t.id}>{t.nome}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tempo (minutos)</label>
            <input
              type="number"
              min={1}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={tempoMinutos}
              onChange={(e) => setTempoMinutos(Number(e.target.value))}
            />
          </div>
        </div>

        <label className="flex items-center gap-2 text-sm text-gray-700">
          <input type="checkbox" checked={pontuado} onChange={(e) => setPontuado(e.target.checked)} />
          Simulado pontuado (conta nota)
        </label>

        {/* Modo de montagem */}
        <div className="flex gap-2 pt-2">
          {(['aleatorio', 'manual'] as const).map((m) => (
            <button
              type="button"
              key={m}
              onClick={() => setModo(m)}
              className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium transition ${
                modo === m ? 'bg-brand-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {m === 'aleatorio' ? <><Shuffle size={15} /> Questões aleatórias</> : <><PencilLine size={15} /> Selecionar manualmente</>}
            </button>
          ))}
        </div>

        {modo === 'aleatorio' && (
          <div className="grid grid-cols-3 gap-3 bg-gray-50 p-3 rounded-lg">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Qtd. de questões *</label>
              <input
                type="number"
                min={1}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={quantidade}
                onChange={(e) => setQuantidade(Number(e.target.value))}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Disciplina</label>
              <select
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={disciplinaId}
                onChange={(e) => setDisciplinaId(e.target.value)}
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
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={dificuldade}
                onChange={(e) => setDificuldade(e.target.value)}
              >
                <option value="">Todas</option>
                <option value="FACIL">Fácil</option>
                <option value="MEDIO">Médio</option>
                <option value="DIFICIL">Difícil</option>
              </select>
            </div>
          </div>
        )}

        {modo === 'manual' && (
          <div className="bg-gray-50 p-3 rounded-lg space-y-3">
            <div className="flex items-center justify-between gap-3 flex-wrap">
              <span className="text-sm font-medium text-gray-700">
                Selecione as questões
                {selecionadas.size > 0 && <span className="ml-2 text-brand-600">({selecionadas.size} selecionada{selecionadas.size > 1 ? 's' : ''})</span>}
              </span>
              <select
                className="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={disciplinaId}
                onChange={(e) => setDisciplinaId(e.target.value)}
              >
                <option value="">Todas as disciplinas</option>
                {disciplinas.map((d) => (
                  <option key={d.id} value={d.id}>{d.nome}</option>
                ))}
              </select>
            </div>

            {loadingQuestoes ? (
              <p className="text-sm text-gray-400 py-4 text-center">Carregando questões…</p>
            ) : questoes.length === 0 ? (
              <p className="text-sm text-gray-400 py-4 text-center">Nenhuma questão encontrada.</p>
            ) : (
              <ul className="max-h-72 overflow-y-auto divide-y divide-gray-100 bg-white rounded-lg border border-gray-100">
                {questoes.map((q) => (
                  <li key={q.id}>
                    <label className="flex items-start gap-3 p-3 hover:bg-gray-50 cursor-pointer">
                      <input
                        type="checkbox"
                        className="mt-1"
                        checked={selecionadas.has(q.id)}
                        onChange={() => toggleQuestao(q.id)}
                      />
                      <span className="min-w-0">
                        <span className="text-sm text-gray-700 line-clamp-2">
                          {q.enunciado.replace(/!\[[^\]]*\]\([^)]+\)/g, '[imagem]')}
                        </span>
                        <span className="block text-xs text-gray-400 mt-1">
                          {q.disciplina} · {q.dificuldade}
                        </span>
                      </span>
                    </label>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        <div className="flex gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/provas')}
            className="flex-1 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm hover:bg-gray-200"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={mutation.isPending}
            className="flex-1 inline-flex items-center justify-center gap-1.5 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 disabled:opacity-50"
          >
            <Check size={16} /> {mutation.isPending ? 'Criando...' : 'Criar prova'}
          </button>
        </div>
      </form></Card>
    </div>
  )
}

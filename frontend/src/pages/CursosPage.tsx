import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '../contexts/AuthContext'
import {
  concluirInscricao,
  inscreverEmCurso,
  listarCursos,
  listarMinhasInscricoes,
  type Curso,
  type Inscricao,
} from '../lib/api'
import { EmptyState } from '../components/EmptyState'
import { Card } from '../components/Card'
import { BookOpen, CheckCircle2, GraduationCap, Award } from 'lucide-react'

/* ── Card de curso ─────────────────────────────────────────── */

interface CursoCardProps {
  curso: Curso
  inscricao?: Inscricao
  onInscrever: (cursoId: string) => void
  onConcluir: (inscricaoId: string) => void
  loadingInscrever: boolean
  loadingConcluir: boolean
}

function CursoCard({
  curso,
  inscricao,
  onInscrever,
  onConcluir,
  loadingInscrever,
  loadingConcluir,
}: CursoCardProps) {
  const concluido  = inscricao?.concluido ?? false
  const inscrito   = !!inscricao
  const dataInsc   = inscricao
    ? new Date(inscricao.dataInscricao).toLocaleDateString('pt-BR')
    : null

  return (
    <Card hover className="p-5 flex flex-col gap-3">
      {/* Cabeçalho */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-start gap-3 min-w-0">
          <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
            <BookOpen size={20} />
          </span>
          <h3 className="font-semibold text-gray-800 leading-snug pt-1.5">{curso.nome}</h3>
        </div>
        {concluido && (
          <span className="shrink-0 inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full bg-brand-50 text-brand-700">
            <CheckCircle2 size={13} /> Concluído
          </span>
        )}
        {inscrito && !concluido && (
          <span className="shrink-0 text-xs font-medium px-2 py-1 rounded-full bg-gold-400/20 text-gold-600">
            Em andamento
          </span>
        )}
      </div>

      {/* Descrição */}
      {curso.descricao && (
        <p className="text-sm text-gray-500 leading-relaxed">{curso.descricao}</p>
      )}

      {/* Data de inscrição */}
      {dataInsc && (
        <p className="text-xs text-gray-400">Inscrito em {dataInsc}</p>
      )}

      {/* Ações */}
      <div className="mt-auto pt-2">
        {!inscrito && (
          <button
            onClick={() => onInscrever(curso.id)}
            disabled={loadingInscrever}
            className="w-full py-2 px-4 rounded-lg bg-brand-600 text-white text-sm font-medium
                       hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loadingInscrever ? 'Inscrevendo...' : 'Inscrever-se'}
          </button>
        )}

        {inscrito && !concluido && (
          <button
            onClick={() => onConcluir(inscricao!.id)}
            disabled={loadingConcluir}
            className="w-full py-2 px-4 rounded-lg bg-green-600 text-white text-sm font-medium
                       hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loadingConcluir ? 'Concluindo...' : 'Marcar como concluído'}
          </button>
        )}

        {concluido && (
          <div className="flex items-center justify-center gap-1.5 text-sm text-brand-600 font-medium py-2">
            <GraduationCap size={16} /> Certificado em <a href="/certificados" className="underline">Certificados</a>
          </div>
        )}
      </div>
    </Card>
  )
}

/* ── Skeleton ──────────────────────────────────────────────── */

function CursoSkeleton() {
  return (
    <div className="bg-white rounded-xl p-5 border border-gray-100 animate-pulse">
      <div className="h-5 bg-gray-200 rounded w-3/4 mb-3" />
      <div className="h-4 bg-gray-100 rounded w-full mb-2" />
      <div className="h-4 bg-gray-100 rounded w-2/3 mb-4" />
      <div className="h-9 bg-gray-200 rounded-lg" />
    </div>
  )
}

/* ── Página principal ──────────────────────────────────────── */

export default function CursosPage() {
  const { user } = useAuth()
  const queryClient = useQueryClient()

  const { data: cursos = [], isLoading: loadingCursos } = useQuery<Curso[]>({
    queryKey: ['cursos'],
    queryFn: listarCursos,
  })

  const { data: inscricoes = [], isLoading: loadingInscricoes } = useQuery<Inscricao[]>({
    queryKey: ['inscricoes-minhas'],
    queryFn: listarMinhasInscricoes,
    enabled: !!user,
  })

  const { mutate: inscrever, isPending: inscrevendo, variables: inscrevendoId } =
    useMutation({
      mutationFn: inscreverEmCurso,
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['inscricoes-minhas'] })
        queryClient.invalidateQueries({ queryKey: ['stats'] })
      },
    })

  const { mutate: concluir, isPending: concluindo, variables: concluindoId } =
    useMutation({
      mutationFn: concluirInscricao,
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['inscricoes-minhas'] })
        queryClient.invalidateQueries({ queryKey: ['certificado'] })
        queryClient.invalidateQueries({ queryKey: ['stats'] })
      },
    })

  /* mapa cursoId → inscrição */
  const inscricaoMap = Object.fromEntries(
    inscricoes.map((i) => [i.cursoId, i]),
  )

  const isLoading = loadingCursos || loadingInscricoes

  return (
    <div className="space-y-6">
      {/* Cabeçalho */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-800">Cursos disponíveis</h2>
          <p className="text-sm text-gray-500">
            Inscreva-se em um curso e acompanhe seu progresso.
          </p>
        </div>
        {!isLoading && (
          <span className="text-sm text-gray-400">
            {cursos.length} {cursos.length === 1 ? 'curso' : 'cursos'}
          </span>
        )}
      </div>

      {/* Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => <CursoSkeleton key={i} />)}
        </div>
      ) : cursos.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<Award size={30} strokeWidth={1.75} />}
            title="Nenhum curso disponível"
            description="Novos cursos serão adicionados em breve."
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {cursos.map((curso) => (
            <CursoCard
              key={curso.id}
              curso={curso}
              inscricao={inscricaoMap[curso.id]}
              onInscrever={(id) => inscrever(id)}
              onConcluir={(id) => concluir(id)}
              loadingInscrever={inscrevendo && inscrevendoId === curso.id}
              loadingConcluir={concluindo && concluindoId === inscricaoMap[curso.id]?.id}
            />
          ))}
        </div>
      )}
    </div>
  )
}

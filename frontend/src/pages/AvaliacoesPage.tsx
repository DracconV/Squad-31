import { useQuery } from '@tanstack/react-query'
import { ClipboardCheck, FileText, Users, Star } from 'lucide-react'
import { listarAvaliacoes, type Avaliacao } from '../lib/api'
import { Card } from '../components/Card'
import { StatCard } from '../components/StatCard'
import { EmptyState } from '../components/EmptyState'

function corTaxa(taxa: number): string {
  if (taxa >= 70) return 'text-brand-600'
  if (taxa >= 50) return 'text-amber-600'
  return 'text-red-500'
}

export default function AvaliacoesPage() {
  const { data: avaliacoes = [], isLoading, isError } = useQuery<Avaliacao[]>({
    queryKey: ['avaliacoes'],
    queryFn: listarAvaliacoes,
    retry: false,
  })

  const totalTentativas = avaliacoes.reduce((s, a) => s + a.total_tentativas, 0)
  const comTentativas = avaliacoes.filter((a) => a.total_tentativas > 0)
  const mediaGeral = comTentativas.length
    ? comTentativas.reduce((s, a) => s + Number(a.nota_media), 0) / comTentativas.length
    : 0

  return (
    <div className="space-y-6">
      <Card className="p-5 flex items-center gap-3">
        <span className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
          <ClipboardCheck size={22} />
        </span>
        <div>
          <h2 className="font-bold text-gray-800 leading-tight">Avaliações</h2>
          <p className="text-sm text-gray-500">Desempenho dos simulados da rede.</p>
        </div>
      </Card>

      {/* KPIs */}
      <div className="grid grid-cols-2 lg:grid-cols-3 gap-4">
        <StatCard title="Avaliações" value={avaliacoes.length} icon={<FileText size={18} />} />
        <StatCard title="Tentativas totais" value={totalTentativas} icon={<Users size={18} />} />
        <StatCard title="Nota média geral" value={mediaGeral.toFixed(2)} icon={<Star size={18} />} />
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[1, 2, 3].map((i) => <div key={i} className="h-12 bg-white rounded-2xl border animate-pulse" />)}
        </div>
      ) : isError ? (
        <p className="text-sm text-red-500 bg-red-50 p-3 rounded-lg">
          Erro ao carregar avaliações. Verifique se o ms-relatorios está no ar.
        </p>
      ) : avaliacoes.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<ClipboardCheck size={30} strokeWidth={1.75} />}
            title="Nenhuma avaliação ainda"
            description="Quando houver simulados com tentativas, o desempenho aparecerá aqui."
          />
        </Card>
      ) : (
        <Card className="overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-500 text-left">
              <tr>
                <th className="px-5 py-3 font-medium">Simulado</th>
                <th className="px-5 py-3 font-medium">Tipo</th>
                <th className="px-5 py-3 font-medium">Tentativas</th>
                <th className="px-5 py-3 font-medium">Nota média</th>
                <th className="px-5 py-3 font-medium">Taxa de acerto</th>
                <th className="px-5 py-3 font-medium">Criado em</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {avaliacoes.map((a) => (
                <tr key={a.simulado_id} className="hover:bg-gray-50">
                  <td className="px-5 py-3 font-medium text-gray-800">{a.titulo}</td>
                  <td className="px-5 py-3">
                    <span className={`text-xs px-2 py-0.5 rounded-full ${a.pontuado ? 'bg-brand-50 text-brand-700' : 'bg-gray-100 text-gray-500'}`}>
                      {a.pontuado ? 'Pontuado' : 'Treino'}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-700">{a.total_tentativas}</td>
                  <td className="px-5 py-3 font-semibold text-gray-800">{Number(a.nota_media).toFixed(2)}</td>
                  <td className="px-5 py-3">
                    <span className={`font-semibold ${corTaxa(a.taxa_acerto)}`}>
                      {a.total_tentativas > 0 ? `${a.taxa_acerto.toFixed(1)}%` : '—'}
                    </span>
                  </td>
                  <td className="px-5 py-3 text-gray-400 text-xs">
                    {new Date(a.criado_em).toLocaleDateString('pt-BR')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  )
}

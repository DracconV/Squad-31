import { useQuery } from '@tanstack/react-query'
import { History, Clock, Calendar } from 'lucide-react'
import { listarMinhasTentativas, type TentativaSimulado } from '../lib/api'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

function formatDuracao(segundos: number) {
  const m = Math.floor(segundos / 60)
  const s = segundos % 60
  return `${m}m ${s}s`
}

export default function HistoricoPage() {
  const { data: tentativas = [], isLoading } = useQuery({
    queryKey: ['tentativas'],
    queryFn: listarMinhasTentativas,
  })

  return (
    <div className="space-y-6">
      <Card className="p-5 flex items-center gap-3">
        <span className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
          <History size={22} />
        </span>
        <div>
          <h2 className="font-bold text-gray-800 leading-tight">Histórico de Simulados</h2>
          <p className="text-sm text-gray-500">Suas tentativas anteriores e notas.</p>
        </div>
      </Card>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3].map((i) => <div key={i} className="h-20 bg-white rounded-2xl border animate-pulse" />)}
        </div>
      ) : tentativas.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<History size={30} strokeWidth={1.75} />}
            title="Nenhum simulado realizado"
            description="Acesse a seção Simulados e faça sua primeira prova!"
          />
        </Card>
      ) : (
        <div className="space-y-3">
          {tentativas.map((t: TentativaSimulado) => {
            const nota = Number(t.nota)
            const cor = nota >= 7 ? 'text-brand-600' : nota >= 5 ? 'text-amber-600' : 'text-red-500'
            const ring = nota >= 7 ? 'ring-brand-100' : nota >= 5 ? 'ring-amber-100' : 'ring-red-100'

            return (
              <Card key={t.id} hover className={`p-5 flex items-center justify-between gap-4 ring-1 ${ring}`}>
                <div className="min-w-0">
                  <p className="text-xs text-gray-400 font-mono">{t.simuladoId.slice(0, 8)}…</p>
                  <p className="flex flex-wrap items-center gap-x-3 gap-y-0.5 text-sm text-gray-600 mt-1">
                    <span className="inline-flex items-center gap-1.5">
                      <Calendar size={14} />
                      {new Date(t.iniciadoEm).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </span>
                    {t.finalizadoEm && (
                      <span className="inline-flex items-center gap-1.5"><Clock size={14} /> {formatDuracao(t.tempoGastoSegundos)}</span>
                    )}
                  </p>
                </div>
                <div className="text-right shrink-0">
                  <p className={`text-3xl font-bold ${cor}`}>{nota.toFixed(1)}</p>
                  <p className="text-xs text-gray-400">/ 10</p>
                </div>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}

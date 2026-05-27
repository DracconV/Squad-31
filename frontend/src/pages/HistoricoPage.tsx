import { useQuery } from '@tanstack/react-query'
import { listarMinhasTentativas, type TentativaSimulado } from '../lib/api'

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
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <h1 className="text-xl font-bold text-gray-800">Histórico de Simulados</h1>
        <p className="text-sm text-gray-500 mt-0.5">Suas tentativas anteriores</p>
      </div>

      {isLoading ? (
        <div className="space-y-3">
          {[1,2,3].map((i) => <div key={i} className="h-20 bg-white rounded-xl border animate-pulse" />)}
        </div>
      ) : tentativas.length === 0 ? (
        <div className="bg-white rounded-xl p-12 text-center border border-gray-100">
          <p className="text-4xl mb-3">📝</p>
          <p className="text-gray-600 font-medium">Nenhum simulado realizado ainda.</p>
          <p className="text-sm text-gray-400 mt-1">Acesse a seção Simulados e faça sua primeira prova!</p>
        </div>
      ) : (
        <div className="space-y-3">
          {tentativas.map((t: TentativaSimulado) => {
            const nota = Number(t.nota)
            const cor = nota >= 7 ? 'text-green-600' : nota >= 5 ? 'text-yellow-600' : 'text-red-500'
            const bg = nota >= 7 ? 'bg-green-50 border-green-100' : nota >= 5 ? 'bg-yellow-50 border-yellow-100' : 'bg-red-50 border-red-100'

            return (
              <div key={t.id} className={`rounded-xl p-5 border ${bg} flex items-center justify-between gap-4`}>
                <div>
                  <p className="text-xs text-gray-400 font-mono">{t.simuladoId.slice(0, 8)}…</p>
                  <p className="text-sm text-gray-600 mt-0.5">
                    {new Date(t.iniciadoEm).toLocaleDateString('pt-BR', {
                      day: '2-digit', month: 'short', year: 'numeric',
                    })}
                    {t.finalizadoEm && ` · ${formatDuracao(t.tempoGastoSegundos)}`}
                  </p>
                </div>
                <div className="text-right shrink-0">
                  <p className={`text-3xl font-bold ${cor}`}>{nota.toFixed(1)}</p>
                  <p className="text-xs text-gray-400">/ 10</p>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

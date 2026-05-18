import { useParams } from 'react-router-dom'
import { EmptyState } from '../components/EmptyState'

// TODO: implementar SimuladoPage (detalhe/execução de simulado)
export default function SimuladoPage() {
  const { id } = useParams()
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <p className="text-sm text-gray-400 mb-4">Simulado #{id}</p>
        <EmptyState
          title="Em desenvolvimento"
          description="A execução do simulado será implementada em breve."
        />
      </div>
    </div>
  )
}

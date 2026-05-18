import { EmptyState } from '../components/EmptyState'

// TODO: integrar com ms-certificados — GET /certificados/:aluno/:curso
export default function CertificadosPage() {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <EmptyState
          title="Nenhum certificado ainda"
          description="Seus certificados aparecerão aqui após a conclusão dos cursos."
        />
      </div>
    </div>
  )
}

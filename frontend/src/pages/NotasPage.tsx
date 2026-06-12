import { EmptyState } from '../components/EmptyState'

// TODO: implementar NotasPage
export default function NotasPage() {
  return (
    <div className="space-y-6">
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <EmptyState
          title="Em desenvolvimento"
          description="Esta seção será implementada em breve."
        />
      </div>
    </div>
  )
}

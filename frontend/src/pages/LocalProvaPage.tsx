import { MapPin } from 'lucide-react'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

export default function LocalProvaPage() {
  return (
    <div className="space-y-6">
      <Card className="p-4">
        <EmptyState
          icon={<MapPin size={30} strokeWidth={1.75} />}
          title="Local de prova em breve"
          description="Aqui você poderá consultar o endereço, a sala e o horário das suas provas presenciais assim que forem agendadas pela sua escola."
        />
      </Card>
    </div>
  )
}

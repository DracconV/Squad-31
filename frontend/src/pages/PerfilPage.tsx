import { User, BadgeCheck, Hash, Shield } from 'lucide-react'
import { useAuth } from '../contexts/AuthContext'
import { Card } from '../components/Card'

const PERFIL_LABEL: Record<string, string> = {
  ALUNO_EM: 'Aluno · Ensino Médio',
  ALUNO_EJA: 'Aluno · EJA',
  ALUNO_PROF: 'Aluno · Profissionalizante',
  PROFESSOR: 'Professor',
  ADMIN_ESCOLA: 'Administrador da Escola',
  ADMIN_SEED: 'Administrador SEED',
}

export default function PerfilPage() {
  const { user } = useAuth()
  const inicial = user?.nome?.charAt(0).toUpperCase() ?? 'U'

  return (
    <div className="max-w-lg space-y-6">
      <Card className="overflow-hidden">
        {/* Banner */}
        <div className="h-24 bg-gradient-to-br from-brand-600 to-brand-800" />
        <div className="px-6 pb-6">
          <div className="flex items-end gap-4 -mt-10">
            <div className="w-20 h-20 rounded-2xl bg-gold-400 text-brand-900 flex items-center justify-center text-3xl font-extrabold shadow-lg ring-4 ring-white">
              {inicial}
            </div>
            <div className="pb-1">
              <p className="font-bold text-lg text-gray-800 leading-tight">{user?.nome}</p>
              <p className="inline-flex items-center gap-1 text-sm text-brand-700 font-medium">
                <BadgeCheck size={14} /> {PERFIL_LABEL[user?.perfil ?? ''] ?? user?.perfil}
              </p>
            </div>
          </div>
        </div>
      </Card>

      <Card className="p-6">
        <h2 className="font-semibold text-gray-800 mb-4">Informações da conta</h2>
        <div className="space-y-1 text-sm">
          <InfoRow icon={<User size={15} />} label="Nome" value={user?.nome ?? '—'} />
          <InfoRow icon={<Shield size={15} />} label="Perfil" value={PERFIL_LABEL[user?.perfil ?? ''] ?? user?.perfil ?? '—'} />
          <InfoRow icon={<Hash size={15} />} label="ID" value={user?.id ?? '—'} mono last />
        </div>
      </Card>
    </div>
  )
}

function InfoRow({
  icon, label, value, mono = false, last = false,
}: { icon: React.ReactNode; label: string; value: string; mono?: boolean; last?: boolean }) {
  return (
    <div className={`flex items-center justify-between gap-4 py-2.5 ${last ? '' : 'border-b border-gray-50'}`}>
      <span className="inline-flex items-center gap-2 text-gray-500">
        <span className="text-brand-500">{icon}</span> {label}
      </span>
      <span className={`text-gray-700 text-right truncate ${mono ? 'font-mono text-xs' : ''}`}>{value}</span>
    </div>
  )
}

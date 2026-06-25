import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { MapPin, Calendar, Users, Check, X, CalendarCheck } from 'lucide-react'
import {
  listarSlotsProva, meusAgendamentos, agendarProva, cancelarAgendamento,
  type SlotProva, type Agendamento,
} from '../lib/api'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

function fmt(data: string | null) {
  if (!data) return '—'
  return new Date(data).toLocaleString('pt-BR', {
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

export default function LocalProvaPage() {
  const qc = useQueryClient()

  const { data: agendamentos = [], isLoading: loadingAg } = useQuery<Agendamento[]>({
    queryKey: ['meus-agendamentos'],
    queryFn: meusAgendamentos,
    retry: false,
  })

  const { data: slots = [], isLoading: loadingSlots } = useQuery<SlotProva[]>({
    queryKey: ['slots-prova'],
    queryFn: listarSlotsProva,
    retry: false,
  })

  const agendar = useMutation({
    mutationFn: agendarProva,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['meus-agendamentos'] })
      qc.invalidateQueries({ queryKey: ['slots-prova'] })
    },
    onError: () => alert('Não foi possível agendar. O horário pode ter esgotado as vagas.'),
  })

  const cancelar = useMutation({
    mutationFn: cancelarAgendamento,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['meus-agendamentos'] })
      qc.invalidateQueries({ queryKey: ['slots-prova'] })
    },
  })

  const jaAgendados = new Set(agendamentos.map((a) => a.slotId))

  return (
    <div className="space-y-6">
      <Card className="p-5 flex items-center gap-3">
        <span className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
          <MapPin size={22} />
        </span>
        <div>
          <h2 className="font-bold text-gray-800 leading-tight">Local de prova</h2>
          <p className="text-sm text-gray-500">Agende sua prova prática e consulte o local e horário.</p>
        </div>
      </Card>

      {/* Meus agendamentos */}
      <section className="space-y-3">
        <h3 className="text-sm font-semibold text-gray-700 flex items-center gap-2">
          <CalendarCheck size={16} className="text-brand-600" /> Meus agendamentos
        </h3>
        {loadingAg ? (
          <div className="h-20 bg-white rounded-2xl border animate-pulse" />
        ) : agendamentos.length === 0 ? (
          <Card className="p-4">
            <EmptyState
              icon={<CalendarCheck size={28} strokeWidth={1.75} />}
              title="Nenhuma prova agendada"
              description="Escolha um horário disponível abaixo para agendar sua prova prática."
            />
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {agendamentos.map((a) => (
              <Card key={a.id} className="p-5 flex items-start justify-between gap-3 ring-1 ring-brand-100">
                <div className="min-w-0">
                  <p className="inline-flex items-center gap-1.5 font-semibold text-gray-800">
                    <MapPin size={15} className="text-brand-600" /> {a.local ?? 'Local a definir'}
                  </p>
                  <p className="inline-flex items-center gap-1.5 text-sm text-gray-500 mt-1">
                    <Calendar size={14} /> {fmt(a.dataProva)}
                  </p>
                </div>
                <button
                  onClick={() => { if (confirm('Cancelar este agendamento?')) cancelar.mutate(a.id) }}
                  disabled={cancelar.isPending}
                  className="inline-flex items-center gap-1 text-red-500 hover:underline text-xs disabled:opacity-40 shrink-0"
                >
                  <X size={13} /> Cancelar
                </button>
              </Card>
            ))}
          </div>
        )}
      </section>

      {/* Horários disponíveis */}
      <section className="space-y-3">
        <h3 className="text-sm font-semibold text-gray-700 flex items-center gap-2">
          <Calendar size={16} className="text-brand-600" /> Horários disponíveis
        </h3>
        {loadingSlots ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => <div key={i} className="h-32 bg-white rounded-2xl border animate-pulse" />)}
          </div>
        ) : slots.length === 0 ? (
          <Card className="p-4">
            <EmptyState
              icon={<Calendar size={28} strokeWidth={1.75} />}
              title="Nenhum horário disponível"
              description="Ainda não há horários abertos para agendamento. Volte mais tarde."
            />
          </Card>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {slots.map((s) => {
              const lotado = s.vagasDisponiveis <= 0
              const agendado = jaAgendados.has(s.id)
              return (
                <Card key={s.id} hover className="p-5 flex flex-col gap-3">
                  <p className="inline-flex items-center gap-1.5 font-semibold text-gray-800">
                    <MapPin size={15} className="text-brand-600" /> {s.local}
                  </p>
                  <div className="flex flex-col gap-1 text-sm text-gray-500">
                    <span className="inline-flex items-center gap-1.5"><Calendar size={14} /> {fmt(s.data)}</span>
                    <span className="inline-flex items-center gap-1.5">
                      <Users size={14} /> {s.vagasDisponiveis} de {s.vagasTotais} vaga{s.vagasTotais > 1 ? 's' : ''}
                    </span>
                  </div>
                  <button
                    onClick={() => agendar.mutate(s.id)}
                    disabled={lotado || agendado || agendar.isPending}
                    className="mt-auto inline-flex items-center justify-center gap-1.5 py-2 rounded-lg text-sm font-medium transition disabled:cursor-not-allowed
                      bg-brand-600 text-white hover:bg-brand-700 disabled:bg-gray-100 disabled:text-gray-400"
                  >
                    {agendado ? <><Check size={15} /> Agendado</> : lotado ? 'Esgotado' : 'Agendar'}
                  </button>
                </Card>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}

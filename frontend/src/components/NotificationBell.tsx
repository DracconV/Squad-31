import { useState, useRef, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Bell, Check } from 'lucide-react'
import {
  listarNotificacoesNaoLidas,
  marcarNotificacaoLida,
  type Notificacao,
} from '../lib/api'

export default function NotificationBell() {
  const qc = useQueryClient()
  const [aberto, setAberto] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  // Não-lidas; atualiza a cada 30s. retry:false p/ não quebrar se o ms estiver fora.
  const { data: naoLidas = [] } = useQuery<Notificacao[]>({
    queryKey: ['notificacoes-nao-lidas'],
    queryFn: listarNotificacoesNaoLidas,
    refetchInterval: 30_000,
    retry: false,
  })

  const marcarLida = useMutation({
    mutationFn: marcarNotificacaoLida,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notificacoes-nao-lidas'] }),
  })

  // Fecha o dropdown ao clicar fora
  useEffect(() => {
    function onClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setAberto(false)
    }
    document.addEventListener('mousedown', onClick)
    return () => document.removeEventListener('mousedown', onClick)
  }, [])

  const count = naoLidas.length

  return (
    <div className="relative" ref={ref}>
      <button
        onClick={() => setAberto((v) => !v)}
        aria-label={`Notificações${count > 0 ? ` (${count} não lidas)` : ''}`}
        className="relative p-2 rounded-lg hover:bg-gray-100 transition text-gray-600"
      >
        <Bell size={20} aria-hidden="true" />
        {count > 0 && (
          <span className="absolute top-0 right-0 min-w-[18px] h-[18px] px-1 rounded-full bg-gold-500 text-brand-900 text-[10px] font-bold flex items-center justify-center ring-2 ring-white">
            {count > 9 ? '9+' : count}
          </span>
        )}
      </button>

      {aberto && (
        <div className="absolute right-0 mt-2 w-80 max-h-96 overflow-y-auto bg-white rounded-xl shadow-xl border border-gray-100 z-50">
          <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between sticky top-0 bg-white">
            <span className="font-semibold text-gray-800 text-sm">Notificações</span>
            <span className="text-xs text-gray-400">{count} não lidas</span>
          </div>

          {count === 0 ? (
            <p className="px-4 py-8 text-center text-sm text-gray-400">Nenhuma notificação nova ✓</p>
          ) : (
            <ul className="divide-y divide-gray-50">
              {naoLidas.map((n) => (
                <li key={n.id} className="px-4 py-3 hover:bg-gray-50">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <p className="text-sm font-medium text-gray-800 truncate">{n.title}</p>
                      <p className="text-xs text-gray-500 mt-0.5 line-clamp-2">{n.message}</p>
                      <p className="text-[11px] text-gray-400 mt-1">
                        {new Date(n.createdAt).toLocaleString('pt-BR')}
                      </p>
                    </div>
                    <button
                      onClick={() => marcarLida.mutate(n.id)}
                      disabled={marcarLida.isPending}
                      title="Marcar como lida"
                      className="shrink-0 text-brand-600 hover:text-brand-800 disabled:opacity-40"
                    >
                      <Check size={16} />
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}

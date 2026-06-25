import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Building2, Search, MapPin } from 'lucide-react'
import { listarInstituicoes, type Instituicao } from '../lib/api'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

export default function EscolasPage() {
  const [busca, setBusca] = useState('')

  const { data: escolas = [], isLoading, isError } = useQuery<Instituicao[]>({
    queryKey: ['instituicoes'],
    queryFn: listarInstituicoes,
    retry: false,
  })

  const filtradas = escolas.filter(
    (e) =>
      busca === '' ||
      e.nome.toLowerCase().includes(busca.toLowerCase()) ||
      e.municipio.toLowerCase().includes(busca.toLowerCase()),
  )

  const municipios = new Set(escolas.map((e) => e.municipio)).size

  return (
    <div className="space-y-6">
      <Card className="p-5 flex items-center justify-between gap-4 flex-wrap">
        <div className="flex items-center gap-3">
          <span className="inline-flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
            <Building2 size={22} />
          </span>
          <div>
            <p className="text-2xl font-bold text-gray-800 leading-none">{escolas.length}</p>
            <p className="text-sm text-gray-500 mt-1">
              escolas {municipios > 0 && `em ${municipios} município${municipios > 1 ? 's' : ''}`}
            </p>
          </div>
        </div>
        <div className="relative">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
          <input
            aria-label="Buscar escola por nome ou município"
            className="border border-gray-300 rounded-lg pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 w-64"
            placeholder="Buscar por nome ou município..."
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
          />
        </div>
      </Card>

      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => <div key={i} className="h-28 bg-white rounded-2xl border animate-pulse" />)}
        </div>
      ) : isError ? (
        <p className="text-sm text-red-500 bg-red-50 p-3 rounded-lg">
          Erro ao carregar escolas. Verifique se o serviço de autenticação está no ar.
        </p>
      ) : filtradas.length === 0 ? (
        <Card className="p-4">
          <EmptyState
            icon={<Building2 size={30} strokeWidth={1.75} />}
            title="Nenhuma escola encontrada"
            description={busca ? 'Tente outro termo de busca.' : 'Ainda não há escolas cadastradas na rede.'}
          />
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {filtradas.map((e) => (
            <Card key={e.id} hover className="p-5 flex flex-col gap-3">
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-start gap-3 min-w-0">
                  <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
                    <Building2 size={20} />
                  </span>
                  <div className="min-w-0">
                    <h3 className="font-semibold text-gray-800 leading-snug truncate">{e.nome}</h3>
                    <p className="inline-flex items-center gap-1 text-xs text-gray-500 mt-0.5">
                      <MapPin size={12} /> {e.municipio}
                    </p>
                  </div>
                </div>
                <span className={`shrink-0 text-xs font-medium px-2 py-1 rounded-full ${
                  e.ativo ? 'bg-brand-50 text-brand-700' : 'bg-gray-100 text-gray-500'
                }`}>
                  {e.ativo ? 'Ativa' : 'Inativa'}
                </span>
              </div>
              <p className="text-xs text-gray-400 font-mono">INEP {e.codigoInep || '—'}</p>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

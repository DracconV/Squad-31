import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Building2, Search, MapPin, Plus, Pencil, X } from 'lucide-react'
import {
  listarInstituicoes, criarInstituicao, atualizarInstituicao,
  type Instituicao, type InstituicaoInput,
} from '../lib/api'
import { useAuth } from '../contexts/AuthContext'
import { Card } from '../components/Card'
import { EmptyState } from '../components/EmptyState'

function EscolaModal({ escola, onClose }: { escola: Instituicao | null; onClose: () => void }) {
  const qc = useQueryClient()
  const editando = !!escola
  const [form, setForm] = useState({
    nome: escola?.nome ?? '',
    municipio: escola?.municipio ?? '',
    codigoInep: escola?.codigoInep ?? '',
    ativo: escola?.ativo ?? true,
  })
  const [erro, setErro] = useState('')

  const mutation = useMutation({
    mutationFn: () => {
      const payload: InstituicaoInput = {
        nome: form.nome.trim(),
        municipio: form.municipio.trim(),
        codigoInep: form.codigoInep.trim(),
        ativo: form.ativo,
      }
      return editando ? atualizarInstituicao(escola!.id, payload) : criarInstituicao(payload)
    },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['instituicoes'] }); onClose() },
    onError: (e: unknown) => {
      const msg = (e as { response?: { data?: { mensagem?: string } } })?.response?.data?.mensagem
      setErro(msg ?? 'Erro ao salvar a escola.')
    },
  })

  function handleSubmit(ev: React.FormEvent) {
    ev.preventDefault()
    if (!form.nome.trim() || !form.municipio.trim() || !form.codigoInep.trim()) {
      return setErro('Nome, município e código INEP são obrigatórios.')
    }
    setErro('')
    mutation.mutate()
  }

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-bold text-gray-800">{editando ? 'Editar escola' : 'Nova escola'}</h2>
          <button onClick={onClose} aria-label="Fechar" className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {erro && <p className="text-sm text-red-600 bg-red-50 p-3 rounded-lg">{erro}</p>}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nome *</label>
            <input
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
              value={form.nome} onChange={(e) => setForm((f) => ({ ...f, nome: e.target.value }))}
              placeholder="Ex: EEEM Governador..." />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Município *</label>
              <input
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={form.municipio} onChange={(e) => setForm((f) => ({ ...f, municipio: e.target.value }))}
                placeholder="Aracaju" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Código INEP *</label>
              <input
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500"
                value={form.codigoInep} onChange={(e) => setForm((f) => ({ ...f, codigoInep: e.target.value }))}
                placeholder="28xxxxxx" />
            </div>
          </div>
          {editando && (
            <label className="flex items-center gap-2 text-sm text-gray-700">
              <input type="checkbox" checked={form.ativo} onChange={(e) => setForm((f) => ({ ...f, ativo: e.target.checked }))} />
              Escola ativa
            </label>
          )}
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 rounded-lg bg-gray-100 text-gray-700 text-sm hover:bg-gray-200">Cancelar</button>
            <button type="submit" disabled={mutation.isPending}
              className="flex-1 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 disabled:opacity-50">
              {mutation.isPending ? 'Salvando...' : editando ? 'Salvar' : 'Criar escola'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function EscolasPage() {
  const { user } = useAuth()
  const isAdminSeed = user?.perfil === 'ADMIN_SEED'
  const [busca, setBusca] = useState('')
  const [modal, setModal] = useState<{ open: boolean; escola: Instituicao | null }>({ open: false, escola: null })

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
      {modal.open && <EscolaModal escola={modal.escola} onClose={() => setModal({ open: false, escola: null })} />}

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
        <div className="flex items-center gap-2 flex-wrap">
          <div className="relative">
            <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
            <input
              aria-label="Buscar escola por nome ou município"
              className="border border-gray-300 rounded-lg pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-500 w-60"
              placeholder="Buscar por nome ou município..."
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
            />
          </div>
          {isAdminSeed && (
            <button
              onClick={() => setModal({ open: true, escola: null })}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-brand-600 text-white text-sm font-medium hover:bg-brand-700 transition shrink-0"
            >
              <Plus size={16} /> Nova escola
            </button>
          )}
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
            action={isAdminSeed && !busca ? { label: 'Nova escola', onClick: () => setModal({ open: true, escola: null }) } : undefined}
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
              <div className="flex items-center justify-between">
                <p className="text-xs text-gray-400 font-mono">INEP {e.codigoInep || '—'}</p>
                {isAdminSeed && (
                  <button
                    onClick={() => setModal({ open: true, escola: e })}
                    className="inline-flex items-center gap-1 text-brand-600 hover:underline text-xs"
                  >
                    <Pencil size={13} /> Editar
                  </button>
                )}
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}

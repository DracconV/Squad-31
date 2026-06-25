import { useState } from 'react'
import { useQueries, useQuery } from '@tanstack/react-query'
import { useAuth } from '../contexts/AuthContext'
import { api, buscarCertificado, listarCursos, type Certificado, type Curso } from '../lib/api'
import { EmptyState } from '../components/EmptyState'
import { StatusBanner } from '../components/StatusBanner'
import { Card } from '../components/Card'
import { Award, Download, ShieldCheck, ExternalLink } from 'lucide-react'

/* ── Card de certificado ──────────────────────────────────── */

function CertificadoCard({ cert, curso, alunoId }: { cert: Certificado; curso: Curso; alunoId: string }) {
  const emitidoEm = new Date(cert.emitidoEm).toLocaleDateString('pt-BR')
  const [baixando, setBaixando] = useState(false)

  async function handleDownload() {
    setBaixando(true)
    try {
      const resp = await api.get(`/certificados/${alunoId}/${curso.id}/pdf`, {
        responseType: 'blob',
      })
      const url = URL.createObjectURL(new Blob([resp.data], { type: 'application/pdf' }))
      const a = document.createElement('a')
      a.href = url
      a.download = `certificado-${curso.nome.replace(/\s+/g, '-')}.pdf`
      a.click()
      URL.revokeObjectURL(url)
    } catch {
      alert('Não foi possível baixar o PDF. Tente novamente.')
    } finally {
      setBaixando(false)
    }
  }

  return (
    <Card hover className="p-5 flex flex-col gap-3">
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-start gap-3 min-w-0">
          <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gold-400/20 text-gold-600">
            <Award size={20} />
          </span>
          <div className="min-w-0">
            <h3 className="font-semibold text-gray-800 truncate">{curso.nome}</h3>
            <p className="text-xs text-gray-400 mt-0.5">Emitido em {emitidoEm}</p>
          </div>
        </div>
        <span
          className={`shrink-0 inline-flex items-center gap-1 text-xs font-medium px-2 py-1 rounded-full ${
            cert.valido
              ? 'bg-brand-50 text-brand-700'
              : 'bg-red-100 text-red-600'
          }`}
        >
          {cert.valido && <ShieldCheck size={13} />}
          {cert.valido ? 'Válido' : 'Revogado'}
        </span>
      </div>

      {cert.valido && (
        <>
          <button
            onClick={handleDownload}
            disabled={baixando}
            className="w-full inline-flex items-center justify-center gap-1.5 py-2 px-4 rounded-lg bg-brand-600 text-white text-sm font-medium
                       hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <Download size={15} /> {baixando ? 'Baixando...' : 'Baixar certificado PDF'}
          </button>

          <div className="flex gap-2 items-center">
            <a
              href={`/verificar-certificado/${cert.qrCode}`}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1 text-xs text-brand-600 hover:underline"
            >
              <ExternalLink size={12} /> Verificar autenticidade
            </a>
            <span className="text-gray-300">·</span>
            <span className="text-xs text-gray-400 font-mono truncate">{cert.qrCode}</span>
          </div>
        </>
      )}

      {!cert.valido && (
        <StatusBanner variant="warning">
          Este certificado foi revogado e não é mais válido.
        </StatusBanner>
      )}
    </Card>
  )
}

/* ── Página principal ─────────────────────────────────────── */

export default function CertificadosPage() {
  const { user } = useAuth()

  /* 1 — Busca todos os cursos ativos */
  const {
    data: cursos = [],
    isLoading: loadingCursos,
    isError: erroCursos,
  } = useQuery<Curso[]>({
    queryKey: ['cursos'],
    queryFn: listarCursos,
    staleTime: 1000 * 60 * 10, // 10 min
  })

  /* 2 — Para cada curso, tenta buscar o certificado do aluno em paralelo */
  const certQueries = useQueries({
    queries: cursos.map((curso) => ({
      queryKey: ['certificado', user?.id, curso.id],
      queryFn: () => buscarCertificado(user!.id, curso.id),
      enabled: !!user?.id && cursos.length > 0,
      staleTime: 1000 * 60 * 5,
      retry: false, // 404 não deve retentar
    })),
  })

  /* 3 — Filtra somente os que existem (não-null) */
  const certificados: Array<{ cert: Certificado; curso: Curso }> = certQueries
    .map((q, i) => ({ cert: q.data ?? null, curso: cursos[i] }))
    .filter((item): item is { cert: Certificado; curso: Curso } =>
      item.cert !== null && item.cert !== undefined,
    )

  const loadingCerts = certQueries.some((q) => q.isLoading)
  const isLoading    = loadingCursos || loadingCerts

  /* ── Render ── */

  if (erroCursos) {
    return (
      <StatusBanner variant="error">
        Não foi possível carregar os cursos. Verifique a conexão e tente novamente.
      </StatusBanner>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-semibold text-gray-800">Meus Certificados</h2>
          <p className="text-sm text-gray-500">
            Certificados emitidos após a conclusão dos cursos.
          </p>
        </div>
        {!isLoading && (
          <span className="text-sm text-gray-400">
            {certificados.length}{' '}
            {certificados.length === 1 ? 'certificado' : 'certificados'}
          </span>
        )}
      </div>

      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[1, 2, 3].map((i) => (
            <div
              key={i}
              className="bg-white rounded-xl p-5 border border-gray-100 animate-pulse h-28"
            />
          ))}
        </div>
      )}

      {!isLoading && certificados.length === 0 && (
        <Card className="p-4">
          <EmptyState
            icon={<Award size={30} strokeWidth={1.75} />}
            title="Nenhum certificado ainda"
            description="Seus certificados aparecerão aqui após a conclusão dos cursos."
          />
        </Card>
      )}

      {!isLoading && certificados.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {certificados.map(({ cert, curso }) => (
            <CertificadoCard key={cert.id} cert={cert} curso={curso} alunoId={user!.id} />
          ))}
        </div>
      )}
    </div>
  )
}

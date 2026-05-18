import { useQueries, useQuery } from '@tanstack/react-query'
import { useAuth } from '../contexts/AuthContext'
import { buscarCertificado, listarCursos, type Certificado, type Curso } from '../lib/api'
import { EmptyState } from '../components/EmptyState'
import { StatusBanner } from '../components/StatusBanner'

/* ── Card de certificado ──────────────────────────────────── */

function CertificadoCard({ cert, curso }: { cert: Certificado; curso: Curso }) {
  const emitidoEm = new Date(cert.emitidoEm).toLocaleDateString('pt-BR')

  return (
    <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100 flex flex-col gap-3">
      <div className="flex items-start justify-between">
        <div>
          <h3 className="font-semibold text-gray-800">{curso.nome}</h3>
          <p className="text-xs text-gray-400 mt-0.5">Emitido em {emitidoEm}</p>
        </div>
        <span
          className={`text-xs font-medium px-2 py-1 rounded-full ${
            cert.valido
              ? 'bg-green-100 text-green-700'
              : 'bg-red-100 text-red-600'
          }`}
        >
          {cert.valido ? 'Válido' : 'Revogado'}
        </span>
      </div>

      {cert.valido && (
        <div className="flex gap-2 mt-1">
          <a
            href={`/verificar-certificado/${cert.qrCode}`}
            target="_blank"
            rel="noreferrer"
            className="text-xs text-blue-600 hover:underline"
          >
            🔗 Verificar autenticidade
          </a>
          <span className="text-gray-300">·</span>
          <span className="text-xs text-gray-400 font-mono">{cert.qrCode}</span>
        </div>
      )}

      {!cert.valido && (
        <StatusBanner variant="warning">
          Este certificado foi revogado e não é mais válido.
        </StatusBanner>
      )}
    </div>
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
        <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
          <EmptyState
            title="Nenhum certificado ainda"
            description="Seus certificados aparecerão aqui após a conclusão dos cursos."
          />
        </div>
      )}

      {!isLoading && certificados.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {certificados.map(({ cert, curso }) => (
            <CertificadoCard key={cert.id} cert={cert} curso={curso} />
          ))}
        </div>
      )}
    </div>
  )
}

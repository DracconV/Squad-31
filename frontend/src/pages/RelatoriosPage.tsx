import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../contexts/AuthContext'
import { getResumoRede, getTaxaConclusao, getAlunosPrimeiroAcesso, getPainelMacro } from '../lib/api'

function StatBox({ label, value, sub }: { label: string; value: string | number; sub?: string }) {
  return (
    <div className="bg-white rounded-xl p-5 border border-gray-100 shadow-sm">
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-3xl font-bold text-gray-800 mt-1">{value}</p>
      {sub && <p className="text-xs text-gray-400 mt-0.5">{sub}</p>}
    </div>
  )
}

export default function RelatoriosPage() {
  const { user } = useAuth()
  const isAdminSeed = user?.perfil === 'ADMIN_SEED'

  const { data: rede, isLoading: loadingRede, error: erroRede } = useQuery({
    queryKey: ['relatorio-rede'],
    queryFn: getResumoRede,
    enabled: isAdminSeed,
    retry: false,
  })

  const { data: taxas = [], isLoading: loadingTaxas } = useQuery({
    queryKey: ['relatorio-taxas'],
    queryFn: getTaxaConclusao,
    retry: false,
  })

  const { data: primeiroAcesso, isLoading: loadingPA } = useQuery({
    queryKey: ['relatorio-primeiro-acesso'],
    queryFn: getAlunosPrimeiroAcesso,
    enabled: isAdminSeed,
    retry: false,
  })

  const { data: painel, isLoading: loadingPainel } = useQuery({
    queryKey: ['relatorio-painel-macro'],
    queryFn: getPainelMacro,
    enabled: isAdminSeed,
    retry: false,
  })

  return (
    <div className="space-y-8">
      <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <h1 className="text-xl font-bold text-gray-800">Relatórios</h1>
        <p className="text-sm text-gray-500 mt-0.5">Indicadores da rede SEED Educa</p>
      </div>

      {/* Resumo da rede — só ADMIN_SEED */}
      {isAdminSeed && (
        <section>
          <h2 className="text-base font-semibold text-gray-700 mb-3">Resumo da rede</h2>
          {loadingRede ? (
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              {[1,2,3,4].map((i) => <div key={i} className="h-24 bg-white rounded-xl border animate-pulse" />)}
            </div>
          ) : erroRede ? (
            <p className="text-sm text-red-500 bg-red-50 p-3 rounded-lg">
              Erro ao carregar resumo da rede. Verifique se o ms-relatorios está rodando.
            </p>
          ) : rede ? (
            <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
              <StatBox label="Instituições ativas" value={rede.total_instituicoes} />
              <StatBox label="Turmas ativas" value={rede.total_turmas} />
              <StatBox label="Alunos ativos" value={rede.total_alunos} />
              <StatBox label="Professores" value={rede.total_professores} />
              <StatBox
                label="Média geral de notas"
                value={Number(rede.media_geral_nota).toFixed(2)}
                sub="Escala 0–10"
              />
            </div>
          ) : null}
        </section>
      )}

      {/* Taxa de conclusão por curso */}
      <section>
        <h2 className="text-base font-semibold text-gray-700 mb-3">Taxa de conclusão por curso</h2>
        {loadingTaxas ? (
          <div className="space-y-2">
            {[1,2,3].map((i) => <div key={i} className="h-12 bg-white rounded-xl border animate-pulse" />)}
          </div>
        ) : taxas.length === 0 ? (
          <div className="bg-white rounded-xl p-8 text-center border border-gray-100">
            <p className="text-gray-400 text-sm">Nenhum dado de conclusão disponível.</p>
          </div>
        ) : (
          <div className="bg-white rounded-xl border border-gray-100 overflow-hidden shadow-sm">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-500 text-left">
                <tr>
                  <th className="px-5 py-3 font-medium">Curso ID</th>
                  <th className="px-5 py-3 font-medium">Inscritos</th>
                  <th className="px-5 py-3 font-medium">Concluídos</th>
                  <th className="px-5 py-3 font-medium">Taxa</th>
                  <th className="px-5 py-3 font-medium w-48">Progresso</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {taxas.map((t) => (
                  <tr key={t.curso_id} className="hover:bg-gray-50">
                    <td className="px-5 py-3 font-mono text-xs text-gray-500">{t.curso_id.slice(0, 8)}…</td>
                    <td className="px-5 py-3 text-gray-700">{t.total_inscritos}</td>
                    <td className="px-5 py-3 text-gray-700">{t.total_concluidos}</td>
                    <td className="px-5 py-3 font-semibold text-gray-800">{t.taxa_conclusao.toFixed(1)}%</td>
                    <td className="px-5 py-3">
                      <div className="w-full bg-gray-100 rounded-full h-2">
                        <div
                          className="bg-blue-500 h-2 rounded-full transition-all"
                          style={{ width: `${Math.min(t.taxa_conclusao, 100)}%` }}
                        />
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Painel macro por município — só ADMIN_SEED */}
      {isAdminSeed && (
        <section aria-labelledby="painel-macro-titulo">
          <h2 id="painel-macro-titulo" className="text-base font-semibold text-gray-700 mb-3">
            Painel macro por município
            {painel && (
              <span className="ml-2 text-sm font-normal text-gray-400">
                ({painel.total_municipios} municípios)
              </span>
            )}
          </h2>
          {loadingPainel ? (
            <div className="space-y-2">
              {[1,2,3].map((i) => <div key={i} className="h-12 bg-white rounded-xl border animate-pulse" />)}
            </div>
          ) : !painel || painel.municipios.length === 0 ? (
            <div className="bg-white rounded-xl p-8 text-center border border-gray-100">
              <p className="text-gray-400 text-sm">Nenhum dado de município disponível.</p>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-gray-100 overflow-hidden shadow-sm">
              <table className="w-full text-sm" aria-label="Painel de municípios">
                <thead className="bg-gray-50 text-gray-500 text-left">
                  <tr>
                    <th scope="col" className="px-5 py-3 font-medium">Município</th>
                    <th scope="col" className="px-5 py-3 font-medium">Escolas</th>
                    <th scope="col" className="px-5 py-3 font-medium">Alunos</th>
                    <th scope="col" className="px-5 py-3 font-medium">Professores</th>
                    <th scope="col" className="px-5 py-3 font-medium">Média notas</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {painel.municipios.map((m) => (
                    <tr key={m.municipio} className="hover:bg-gray-50">
                      <td className="px-5 py-3 font-medium text-gray-800">{m.municipio}</td>
                      <td className="px-5 py-3 text-gray-600">{m.total_instituicoes}</td>
                      <td className="px-5 py-3 text-gray-600">{m.total_alunos}</td>
                      <td className="px-5 py-3 text-gray-600">{m.total_professores}</td>
                      <td className="px-5 py-3">
                        <span className={`font-semibold ${Number(m.media_notas) >= 7 ? 'text-green-600' : Number(m.media_notas) >= 5 ? 'text-amber-600' : 'text-red-500'}`}>
                          {Number(m.media_notas).toFixed(2)}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}

      {/* Alunos aguardando 1º acesso — só ADMIN_SEED */}
      {isAdminSeed && (
        <section>
          <h2 className="text-base font-semibold text-gray-700 mb-3">
            Alunos aguardando 1º acesso
            {primeiroAcesso && (
              <span className="ml-2 text-sm font-normal text-gray-400">({primeiroAcesso.total})</span>
            )}
          </h2>
          {loadingPA ? (
            <div className="space-y-2">
              {[1,2].map((i) => <div key={i} className="h-12 bg-white rounded-xl border animate-pulse" />)}
            </div>
          ) : !primeiroAcesso || primeiroAcesso.alunos.length === 0 ? (
            <div className="bg-white rounded-xl p-8 text-center border border-gray-100">
              <p className="text-gray-400 text-sm">Todos os alunos já realizaram o 1º acesso. ✓</p>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-gray-100 overflow-hidden shadow-sm">
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-500 text-left">
                  <tr>
                    <th className="px-5 py-3 font-medium">Nome</th>
                    <th className="px-5 py-3 font-medium">Matrícula</th>
                    <th className="px-5 py-3 font-medium">Perfil</th>
                    <th className="px-5 py-3 font-medium">Cadastrado em</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {primeiroAcesso.alunos.map((a) => (
                    <tr key={a.id} className="hover:bg-gray-50">
                      <td className="px-5 py-3 font-medium text-gray-800">{a.nome}</td>
                      <td className="px-5 py-3 text-gray-500">{a.matricula}</td>
                      <td className="px-5 py-3 text-xs text-gray-400">{a.perfil}</td>
                      <td className="px-5 py-3 text-gray-400 text-xs">
                        {new Date(a.criado_em).toLocaleDateString('pt-BR')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}
    </div>
  )
}

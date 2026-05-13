import PortalLayout from '../../components/PortalLayout'
import PlaceholderPanel from '../../components/PlaceholderPanel'

export default function AdminSeedHome() {
  return (
    <PortalLayout
      titulo="Administração · SEED"
      subtitulo="Painel macro de inteligência educacional"
    >
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <PlaceholderPanel
          titulo="Visão por município"
          itens={[
            'Comparativo entre municípios',
            'Adesão e desempenho',
            'Mapa de calor por disciplina',
          ]}
        />
        <PlaceholderPanel
          titulo="Diagnóstico estadual"
          itens={[
            'Lacunas mais frequentes',
            'Tendências por série',
            'Exportar relatório PDF',
          ]}
        />
        <PlaceholderPanel
          titulo="Auditoria"
          itens={[
            'Logs de acesso',
            'Inferências de IA',
            'Conformidade LGPD',
          ]}
        />
      </div>
    </PortalLayout>
  )
}

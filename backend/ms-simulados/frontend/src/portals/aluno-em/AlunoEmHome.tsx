import PortalLayout from '../../components/PortalLayout'
import PlaceholderPanel from '../../components/PlaceholderPanel'

export default function AlunoEmHome() {
  return (
    <PortalLayout
      titulo="Aluno · Ensino Médio"
      subtitulo="Estude, simule e acompanhe seu desempenho"
    >
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <PlaceholderPanel
          titulo="Banco de questões"
          itens={[
            'Filtrar por disciplina e assunto',
            'Resolver com correção imediata',
            'Histórico de acertos por tópico',
          ]}
        />
        <PlaceholderPanel
          titulo="Simulados"
          itens={[
            'Lista de simulados disponíveis',
            'Cronômetro e auto-save',
            'Resultado e gabarito comentado',
          ]}
        />
        <PlaceholderPanel
          titulo="Diagnóstico adaptativo"
          itens={[
            'Mapa de lacunas por disciplina',
            'Sugestão de questões personalizadas',
            'Evolução semanal',
          ]}
        />
      </div>
    </PortalLayout>
  )
}

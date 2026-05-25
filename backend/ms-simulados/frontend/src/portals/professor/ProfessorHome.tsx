import PortalLayout from '../../components/PortalLayout'
import PlaceholderPanel from '../../components/PlaceholderPanel'

export default function ProfessorHome() {
  return (
    <PortalLayout
      titulo="Professor"
      subtitulo="Gestão de questões, simulados e turmas"
    >
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <PlaceholderPanel
          titulo="Banco de questões"
          itens={[
            'Cadastrar e revisar questões',
            'Categorização automática (IA)',
            'Importar de planilha',
          ]}
        />
        <PlaceholderPanel
          titulo="Simulados"
          itens={[
            'Montar simulado por tópico',
            'Embaralhamento inteligente',
            'Liberar para uma ou várias turmas',
          ]}
        />
        <PlaceholderPanel
          titulo="Acompanhamento de turmas"
          itens={[
            'Desempenho agregado',
            'Aluno em atenção',
            'Comparativo entre turmas',
          ]}
        />
      </div>
    </PortalLayout>
  )
}

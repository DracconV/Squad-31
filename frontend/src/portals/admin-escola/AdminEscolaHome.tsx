import PortalLayout from '../../components/PortalLayout'
import PlaceholderPanel from '../../components/PlaceholderPanel'

export default function AdminEscolaHome() {
  return (
    <PortalLayout
      titulo="Administração · Escola"
      subtitulo="Gestão institucional da unidade"
    >
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <PlaceholderPanel
          titulo="Importar alunos"
          itens={[
            'Upload de CSV em lote',
            'Validação de matrícula e CPF',
            'Relatório de erros',
          ]}
        />
        <PlaceholderPanel
          titulo="Turmas e professores"
          itens={[
            'Criar e editar turmas',
            'Vincular professores',
            'Listar alunos por turma',
          ]}
        />
        <PlaceholderPanel
          titulo="Painel da escola"
          itens={[
            'Adesão à plataforma',
            'Desempenho geral',
            'Comparativo entre turmas',
          ]}
        />
      </div>
    </PortalLayout>
  )
}

import PortalLayout from '../../components/PortalLayout'
import PlaceholderPanel from '../../components/PlaceholderPanel'

export default function AlunoProfHome() {
  return (
    <PortalLayout
      titulo="Aluno · Profissionalizante"
      subtitulo="Cursos técnicos, módulos e provas práticas"
    >
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <PlaceholderPanel
          titulo="Meus cursos"
          itens={[
            'Listagem de inscrições ativas',
            'Progresso por módulo',
            'Pré-requisitos desbloqueados',
          ]}
        />
        <PlaceholderPanel
          titulo="Agendar prova prática"
          itens={[
            'Slots por unidade e horário',
            'Controle de vagas',
            'Confirmação por e-mail',
          ]}
        />
        <PlaceholderPanel
          titulo="Certificados"
          itens={[
            'Download em PDF',
            'QR Code de validação pública',
            'Compartilhar link',
          ]}
        />
      </div>
    </PortalLayout>
  )
}

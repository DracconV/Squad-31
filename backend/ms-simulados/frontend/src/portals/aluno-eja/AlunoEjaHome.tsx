import PortalLayout from '../../components/PortalLayout'
import PlaceholderPanel from '../../components/PlaceholderPanel'

export default function AlunoEjaHome() {
  return (
    <PortalLayout
      titulo="Aluno · EJA"
      subtitulo="PWA leve, com auto-save e foco em mobile"
    >
      <div className="grid gap-6 sm:grid-cols-2">
        <PlaceholderPanel
          titulo="Continuar de onde parei"
          itens={[
            'Auto-save de respostas no Redis',
            'Notificações contextuais',
            'Modo offline (PWA)',
          ]}
        />
        <PlaceholderPanel
          titulo="Atividades da semana"
          itens={[
            'Lista priorizada por professor',
            'Foco em vídeos curtos e questões',
            'Recompensa por sequência de dias',
          ]}
        />
      </div>
    </PortalLayout>
  )
}

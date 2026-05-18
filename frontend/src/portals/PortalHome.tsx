import { useAuth } from '../contexts/AuthContext'
import PortalLayout from '../components/PortalLayout'
import PlaceholderPanel from '../components/PlaceholderPanel'
import { PORTAL_CONFIG } from './portalConfig'
import type { Perfil } from '../lib/auth'

/**
 * Componente único que renderiza o home de qualquer portal
 * com base no perfil do usuário autenticado.
 * O roteamento continua garantindo que apenas o perfil correto
 * acessa cada rota, portanto `user.perfil` é sempre válido aqui.
 */
export default function PortalHome() {
  const { user } = useAuth()
  const config = PORTAL_CONFIG[user!.perfil as Perfil]

  return (
    <PortalLayout titulo={config.titulo} subtitulo={config.subtitulo}>
      <div className={config.gridCols}>
        {config.panels.map((panel) => (
          <PlaceholderPanel
            key={panel.titulo}
            titulo={panel.titulo}
            itens={panel.itens}
          />
        ))}
      </div>
    </PortalLayout>
  )
}

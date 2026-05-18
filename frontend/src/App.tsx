import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import LoadingScreen from './components/LoadingScreen'
import MainLayout from './layouts/MainLayout'
import { useAuth } from './contexts/AuthContext'
import { getNavConfig } from './config/navigation'
import type { Perfil } from './lib/auth'

import LoginPage        from './pages/LoginPage'
import SemAcessoPage    from './pages/SemAcessoPage'
import DashboardPage    from './pages/DashboardPage'
import SimuladosPage    from './pages/SimuladosPage'
import SimuladoPage     from './pages/SimuladoPage'
import BancoQuestoesPage from './pages/BancoQuestoesPage'
import HistoricoPage    from './pages/HistoricoPage'
import PerformancePage  from './pages/PerformancePage'
import NotasPage        from './pages/NotasPage'
import LocalProvaPage   from './pages/LocalProvaPage'
import CertificadosPage from './pages/CertificadosPage'
import ProvasPage       from './pages/ProvasPage'
import CriarProvaPage   from './pages/CriarProvaPage'
import TurmasPage       from './pages/TurmasPage'
import RelatoriosPage   from './pages/RelatoriosPage'
import GestaoUsuariosPage from './pages/GestaoUsuariosPage'
import EscolasPage      from './pages/EscolasPage'
import AvaliacoesPage   from './pages/AvaliacoesPage'
import PerfilPage       from './pages/PerfilPage'

/** Redireciona para o dashboard do perfil após login */
function RootRedirect() {
  const { user, loading } = useAuth()
  if (loading) return <LoadingScreen />
  if (!user) return <Navigate to="/login" replace />
  const { defaultPath } = getNavConfig(user.perfil as Perfil)
  return <Navigate to={defaultPath} replace />
}

function App() {
  return (
    <Routes>
      {/* Públicas */}
      <Route path="/login"      element={<LoginPage />} />
      <Route path="/sem-acesso" element={<SemAcessoPage />} />

      {/* Raiz → redireciona para dashboard */}
      <Route path="/" element={<RootRedirect />} />

      {/* Área autenticada — todas as rotas dentro do MainLayout */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route path="dashboard"       element={<DashboardPage />} />
        <Route path="simulados"       element={<SimuladosPage />} />
        <Route path="simulados/:id"   element={<SimuladoPage />} />
        <Route path="banco-questoes"  element={<BancoQuestoesPage />} />
        <Route path="historico"       element={<HistoricoPage />} />
        <Route path="desempenho"      element={<PerformancePage />} />
        <Route path="notas"           element={<NotasPage />} />
        <Route path="local-prova"     element={<LocalProvaPage />} />
        <Route path="certificados"    element={<CertificadosPage />} />
        <Route path="provas"          element={<ProvasPage />} />
        <Route path="criar-prova"     element={<CriarProvaPage />} />
        <Route path="turmas"          element={<TurmasPage />} />
        <Route path="relatorios"      element={<RelatoriosPage />} />
        <Route path="gestao-usuarios" element={<GestaoUsuariosPage />} />
        <Route path="escolas"         element={<EscolasPage />} />
        <Route path="avaliacoes"      element={<AvaliacoesPage />} />
        <Route path="perfil"          element={<PerfilPage />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App

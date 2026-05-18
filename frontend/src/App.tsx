import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import LoadingScreen from './components/LoadingScreen'
import { useAuth } from './contexts/AuthContext'
import { ROTA_POR_PERFIL } from './lib/auth'
import LoginPage from './pages/LoginPage'
import SemAcessoPage from './pages/SemAcessoPage'
import PortalHome from './portals/PortalHome'

/**
 * Redireciona o usuário logado para o portal correspondente ao
 * seu perfil. Se não estiver logado, manda para /login.
 */
function RootRedirect() {
  const { user, loading } = useAuth()
  if (loading) return <LoadingScreen />
  if (!user) return <Navigate to="/login" replace />
  return <Navigate to={ROTA_POR_PERFIL[user.perfil] ?? '/login'} replace />
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/sem-acesso" element={<SemAcessoPage />} />

      <Route
        path="/aluno-em/*"
        element={
          <ProtectedRoute perfis={['ALUNO_EM']}>
            <PortalHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/aluno-eja/*"
        element={
          <ProtectedRoute perfis={['ALUNO_EJA']}>
            <PortalHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/aluno-prof/*"
        element={
          <ProtectedRoute perfis={['ALUNO_PROF']}>
            <PortalHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/professor/*"
        element={
          <ProtectedRoute perfis={['PROFESSOR']}>
            <PortalHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin-escola/*"
        element={
          <ProtectedRoute perfis={['ADMIN_ESCOLA']}>
            <PortalHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin-seed/*"
        element={
          <ProtectedRoute perfis={['ADMIN_SEED']}>
            <PortalHome />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App

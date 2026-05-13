import { Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './components/ProtectedRoute'
import { useAuth } from './contexts/AuthContext'
import { ROTA_POR_PERFIL } from './lib/auth'
import LoginPage from './pages/LoginPage'
import SemAcessoPage from './pages/SemAcessoPage'
import AlunoEmHome from './portals/aluno-em/AlunoEmHome'
import AlunoEjaHome from './portals/aluno-eja/AlunoEjaHome'
import AlunoProfHome from './portals/aluno-prof/AlunoProfHome'
import ProfessorHome from './portals/professor/ProfessorHome'
import AdminEscolaHome from './portals/admin-escola/AdminEscolaHome'
import AdminSeedHome from './portals/admin-seed/AdminSeedHome'

/**
 * Redireciona o usuário logado para o portal correspondente ao
 * seu perfil. Se não estiver logado, manda para /login.
 */
function RootRedirect() {
  const { user, loading } = useAuth()
  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center text-slate-500">
        Carregando…
      </div>
    )
  }
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
            <AlunoEmHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/aluno-eja/*"
        element={
          <ProtectedRoute perfis={['ALUNO_EJA']}>
            <AlunoEjaHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/aluno-prof/*"
        element={
          <ProtectedRoute perfis={['ALUNO_PROF']}>
            <AlunoProfHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/professor/*"
        element={
          <ProtectedRoute perfis={['PROFESSOR']}>
            <ProfessorHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin-escola/*"
        element={
          <ProtectedRoute perfis={['ADMIN_ESCOLA']}>
            <AdminEscolaHome />
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin-seed/*"
        element={
          <ProtectedRoute perfis={['ADMIN_SEED']}>
            <AdminSeedHome />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App

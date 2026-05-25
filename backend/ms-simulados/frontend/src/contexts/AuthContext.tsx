import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  clearAuth,
  decodeJwt,
  getPerfilCache,
  getToken,
  isTokenExpired,
  setPerfilCache,
  setToken,
  type Perfil,
} from '../lib/auth'
import { login as apiLogin, type LoginRequest } from '../lib/api'

export interface AuthUser {
  id: string
  perfil: Perfil
  nome?: string
  matricula?: string
}

interface AuthContextValue {
  user: AuthUser | null
  loading: boolean
  signIn: (creds: LoginRequest) => Promise<AuthUser>
  signOut: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

function userFromToken(token: string): AuthUser | null {
  const payload = decodeJwt(token)
  if (!payload) return null
  return {
    id: payload.sub,
    perfil: payload.perfil ?? (getPerfilCache() as Perfil),
    nome: payload.nome,
    matricula: payload.matricula,
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null)
  const [loading, setLoading] = useState(true)

  // Restaura sessão a partir do localStorage no boot.
  useEffect(() => {
    const token = getToken()
    if (token && !isTokenExpired(token)) {
      setUser(userFromToken(token))
    } else if (token) {
      clearAuth()
    }
    setLoading(false)
  }, [])

  const signIn = useCallback(async (creds: LoginRequest) => {
    const resp = await apiLogin(creds)
    setToken(resp.token)
    if (resp.perfil) {
      setPerfilCache(resp.perfil as Perfil)
    }
    const u = userFromToken(resp.token)
    if (!u) {
      throw new Error('Token inválido recebido do servidor')
    }
    setUser(u)
    return u
  }, [])

  const signOut = useCallback(() => {
    clearAuth()
    setUser(null)
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({ user, loading, signIn, signOut }),
    [user, loading, signIn, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  }
  return ctx
}

/**
 * Helpers de armazenamento e leitura do JWT no navegador.
 *
 * Em produção podemos migrar para httpOnly cookies emitidos pelo
 * api-gateway, mas para a primeira versão usamos localStorage por
 * simplicidade. Toda leitura do token deve passar por aqui.
 */

const TOKEN_KEY = 'seed_educa_jwt'
const PROFILE_KEY = 'seed_educa_perfil'

export type Perfil =
  | 'ALUNO_EM'
  | 'ALUNO_EJA'
  | 'ALUNO_PROF'
  | 'PROFESSOR'
  | 'ADMIN_ESCOLA'
  | 'ADMIN_SEED'

export interface JwtPayload {
  sub: string // id do usuário
  perfil: Perfil
  nome?: string
  matricula?: string
  exp: number
  iat?: number
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(PROFILE_KEY)
}

export function setPerfilCache(perfil: Perfil): void {
  localStorage.setItem(PROFILE_KEY, perfil)
}

export function getPerfilCache(): Perfil | null {
  return localStorage.getItem(PROFILE_KEY) as Perfil | null
}

/**
 * Decodifica o payload de um JWT sem validar a assinatura.
 * A validação real é feita no backend — aqui só lemos claims
 * para roteamento e UI.
 */
export function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    // Decodifica base64url respeitando UTF-8 (nomes acentuados: José, João, Conceição)
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join(''),
    )
    return JSON.parse(json) as JwtPayload
  } catch {
    return null
  }
}

export function isTokenExpired(token: string | null): boolean {
  if (!token) return true
  const payload = decodeJwt(token)
  if (!payload || !payload.exp) return true
  // exp é em segundos desde epoch
  return payload.exp * 1000 <= Date.now()
}

/**
 * Mapa de perfil → caminho do portal correspondente.
 * Usado para redirecionar o usuário após o login.
 */
/** Todos os perfis vão para /dashboard — o MainLayout adapta o menu */
export const ROTA_POR_PERFIL: Record<Perfil, string> = {
  ALUNO_EM:    '/dashboard',
  ALUNO_EJA:   '/dashboard',
  ALUNO_PROF:  '/dashboard',
  PROFESSOR:   '/dashboard',
  ADMIN_ESCOLA: '/dashboard',
  ADMIN_SEED:  '/dashboard',
}

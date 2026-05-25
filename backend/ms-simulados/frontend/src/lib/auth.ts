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
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
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
export const ROTA_POR_PERFIL: Record<Perfil, string> = {
  ALUNO_EM: '/aluno-em',
  ALUNO_EJA: '/aluno-eja',
  ALUNO_PROF: '/aluno-prof',
  PROFESSOR: '/professor',
  ADMIN_ESCOLA: '/admin-escola',
  ADMIN_SEED: '/admin-seed',
}

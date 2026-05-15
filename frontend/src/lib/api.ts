import axios, { AxiosError } from 'axios'
import { clearAuth, getToken } from './auth'

/**
 * Cliente HTTP único da aplicação.
 *
 * Em desenvolvimento o Vite faz proxy de /api para o api-gateway
 * (ver vite.config.ts). Em produção a baseURL pode vir de
 * VITE_API_BASE_URL.
 */
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15_000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Anexa o JWT em toda requisição autenticada.
api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Em caso de 401, derruba a sessão e força novo login.
api.interceptors.response.use(
  (resp) => resp,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      clearAuth()
      // Evita loops infinitos: só redireciona se já estivermos
      // fora da tela de login.
      if (window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(error)
  },
)

export interface LoginRequest {
  matricula: string
  senha: string
}

export interface LoginResponse {
  token: string
  perfil: string
  nome?: string
  primeiroAcesso?: boolean
}

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/login', payload)
  return data
}

export interface PrimeiroAcessoRequest {
  matricula: string
  senhaTemporaria: string
  novaSenha: string
}

export async function primeiroAcesso(
  payload: PrimeiroAcessoRequest,
): Promise<void> {
  await api.post('/auth/primeiro-acesso', payload)
}

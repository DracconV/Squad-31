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

/* ── Cursos (ms-cursos) ─────────────────────────────────── */

export interface Curso {
  id: string
  nome: string
  descricao: string
  ativo: boolean
  criadoEm: string
}

export async function listarCursos(): Promise<Curso[]> {
  const { data } = await api.get<Curso[]>('/cursos')
  return data
}

export interface Inscricao {
  id: string
  alunoId: string
  cursoId: string
  nomeCurso: string
  descricaoCurso: string
  dataInscricao: string
  concluido: boolean
}

export async function listarMinhasInscricoes(): Promise<Inscricao[]> {
  const { data } = await api.get<Inscricao[]>('/inscricoes/minhas')
  return data
}

export async function inscreverEmCurso(cursoId: string): Promise<Inscricao> {
  const { data } = await api.post<Inscricao>('/inscricoes', { cursoId })
  return data
}

export async function concluirInscricao(inscricaoId: string): Promise<Inscricao> {
  const { data } = await api.put<Inscricao>(`/inscricoes/${inscricaoId}/concluir`)
  return data
}

/* ── Stats (ms-cursos) ──────────────────────────────────── */

export interface StatsAluno {
  cursosAtivos: number
  cursosConcluidos: number
  certificados: number
  totalCursos: number
}

export interface StatsProfessor {
  totalCursos: number
  totalInscricoes: number
  totalConcluidos: number
  alunosAtivos: number
}

export interface StatsAdmin {
  totalCursos: number
  totalInscricoes: number
  totalConcluidos: number
  totalAlunos: number
}

export type Stats = StatsAluno | StatsProfessor | StatsAdmin

export async function getStats(): Promise<Stats> {
  const { data } = await api.get<Stats>('/stats')
  return data
}

/* ── Certificados (ms-certificados) ────────────────────── */

export interface Certificado {
  id: string
  alunoId: string
  cursoId: string
  qrCode: string
  emitidoEm: string
  valido: boolean
}

/**
 * Busca o certificado de um aluno em um curso específico.
 * Retorna null se não encontrado (404).
 */
/* ── Questões (ms-questoes) ─────────────────────────────────── */

export interface QuestaoStats {
  total: number
}

export async function getQuestaoStats(): Promise<QuestaoStats> {
  const { data } = await api.get<QuestaoStats>('/questoes/stats')
  return data
}

export async function triggerImportarEnem(): Promise<{ status: string }> {
  const { data } = await api.post<{ status: string }>('/questoes/importar')
  return data
}

export async function buscarCertificado(
  alunoId: string,
  cursoId: string,
): Promise<Certificado | null> {
  try {
    const { data } = await api.get<Certificado>(
      `/certificados/${alunoId}/${cursoId}`,
    )
    return data
  } catch (err: unknown) {
    if (
      typeof err === 'object' &&
      err !== null &&
      'response' in err &&
      (err as { response?: { status?: number } }).response?.status === 404
    ) {
      return null
    }
    throw err
  }
}

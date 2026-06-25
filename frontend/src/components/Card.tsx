import type { ReactNode } from 'react'

const SHADOW = 'shadow-[0_1px_2px_rgba(16,40,28,.04),0_8px_24px_-12px_rgba(16,40,28,.12)]'
const SHADOW_HOVER = 'hover:shadow-[0_2px_4px_rgba(16,40,28,.06),0_14px_30px_-10px_rgba(16,40,28,.2)]'

interface CardProps {
  children: ReactNode
  className?: string
  /** Adiciona realce ao passar o mouse (para cards clicáveis). */
  hover?: boolean
}

/** Container padrão do app: branco, cantos suaves, borda discreta e sombra em camadas. */
export function Card({ children, className = '', hover = false }: CardProps) {
  return (
    <div
      className={`bg-white rounded-2xl border border-[#eef2f0] ${SHADOW} ${
        hover ? `${SHADOW_HOVER} transition-shadow` : ''
      } ${className}`}
    >
      {children}
    </div>
  )
}

/** Cabeçalho de seção interno: ícone em chip + título + subtítulo + ação opcional. */
export function SectionHeader({
  icon,
  title,
  subtitle,
  action,
}: {
  icon?: ReactNode
  title: string
  subtitle?: string
  action?: ReactNode
}) {
  return (
    <div className="flex items-center justify-between gap-3 mb-4">
      <div className="flex items-center gap-3 min-w-0">
        {icon && (
          <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600">
            {icon}
          </span>
        )}
        <div className="min-w-0">
          <h2 className="font-bold text-gray-800 leading-tight truncate">{title}</h2>
          {subtitle && <p className="text-sm text-gray-500 truncate">{subtitle}</p>}
        </div>
      </div>
      {action}
    </div>
  )
}

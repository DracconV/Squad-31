import type { ReactNode } from 'react'

type Variant = 'info' | 'success' | 'warning' | 'error'

interface StatusBannerProps {
  variant?: Variant
  children: ReactNode
}

const styles: Record<Variant, string> = {
  info:    'bg-blue-50 border-blue-300 text-blue-800',
  success: 'bg-green-50 border-green-300 text-green-800',
  warning: 'bg-yellow-50 border-yellow-300 text-yellow-800',
  error:   'bg-red-50 border-red-300 text-red-800',
}

const icons: Record<Variant, string> = {
  info: 'ℹ️', success: '✅', warning: '⚠️', error: '❌',
}

export function StatusBanner({ variant = 'info', children }: StatusBannerProps) {
  return (
    <div className={`flex items-start gap-2 px-4 py-3 rounded-lg border text-sm ${styles[variant]}`}>
      <span>{icons[variant]}</span>
      <span>{children}</span>
    </div>
  )
}

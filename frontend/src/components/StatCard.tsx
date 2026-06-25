import type { ReactNode } from 'react'

interface StatCardProps {
  title: string
  value: string | number
  icon?: ReactNode
  trend?: 'up' | 'down' | 'neutral'
  trendValue?: string
  className?: string
}

export function StatCard({
  title,
  value,
  icon,
  trend,
  trendValue,
  className = '',
}: StatCardProps) {
  const trendColors = {
    up:      'text-green-600',
    down:    'text-red-600',
    neutral: 'text-gray-500',
  }
  const trendIcons = { up: '↑', down: '↓', neutral: '→' }

  return (
    <div
      className={`bg-white rounded-2xl p-5 border border-[#eef2f0] shadow-[0_1px_2px_rgba(16,40,28,.04),0_8px_24px_-12px_rgba(16,40,28,.12)] hover:shadow-[0_2px_4px_rgba(16,40,28,.06),0_12px_28px_-10px_rgba(16,40,28,.18)] transition-shadow ${className}`}
    >
      <div className="flex items-start justify-between mb-2">
        <span className="text-sm text-gray-500 font-medium">{title}</span>
        {icon && (
          <span className="inline-flex items-center justify-center w-9 h-9 rounded-xl bg-brand-50 text-brand-600">
            {icon}
          </span>
        )}
      </div>
      <div className="text-3xl font-bold text-gray-800 mb-1">{value}</div>
      {trend && trendValue && (
        <div className={`text-xs font-medium ${trendColors[trend]}`}>
          {trendIcons[trend]} {trendValue}
        </div>
      )}
    </div>
  )
}

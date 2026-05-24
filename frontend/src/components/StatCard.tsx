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
      className={`bg-white rounded-xl p-5 shadow-sm border border-gray-100 hover:shadow-md transition-shadow ${className}`}
    >
      <div className="flex items-start justify-between mb-2">
        <span className="text-sm text-gray-500 font-medium">{title}</span>
        {icon && <span className="text-blue-600">{icon}</span>}
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

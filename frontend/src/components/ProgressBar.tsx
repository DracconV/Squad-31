interface ProgressBarProps {
  value: number        // 0 – 100
  label?: string
  showPercent?: boolean
  color?: 'blue' | 'green' | 'red' | 'yellow'
  className?: string
}

const colorMap = {
  blue:   'bg-brand-600',
  green:  'bg-green-500',
  red:    'bg-red-500',
  yellow: 'bg-yellow-400',
}

export function ProgressBar({
  value,
  label,
  showPercent = true,
  color = 'blue',
  className = '',
}: ProgressBarProps) {
  const pct = Math.min(100, Math.max(0, value))
  return (
    <div className={`w-full ${className}`}>
      {(label || showPercent) && (
        <div className="flex justify-between text-xs text-gray-500 mb-1">
          {label && <span>{label}</span>}
          {showPercent && <span>{pct}%</span>}
        </div>
      )}
      <div className="w-full bg-gray-200 rounded-full h-2">
        <div
          className={`${colorMap[color]} h-2 rounded-full transition-all duration-500`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

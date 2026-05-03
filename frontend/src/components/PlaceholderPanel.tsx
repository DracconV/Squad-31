interface PlaceholderPanelProps {
  titulo: string
  itens: string[]
}

/**
 * Painel reutilizado pelos dashboards placeholder de cada portal.
 * Mostra a lista de funcionalidades planejadas para o perfil.
 */
export default function PlaceholderPanel({
  titulo,
  itens,
}: PlaceholderPanelProps) {
  return (
    <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
      <h2 className="mb-3 text-base font-semibold text-slate-900">{titulo}</h2>
      <ul className="space-y-2 text-sm text-slate-600">
        {itens.map((item) => (
          <li key={item} className="flex items-start gap-2">
            <span
              aria-hidden="true"
              className="mt-1 inline-block h-1.5 w-1.5 flex-shrink-0 rounded-full bg-blue-500"
            />
            <span>{item}</span>
          </li>
        ))}
      </ul>
      <p className="mt-4 text-xs text-slate-400">
        Em construção pelo Squad 31.
      </p>
    </div>
  )
}

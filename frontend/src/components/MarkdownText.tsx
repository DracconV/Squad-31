import type { ReactNode } from 'react'

/** Renderiza negrito (**), itálico (_) e imagens markdown ![alt](url). */
function renderInline(text: string): ReactNode[] {
  const parts: ReactNode[] = []
  const regex = /(\*\*([^*]+)\*\*)|(_([^_]+)_)/g
  let last = 0
  let match: RegExpExecArray | null
  while ((match = regex.exec(text)) !== null) {
    if (match.index > last) parts.push(text.slice(last, match.index))
    if (match[1]) parts.push(<strong key={match.index}>{match[2]}</strong>)
    else if (match[3]) parts.push(<em key={match.index}>{match[4]}</em>)
    last = match.index + match[0].length
  }
  if (last < text.length) parts.push(text.slice(last))
  return parts
}

/** Texto de questão com suporte a imagens (enunciados do ENEM trazem ![](url)). */
export default function MarkdownText({ text }: { text: string }) {
  if (!text) return null

  const segments: ReactNode[] = []
  let remaining = text
  let key = 0

  while (remaining.length > 0) {
    const imgMatch = remaining.match(/^([\s\S]*?)!\[([^\]]*)\]\((https?:\/\/[^)]+)\)/)
    if (imgMatch) {
      if (imgMatch[1]) segments.push(<span key={key++}>{renderInline(imgMatch[1])}</span>)
      segments.push(
        <img
          key={key++}
          src={imgMatch[3]}
          alt={imgMatch[2] || 'imagem da questão'}
          className="max-w-full rounded-lg my-3 mx-auto block border border-gray-100"
          loading="lazy"
          onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
        />,
      )
      remaining = remaining.slice(imgMatch[0].length)
      continue
    }
    segments.push(<span key={key++}>{renderInline(remaining)}</span>)
    break
  }

  return <span className="whitespace-pre-wrap">{segments}</span>
}

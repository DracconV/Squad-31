/**
 * Service Worker — SEED Educa
 * Estratégia: Network First para API; Cache First para assets estáticos.
 */

const CACHE_NAME = 'seed-educa-v1'
const STATIC_ASSETS = ['/', '/index.html', '/manifest.json']

// Instalação — pré-cache de assets críticos
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS))
  )
  self.skipWaiting()
})

// Ativação — remove caches antigos
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    )
  )
  self.clients.claim()
})

// Fetch — Network First para /api, Cache First para o restante
self.addEventListener('fetch', (event) => {
  const { request } = event
  const url = new URL(request.url)

  // Chamadas de API: sempre tenta a rede; sem fallback de cache
  if (url.pathname.startsWith('/api')) {
    event.respondWith(fetch(request))
    return
  }

  // Assets estáticos: tenta cache, depois rede
  event.respondWith(
    caches.match(request).then((cached) => {
      if (cached) return cached
      return fetch(request).then((response) => {
        // Armazena respostas bem-sucedidas de assets estáticos
        if (response.ok && request.method === 'GET') {
          caches.open(CACHE_NAME).then((cache) => cache.put(request, response.clone()))
        }
        return response
      })
    }).catch(() => {
      // Offline fallback: retorna index.html para navegação SPA
      if (request.mode === 'navigate') {
        return caches.match('/index.html')
      }
    })
  )
})

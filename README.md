# 🎓 SEED Educa — Plataforma Educacional da Rede Pública de Sergipe

> Plataforma web/PWA gratuita de prática, avaliação e certificação educacional para alunos da rede pública estadual de Sergipe.

---

## 📌 Sobre o Projeto

O **SEED Educa** centraliza banco de questões, simulados cronometrados, agendamento de provas práticas e dashboards de desempenho, vinculada institucionalmente às escolas da rede pública. Totalmente gratuita, acessível por celular e em conformidade com LGPD e LBI.

### Perfis de Acesso
| Perfil | Descrição |
|---|---|
| Aluno EM | Acesso a questões, simulados e diagnóstico adaptativo |
| Aluno EJA | PWA leve com auto-save e notificações contextuais |
| Aluno Profissionalizante | Cursos técnicos com provas práticas agendáveis |
| Professor | Gestão de questões, simulados e acompanhamento de turmas |
| Admin Escola | Importação de alunos e gestão institucional |
| Admin SEED | Painel macro de inteligência educacional por município |

---

## 🤖 Funcionalidades com IA

| Tipo | Aplicação |
|---|---|
| Predição | Diagnóstico adaptativo — mapa de lacunas por disciplina/tópico |
| Geração de Conteúdo | Categorização automática de questões por disciplina, assunto e dificuldade |
| Automação | Embaralhamento inteligente, certificados com QR Code e notificações contextuais |

> IA implementada via **Claude API (Anthropic)** no serviço `analytics-ia` (Python/FastAPI)

---

## 🏗️ Arquitetura

Stack poliglota — cada serviço usa a linguagem mais adequada para sua responsabilidade.

```
Squad-31/
├── backend/
│   ├── api-gateway/          # Spring Cloud Gateway — porta de entrada única
│   ├── ms-autenticacao/      # Java — JWT, BCrypt, importação CSV
│   ├── ms-questoes/          # Java — Banco de questões + categorização IA
│   ├── ms-simulados/         # Java — Criação, auto-save Redis, cálculo de notas
│   ├── ms-relatorios/        # Java — Dashboards + diagnóstico IA
│   ├── ms-notificacoes/      # Java — Notificações institucionais
│   ├── ms-cursos/            # Go  — Módulos, agendamento, inscrições
│   └── ms-certificados/      # Go  — Geração PDF, QR Code verificável
├── analytics-ia/             # Python — FastAPI + Claude API (diagnóstico adaptativo)
├── frontend/                 # React + TypeScript + Vite + Tailwind CSS
├── infra/
│   ├── docker-compose.yml    # PostgreSQL, Redis, MinIO, Kafka, Prometheus, Grafana
│   ├── nginx.conf
│   ├── .env.example
│   └── .github/
│       └── workflows/
│           └── ci.yml
└── docs/
    └── openapi/              # Specs Swagger por microserviço
```

### Por que Go em ms-cursos e ms-certificados?
Go usa 4x menos memória que Java e inicia 21x mais rápido — ideal para serviços de alta concorrência e lógica simples como verificação pública de QR Code.

### Por que Kafka?
Comunicação assíncrona via **Outbox Pattern**: o `ms-simulados` persiste o evento na tabela `OUTBOX` na mesma transação da nota. Um job Spring Batch publica no Kafka. O `analytics-ia` consome e atualiza o diagnóstico sem perda de dados em caso de falha.

---

## 🛠️ Stack

| Camada | Tecnologia |
|---|---|
| Gateway | Spring Cloud Gateway |
| Backend (lógica) | Spring Boot 3.x + Spring Security + JWT |
| Backend (alta concorrência) | Go + Gin |
| IA / Analytics | Python + FastAPI + Claude API |
| Frontend | React + TypeScript + Vite + Tailwind CSS |
| Banco de Dados | PostgreSQL 16 |
| Cache / Sessões | Redis 7 |
| Mensageria | Apache Kafka |
| Armazenamento | MinIO (self-hosted S3) |
| Métricas | Prometheus + Grafana |
| Tracing | OpenTelemetry + Jaeger |
| CI/CD | GitHub Actions |
| Containers | Docker + Docker Compose |
| Proxy | Nginx |

---

## 🚀 Como Rodar Localmente

### Pré-requisitos
- Docker Desktop instalado e rodando
- Java 21+
- Node.js 20+
- Go 1.22+
- Python 3.11+

### 1. Subir a infraestrutura
```bash
cd infra
cp .env.example .env
docker-compose up -d
```

### 2. Rodar um microserviço Java (exemplo)
```bash
cd backend/ms-autenticacao
./mvnw spring-boot:run
```

### 3. Rodar o frontend
```bash
cd frontend
npm install
npm run dev
```

| Serviço | URL | Credenciais |
|---|---|---|
| Frontend | http://localhost:5173 | — |
| MinIO Console | http://localhost:9001 | seed_minio_user / seed_minio_pass |
| Grafana | http://localhost:3000 | admin / admin |
| Jaeger UI | http://localhost:16686 | — |

---

## 📡 Principais Endpoints

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | `/auth/login` | Autenticação e geração de JWT | Pública |
| POST | `/auth/primeiro-acesso` | Troca de senha no primeiro login | Pública |
| GET | `/questoes` | Listar questões com filtros | JWT |
| POST | `/simulados` | Criar simulado pontuado | JWT |
| POST | `/simulados/{id}/responder` | Auto-save de resposta | JWT |
| POST | `/simulados/{id}/finalizar` | Finalizar e calcular nota | JWT |
| GET | `/api/notificacoes` | Listar notificações institucionais do usuário | JWT |
| GET | `/api/notificacoes/unread/count` | Contar notificações não lidas | JWT |
| POST | `/api/notificacoes/destino` | Criar notificações para destinatários específicos | JWT (admin) |
| GET | `/alunos/{id}/diagnostico` | Mapa de lacunas por IA | JWT |
| GET | `/turmas/{id}/desempenho` | Desempenho agregado da turma | JWT |
| GET | `/certificados/{aluno}/{curso}` | Download do certificado PDF | JWT |
| GET | `/verificar-certificado/{qr}` | Verificação pública de autenticidade | Pública |
| POST | `/admin/importar-alunos` | Upload CSV para importação em lote | JWT (admin) |
| GET | `/seed/painel-macro` | Painel de inteligência educacional | JWT (SEED) |

> Documentação completa disponível em `/docs/openapi/`

---

## 🔒 Segurança e Conformidade

- **Senhas** nunca commitadas — variáveis de ambiente via `.env` (baseado em `.env.example`)
- **JWT** com controle de acesso por perfil via `@PreAuthorize`
- **LGPD** — dados anonimizados em exportações coletivas, audit log de todas as inferências de IA
- **LBI** — acessibilidade WCAG 2.1 AA, PWA instalável no Android

---

## 👥 Squad 31

Projeto desenvolvido como parte do programa de inovação educacional da SEED/SE.

## 📄 Licença

Este projeto é de uso institucional pela Secretaria de Estado da Educação de Sergipe (SEED/SE).

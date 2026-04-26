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

---

## 🏗️ Arquitetura

```
Squad-31/
├── backend/
│   ├── api-gateway/          # Spring Cloud Gateway
│   ├── ms-autenticacao/      # JWT, BCrypt, importação CSV
│   ├── ms-questoes/          # Banco de questões + categorização IA
│   ├── ms-simulados/         # Criação, auto-save, cálculo de notas
│   ├── ms-cursos/            # Módulos, agendamento, certificados
│   └── ms-relatorios/        # Dashboards + diagnóstico IA
├── frontend/
│   └── src/
│       ├── portals/          # 6 portais separados por perfil
│       └── services/         # Chamadas à API
├── infra/
│   ├── docker-compose.yml
│   ├── nginx.conf
│   └── .github/workflows/
└── docs/
    └── openapi/              # Specs Swagger por microserviço
```

---

## 🛠️ Stack

| Camada | Tecnologia |
|---|---|
| Backend | Spring Boot 3.x + Spring Security + JWT |
| Gateway | Spring Cloud Gateway |
| Banco de Dados | PostgreSQL 16 |
| Cache / Sessões | Redis 7 |
| Armazenamento | MinIO (self-hosted) |
| Frontend | React.js + TypeScript + Tailwind CSS |
| PWA | Service Workers |
| IA | Claude API (Anthropic) / Llama 3 via Ollama |
| CI/CD | GitHub Actions |
| Containers | Docker + Docker Compose |
| Proxy | Nginx |

---

## 🚀 Como Rodar Localmente

### Pré-requisitos
- Docker e Docker Compose instalados
- Java 21+
- Node.js 20+

### 1. Subir a infraestrutura
```bash
cd infra
docker-compose up -d
```

### 2. Rodar um microserviço (exemplo: ms-autenticacao)
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

---

## 📡 Principais Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/auth/login` | Autenticação e geração de JWT |
| GET | `/questoes` | Listar questões com filtros |
| POST | `/simulados` | Criar simulado |
| GET | `/alunos/{id}/diagnostico` | Mapa de lacunas (IA) |
| GET | `/seed/painel-macro` | Painel de inteligência educacional |

> Documentação completa disponível em `/docs/openapi/`

---

## 👥 Squad 31

Projeto desenvolvido como parte do programa de inovação educacional da SEED/SE.

---

## 📄 Licença

Este projeto é de uso institucional pela Secretaria de Estado da Educação de Sergipe (SEED/SE).

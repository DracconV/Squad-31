# ms-cursos

Microserviço Go responsável pelos **cursos profissionalizantes**:
módulos, inscrições e agendamento de provas práticas.

> O schema do banco é controlado pelo Flyway no `ms-autenticacao`.
> Aqui só lemos/escrevemos nas tabelas `curso`, `modulo`,
> `inscricao_curso`, `slot_prova_pratica` e `agendamento_prova`.

## Stack

- Go 1.22
- [Gin](https://github.com/gin-gonic/gin) — HTTP
- [GORM](https://gorm.io) + driver Postgres
- [godotenv](https://github.com/joho/godotenv) — `.env` local

## Como rodar localmente

```bash
# 1) Sobe a infra (Postgres, Kafka, etc.)
cd ../../infra
docker compose up -d postgres

# 2) Volta para o serviço
cd ../backend/ms-cursos

# 3) Variáveis de ambiente
cp .env.example .env

# 4) Dependências e execução
go mod tidy
go run .
```

O serviço sobe em `http://localhost:8085`.

## Endpoints (em desenvolvimento)

| Método | Rota          | Descrição                   |
|--------|---------------|-----------------------------|
| GET    | `/health`     | Healthcheck                 |
| GET    | `/cursos`     | Lista cursos ativos         |
| GET    | `/cursos/:id` | Detalhe de um curso         |
| POST   | `/cursos`     | Cria um novo curso          |

## Próximos passos

- CRUD completo de módulos com pré-requisito.
- Inscrições (`POST /cursos/:id/inscricoes`).
- Slots e agendamento de provas práticas com controle de vagas.
- Validação de JWT injetado pelo `api-gateway`.
- Métricas Prometheus (`/metrics`) e tracing OpenTelemetry.
- Testes (testify + Testcontainers).

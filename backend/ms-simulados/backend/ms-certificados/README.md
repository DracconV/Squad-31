# ms-certificados

Microserviço Go responsável pela **emissão e verificação pública**
de certificados de cursos profissionalizantes da plataforma.

> O schema do banco é controlado pelo Flyway no `ms-autenticacao`.
> Este serviço lê/escreve apenas na tabela `certificado`.

## Por que Go aqui?

A verificação pública (`/verificar-certificado/{qr}`) tende a ter
**alta concorrência e lógica simples** — exatamente o cenário onde
Go brilha (4× menos memória que Java, 21× boot mais rápido,
goroutines baratas).

## Stack

- Go 1.22
- [Gin](https://github.com/gin-gonic/gin)
- [GORM](https://gorm.io) + driver Postgres
- MinIO (S3-compatível) para armazenar os PDFs gerados — em construção

## Como rodar localmente

```bash
cd ../../infra
docker compose up -d postgres minio

cd ../backend/ms-certificados
cp .env.example .env

go mod tidy
go run .
```

O serviço sobe em `http://localhost:8086`.

## Endpoints (em desenvolvimento)

| Método | Rota                                | Descrição                         | Auth   |
|--------|-------------------------------------|-----------------------------------|--------|
| GET    | `/health`                           | Healthcheck                       | —      |
| GET    | `/verificar-certificado/:qr`        | Verificação pública por QR Code   | Pública |
| GET    | `/certificados/:aluno/:curso`       | Buscar certificado do aluno       | JWT    |

## Próximos passos

- Geração real do PDF do certificado (jung-kurt/gofpdf ou equivalente).
- Upload do PDF gerado para o MinIO.
- Geração e embed do QR Code (skip2/go-qrcode).
- Listener Kafka: emite certificado ao consumir evento
  `inscricao_curso.concluida` produzido pelo `ms-cursos`.
- Validação de JWT injetado pelo `api-gateway`.
- Métricas Prometheus e tracing OpenTelemetry.
- Testes (testify + Testcontainers).

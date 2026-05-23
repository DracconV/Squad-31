# ----- build stage -----
FROM golang:1.23-alpine AS builder
WORKDIR /app

# Instala o CLI do swaggo para gerar a documentação Swagger
RUN go install github.com/swaggo/swag/cmd/swag@latest

# Copia os fontes (inclui go.mod/go.sum)
COPY go.mod go.sum* ./
COPY . .

# Adiciona dependências do swagger e gera os docs
RUN go get github.com/swaggo/gin-swagger github.com/swaggo/files && \
    go mod tidy && \
    swag init -g main.go --output docs

RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /out/ms-cursos .

# ----- runtime stage -----
FROM alpine:3.20
RUN apk add --no-cache ca-certificates tzdata
ENV TZ=America/Maceio
WORKDIR /app

COPY --from=builder /out/ms-cursos /app/ms-cursos
EXPOSE 8085
USER nobody
ENTRYPOINT ["/app/ms-cursos"]

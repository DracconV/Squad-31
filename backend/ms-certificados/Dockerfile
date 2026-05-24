# ----- build stage -----
FROM golang:1.25-alpine AS builder
WORKDIR /app

# Instala o CLI do swaggo para gerar a documentação Swagger
RUN go install github.com/swaggo/swag/cmd/swag@latest

COPY go.mod go.sum* ./
RUN go mod download || true

COPY . .

# Adiciona dependências do swagger, gera docs e compila
RUN go get github.com/swaggo/gin-swagger github.com/swaggo/files && \
    go mod tidy && \
    swag init -g main.go --output docs

RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /out/ms-certificados .

# ----- runtime stage -----
FROM alpine:3.20
RUN apk add --no-cache ca-certificates tzdata
ENV TZ=America/Maceio
WORKDIR /app

COPY --from=builder /out/ms-certificados /app/ms-certificados
EXPOSE 8086
USER nobody
ENTRYPOINT ["/app/ms-certificados"]

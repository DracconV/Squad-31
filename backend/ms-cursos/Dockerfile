# ----- build stage -----
FROM golang:1.25-alpine AS builder
WORKDIR /app

# Copia go.mod/go.sum e baixa dependências (camada cacheada)
COPY go.mod go.sum ./
RUN go mod download

# Copia fontes (inclui docs/ já gerado localmente)
COPY . .

# Compila o binário
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

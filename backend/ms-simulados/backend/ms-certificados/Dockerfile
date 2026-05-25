# ----- build stage -----
FROM golang:1.22-alpine AS builder
WORKDIR /app

COPY go.mod go.sum* ./
RUN go mod download || true

COPY . .
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

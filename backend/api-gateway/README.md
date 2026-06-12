# API Gateway

Gateway centralizado do SEED Educa, responsavel por:

- rotear chamadas do frontend para os microsservicos;
- validar JWT emitido pelo `ms-autenticacao`;
- aplicar rate limiting centralizado com Redis.

## Rotas

| Rota | Destino padrao | Autenticacao |
| --- | --- | --- |
| `/auth/login` | `ms-autenticacao` | Publica |
| `/auth/primeiro-acesso` | `ms-autenticacao` | Publica |
| `/auth/**` | `ms-autenticacao` | JWT |
| `/cursos/**` | `ms-cursos` | JWT |
| `/certificados/**` | `ms-certificados` | JWT |
| `/verificar-certificado/**` | `ms-certificados` | Publica |

## Variaveis de ambiente

| Variavel | Padrao |
| --- | --- |
| `JWT_SECRET` | mesmo segredo usado pelo `ms-autenticacao` |
| `SPRING_REDIS_HOST` | `localhost` |
| `SPRING_REDIS_PORT` | `6379` |
| `MS_AUTENTICACAO_URL` | `http://localhost:8081` |
| `MS_CURSOS_URL` | `http://localhost:8085` |
| `MS_CERTIFICADOS_URL` | `http://localhost:8086` |

## Como testar

Com Redis e `ms-autenticacao` rodando:

```bash
mvn spring-boot:run
```

Login pelo gateway:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"matricula":"sua-matricula","senha":"sua-senha"}'
```

Rota protegida:

```bash
curl http://localhost:8080/auth/me \
  -H "Authorization: Bearer SEU_TOKEN"
```

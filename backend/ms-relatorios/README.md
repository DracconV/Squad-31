# MS Relatórios - SEED Educa

Microserviço de dashboards e relatórios de desempenho educacional.

## Descrição

Backend Java/Spring Boot responsável por:
- Cálculo de desempenho de alunos por disciplina
- Agregação de desempenho de turmas
- Geração de relatórios detalhados
- Diagnóstico adaptativo via IA

## Endpoints

- `GET /desempenho/aluno/{alunoId}/disciplina/{disciplina}` - Desempenho de um aluno em disciplina
- `GET /desempenho/aluno/{alunoId}/historico` - Histórico completo do aluno
- `GET /desempenho/turma/{turmaId}` - Desempenho agregado da turma
- `GET /desempenho/turma/{turmaId}/alunos-baixo-desempenho` - Alunos com nota < 6.0

## Tecnologias

- Java 21
- Spring Boot 3.5.14
- PostgreSQL 16
- Kafka
- JWT
- OpenAPI/Swagger

## Como rodar

```bash
cd backend/ms-relatorios
./mvnw spring-boot:run
```

A aplicação rodará na porta `8088`.

## Testes

```bash
./mvnw test
./mvnw jacoco:report
```


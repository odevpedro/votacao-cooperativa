# Cooperative Voting

Aplicação publicada em: [cooperative-voting.onrender.com](https://cooperative-voting.onrender.com)

| Ambiente | Swagger | Health |
|---|---|---|
| Cloud | [swagger-ui.html](https://cooperative-voting.onrender.com/swagger-ui.html) | [/actuator/health](https://cooperative-voting.onrender.com/actuator/health) |
| Local | `http://localhost:8080/swagger-ui/index.html` | `http://localhost:8080/actuator/health` |

Repositório remoto: [github.com/odevpedro/voting-api](https://github.com/odevpedro/voting-api)

## Visão geral

Backend Java 21/Spring Boot para cadastrar pautas, abrir uma única sessão por pauta,
receber votos `SIM`/`NAO`, impedir voto duplicado e calcular resultados. A mesma
aplicação expõe uma API REST de domínio e contratos JSON de apresentação para um
cliente mobile externo.

O projeto está publicado no Render (plano gratuito) com PostgreSQL Neon.
O perfil `cloud` é ativado automaticamente via `render.yaml`.

## Documentação

- [Arquitetura](docs/architecture.md)
- [Modelo de dados](docs/data-model.md)
- [Fluxos funcionais](docs/system-feature-flows.md)
- [Deploy](docs/deployment.md)
- [Testes de performance](docs/performance-tests.md)
- [ADRs](docs/adr/)
- [Contrato OpenAPI](openapi/openapi.yaml)
- [Collection Postman](collections/README.md)

## Decisões principais

- monólito modular organizado por funcionalidade;
- PostgreSQL + Flyway, com `ddl-auto=validate`;
- uma sessão por pauta para remover ambiguidade;
- sessão aberta quando `openedAt <= agora < closesAt`, sem scheduler;
- unicidade de voto garantida em Java e por `UNIQUE (session_id, associate_id)`;
- `Clock` injetado para testes temporais determinísticos;
- agregação de resultados no banco, sem carregar votos;
- integração de elegibilidade sem retry automático;
- rotas públicas centralizadas em `ApiRoutes`;
- erros RFC 9457 com `ProblemDetail` e `correlationId`;
- nenhum frontend, autenticação, mensageria, cache ou microsserviço.

Os trade-offs estão detalhados em `docs/architecture.md` e nos ADRs.

## Executar

Pré-requisitos: Docker e Docker Compose.

```bash
docker compose up --build
```

Nenhum arquivo `.env` é necessário: o Compose já possui valores locais seguros.

Para manter apenas o banco no Docker:

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

A integração externa fica desabilitada por padrão em todos os profiles e pode ser
habilitada explicitamente por variável de ambiente. O schema é criado
automaticamente pelo Flyway.

## Exemplos rápidos

Suba `BASE_URL` conforme o ambiente:

```bash
# local
BASE_URL=http://localhost:8080
# cloud
BASE_URL=https://cooperative-voting.onrender.com
```

```bash
curl -i -X POST "$BASE_URL/api/v1/agendas" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Orçamento anual","description":"Aprovação do orçamento"}'
```

```bash
curl -i -X POST "$BASE_URL/api/v1/agendas/AGENDA_ID/sessions" \
  -H 'Content-Type: application/json' \
  -d '{"durationMinutes":5}'
```

```bash
curl -i -X POST "$BASE_URL/api/v1/agendas/AGENDA_ID/votes" \
  -H 'Content-Type: application/json' \
  -d '{"associateId":"member-42","choice":"SIM"}'
```

O fluxo completo reproduzível está em `scripts/demo.sh`.

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/v1/agendas` | cria pauta |
| GET | `/api/v1/agendas` | lista pautas |
| GET | `/api/v1/agendas/{agendaId}` | consulta pauta |
| POST | `/api/v1/agendas/{agendaId}/sessions` | abre sessão |
| GET | `/api/v1/agendas/{agendaId}/sessions/current` | consulta sessão |
| POST | `/api/v1/agendas/{agendaId}/votes` | registra voto |
| GET | `/api/v1/agendas/{agendaId}/results` | consulta resultado |
| GET | `/api/v1/mobile/agendas` | tela de pautas |
| POST | `/api/v1/mobile/agendas/{agendaId}/identify` | tela de identificação |
| POST | `/api/v1/mobile/agendas/{agendaId}/vote-options` | opções de voto |

## Testes e qualidade

```bash
./mvnw verify
```

A suíte contém testes unitários, WireMock e integração/API com PostgreSQL
Testcontainers. Ela cobre concorrência de voto duplicado, sessão encerrada, empate e
o contrato JSON mobile. JaCoCo gera o relatório em `target/site/jacoco`; Spotless
roda no `verify`.

Em Docker 29, cuja API mínima é 1.44:

```bash
./mvnw -Dapi.version=1.44 verify
```

O cenário k6 e os resultados locais medidos estão em
[`docs/performance-tests.md`](docs/performance-tests.md).

## Variáveis

| Variável | Obrigatória em cloud | Padrão local |
|---|---:|---|
| `PORT` | não | `8080` |
| `SPRING_PROFILES_ACTIVE` | sim | `local` |
| `SPRING_DATASOURCE_URL` | sim | PostgreSQL local |
| `SPRING_DATASOURCE_USERNAME` | sim | `cooperative` |
| `SPRING_DATASOURCE_PASSWORD` | sim | `cooperative` |
| `PUBLIC_BASE_URL` | sim | `http://localhost:8080` |
| `VOTER_ELIGIBILITY_ENABLED` | não | `false` |
| `VOTER_ELIGIBILITY_BASE_URL` | quando habilitada | serviço informado no plano |
| `VOTER_ELIGIBILITY_CONNECT_TIMEOUT` | não | `2s` |
| `VOTER_ELIGIBILITY_READ_TIMEOUT` | não | `3s` |
| `JAVA_TOOL_OPTIONS` | recomendada | vazia |

## Elegibilidade

Com a integração habilitada, o identificador precisa ser um CPF com 11 dígitos:
`ABLE_TO_VOTE` permite o voto, `UNABLE_TO_VOTE` e CPF inválido retornam `422`, e
timeout/5xx/resposta inválida ou aplicação remota indisponível retornam `503`. Não
há retry e nenhum CPF completo é escrito pelos logs da aplicação.

Com a integração desabilitada, a API de domínio aceita identificadores de associado
com 1 a 64 caracteres. O fluxo mobile continua solicitando CPF.

## Limitações e próximos passos

- validar persistência após redeploy e registrar data/tag da publicação;
- repetir a medição k6 no ambiente cloud escolhido;
- substituir a URL de elegibilidade do enunciado antes de habilitar a integração:
  a aplicação Heroku informada atualmente responde `No such app`;
- o plano gratuito de cloud pode apresentar cold start e não representa produção;
- não há autenticação por decisão explícita de escopo.

# Cooperative Voting

Backend Java 21 + Spring Boot para votação cooperativa com pautas, sessões, votos
e resultados — disponível como API REST de domínio e contratos JSON mobile.

**Publicado em:** [cooperative-voting.onrender.com](https://cooperative-voting.onrender.com)

| Ambiente | Swagger | Health |
|---|---|---|
| Cloud | [cooperative-voting.onrender.com/swagger-ui.html](https://cooperative-voting.onrender.com/swagger-ui.html) | [/actuator/health](https://cooperative-voting.onrender.com/actuator/health) |
| Local | `http://localhost:8080/swagger-ui/index.html` | `http://localhost:8080/actuator/health` |

Repositório: [github.com/odevpedro/votacao-cooperativa](https://github.com/odevpedro/votacao-cooperativa)

Publicado no Render (plano gratuito) com PostgreSQL Neon. O perfil `cloud`
é ativado automaticamente via `render.yaml` — sem necessidade de configuração manual.

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

- Monólito modular organizado por funcionalidade (agenda, sessão, voto, mobile)
- PostgreSQL + Flyway com `ddl-auto=validate`
- Uma sessão por pauta — elimina ambiguidade de qual sessão está ativa
- Sessão aberta enquanto `openedAt <= now < closesAt`, sem scheduler
- Unicidade de voto garantida em Java e por constraints `UNIQUE` no banco
- `Clock` injetado para testes temporais determinísticos
- Agregação de resultados no banco (sem carregar votos em memória)
- Integração de elegibilidade sem retry automático
- Rotas centralizadas em `ApiRoutes` — um único ponto de verdade
- Erros seguem RFC 9457 com `ProblemDetail` e `correlationId` para rastreabilidade
- Sem frontend, autenticação, mensageria, cache ou microsserviços
- Logs estruturados em JSON (Logstash) com correlationId em todas as respostas
- CPF nunca é logado — apenas hash SHA-256 para auditoria (LGPD)

Os trade-offs estão detalhados em [`docs/architecture.md`](docs/architecture.md) e nos ADRs.

## Executar localmente

Pré-requisitos: Docker, Docker Compose e Java 21+.

```bash
docker compose up --build
```

O Compose já contém valores seguros para desenvolvimento — nenhum `.env` necessário.
A integração de elegibilidade vem desabilitada por padrão. O schema é criado
automaticamente pelo Flyway.

Para rodar a aplicação fora do container (apenas banco no Docker):

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## Exemplos rápidos

Defina `BASE_URL` conforme o ambiente de destino:

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

O fluxo completo reproduzível está em [`scripts/demo.sh`](scripts/demo.sh).

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
| GET | `/api/v1/mobile/agendas` | lista pautas (contrato mobile) |
| POST | `/api/v1/mobile/agendas/{agendaId}/identify` | tela de identificação |
| POST | `/api/v1/mobile/agendas/{agendaId}/vote-options` | opções de voto |

## Testes e qualidade

```bash
./mvnw verify
```

A suíte contempla:
- **Testes unitários** (JUnit 5 + Mockito) para serviços e handlers de erro
- **WireMock** para simular o serviço externo de elegibilidade
- **Integração com PostgreSQL real** via Testcontainers
- **Cobertura JaCoCo** em `target/site/jacoco`
- **Verificação de formatação** com Spotless (Google Java Format)

Em ambientes com Docker 29 (API mínima 1.44):

```bash
./mvnw -Dapi.version=1.44 verify
```

Cenários de performance com k6 em [`docs/performance-tests.md`](docs/performance-tests.md).

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

A verificação de elegibilidade pode ser habilitada via `VOTER_ELIGIBILITY_ENABLED=true`.

**Habilitada** — o identificador deve ser um CPF de 11 dígitos:
- `ABLE_TO_VOTE` → voto permitido
- `UNABLE_TO_VOTE` ou CPF inválido → `422 Unprocessable Entity`
- Timeout, 5xx, resposta inválida ou serviço indisponível → `503 Service Unavailable`
- Sem retry automático
- CPF nunca é escrito nos logs — apenas hash SHA-256 para auditoria (LGPD)

**Desabilitada** (padrão) — aceita identificadores de 1 a 64 caracteres.
O fluxo mobile continua solicitando CPF, mas a validação é feita apenas no
controller (formato livre).

## Limitações e próximos passos

- Repetir a medição k6 no ambiente cloud e comparar com os resultados locais
- Configurar URL de elegibilidade real antes de habilitar a integração (a URL do
  enunciado — `user-info.herokuapp.com` — não está mais ativa)
- O plano gratuito do Render pode apresentar cold start (~30s) após inatividade
- Não há autenticação — decisão explícita de escopo

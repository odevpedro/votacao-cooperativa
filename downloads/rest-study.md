# Estudo REST — Conceitos que passam despercebidos

## Antes de REST — breve histórico

- **Socket bruto** — cada um inventava protocolo binário próprio
- **CORBA / DCOM / RMI** — objetos remotos, mas acoplados a linguagem/plataforma
- **MQ Series / Tibco** — filas mensagens, caro e proprietário
- **SOAP** — XML sobre HTTP, interoperável mas pesado. Resolveu integração corporativa nos anos 2000
- **REST** — simplificou usando JSON + verbos HTTP. Hoje é padrão pra web e mobile

## O que é um Web Service

Sistema que troca dados estruturados (XML/JSON) entre máquinas via HTTP, com um contrato definido.

**Não é web service:**
- Servir página HTML (`GET /index.html`) — conteúdo pra humano
- Upload de arquivo sem contrato
- Endpoint que retorna imagem binária

Web service = **máquina falando com máquina**.

Para revisão futura.

## 1. Content-Location vs Location

- `Location` → usado com `201 Created` (POST) ou redirects (3xx). Aponta onde o recurso foi criado.
- `Content-Location` → usado em GET. Informa a URL canônica do recurso quando a representação retornada é diferente da URL requisitada (ex: `/clientes?page=1` vs `/clientes/page-1`).

## 2. Cache-Control

Headers que indicam se cliente/proxy pode cachear a resposta.
- `Cache-Control: public, max-age=3600` → pode cachear por 1 hora
- `Cache-Control: no-cache` → não usar cache sem revalidar
- `Cache-Control: private` → só o cliente final pode cachear (não proxies intermediários)

Muitas APIs ignoram cache headers e o cliente nunca sabe se pode reaproveitar a resposta.

## 3. ETag / If-None-Match

- Servidor gera hash do recurso (`ETag: "abc123"`)
- Cliente faz requisição com `If-None-Match: "abc123"`
- Se o recurso não mudou, servidor responde `304 Not Modified` sem body
- Economiza banda e processamento

## 4. Link header (HATEOAS)

Em vez de o cliente montar URLs manualmente, o servidor informa os próximos passos:

```
Link: <https://api.exemplo.com/clientes?page=2>; rel="next"
```

Mesmo princípio do `Location`, mas para navegação entre recursos.

## 5. Versionamento por Accept/Content-Type

Alternativa ao `/v1/` na URL:

```
Accept: application/vnd.meuapp.v2+json
```

Permite evoluir o contrato sem poluir as rotas. O servidor mantém duas versões do mesmo endpoint e escolhe baseado no header.

## 6. OPTIONS

Método HTTP que permite ao cliente descobrir quais verbos uma URL aceita sem precisar testar:

```
OPTIONS /api/v1/agendas
Allow: GET, POST
```

Útil para clientes dinâmicos e documentação automática.

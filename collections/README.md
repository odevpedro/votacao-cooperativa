# Collection da API

A fonte executável é `cooperative-voting.postman_collection.json`. Postman mantém
a collection completa em um único JSON; os grupos internos preservam a ordem
Agendas → Sessões → Mobile → Votos → Resultados.

Ambientes:

- `environments/local.json`: `http://localhost:8080`;
- `environments/cloud.json`: preencher `baseUrl` com a URL HTTPS publicada.

Execução:

```bash
npx newman run collections/cooperative-voting.postman_collection.json \
  -e collections/environments/local.json
```

A collection cria e salva `agendaId`/`sessionId`, gera CPFs únicos a cada execução
e valida status e propriedades essenciais. O caso de voto encerrado requer aguardar
o fechamento da sessão e está documentado nos exemplos/OpenAPI; o fluxo
automatizado cobre validação e duplicidade sem adicionar espera de um minuto ao
pipeline.

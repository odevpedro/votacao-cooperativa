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

A collection cria e salva `agendaId`/`sessionId`, gera um CPF para o fluxo mobile e
um identificador genérico para a API de domínio, além de validar status e o contrato
das telas server-driven. O cenário de sessão encerrada é automatizado na suíte de
integração Java sem adicionar uma espera de um minuto ao pipeline.

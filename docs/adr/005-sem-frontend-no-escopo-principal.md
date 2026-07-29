# ADR 005 — Sem frontend no escopo principal

Status: aceito.

## Contexto

O cliente não faz parte do desafio, mas precisa receber contratos de apresentação.

## Decisão

Entregar Swagger, collection, script e endpoints `FORMULARIO`/`SELECAO`, sem SPA.

## Consequências

Positivas: esforço concentrado em domínio, banco e testes. Negativas: demonstração
visual depende do consumidor da API.

## Alternativas rejeitadas

React ou aplicativo mobile demonstrativo, por consumir tempo sem elevar a correção
do backend.

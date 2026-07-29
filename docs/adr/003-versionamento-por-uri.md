# ADR 003 — Versionamento por URI

Status: aceito.

## Contexto

Clientes mobile precisam identificar claramente o contrato utilizado.

## Decisão

Prefixar endpoints públicos com `/api/v1` e centralizar caminhos em `ApiRoutes`.

## Consequências

Positivas: roteamento e documentação explícitos. Negativas: uma versão futura
duplica caminhos durante a migração.

## Alternativas rejeitadas

Header e media type versionado, menos visíveis e desnecessários neste porte.

# ADR 002 — PostgreSQL e unicidade do voto

Status: aceito.

## Contexto

Uma verificação apenas em Java falha quando requisições concorrentes leem antes da
primeira gravação.

## Decisão

Persistir em PostgreSQL/Flyway e impor `UNIQUE(session_id, associate_id)`, além da
validação amigável no serviço.

## Consequências

Positivas: correção atômica e schema versionado. Negativas: testes de integração
dependem de Docker/PostgreSQL e violações concorrentes aparecem nos logs do driver.

## Alternativas rejeitadas

H2, mapa em memória e lock JVM, que não representam ou não protegem o banco real.

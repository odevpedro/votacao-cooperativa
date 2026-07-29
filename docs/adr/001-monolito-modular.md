# ADR 001 — Monólito modular

Status: aceito.

## Contexto

O domínio e o volume operacional do desafio cabem em uma única aplicação.

## Decisão

Usar um processo Spring Boot organizado por funcionalidade e com limites internos.

## Consequências

Positivas: deploy, transação, testes e observabilidade simples. Negativas: módulos
compartilham processo e banco; crescimento exige disciplina.

## Alternativas rejeitadas

Microsserviços e mensageria, por custo operacional sem requisito correspondente.

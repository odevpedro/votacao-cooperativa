# ADR 004 — Sessão controlada por timestamps

Status: aceito.

## Contexto

Um job de fechamento pode atrasar ou falhar, divergindo do horário real.

## Decisão

Persistir `opened_at`/`closes_at` e calcular o estado com `Clock` em toda operação.

## Consequências

Positivas: regra determinística, sem scheduler, fácil de testar. Negativas:
consultas precisam calcular o estado e os relógios dos nós devem estar
sincronizados.

## Alternativas rejeitadas

Coluna de estado atualizada por scheduler e expiração em cache.

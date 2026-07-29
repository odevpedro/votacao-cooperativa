# Testes de performance

## Objetivo e cenário

`performance/voting.js` cria uma pauta, abre sessão, envia votos com associados
únicos e consulta o resultado. `MODE=smoke` usa carga curta; `MODE=load` aumenta
VUs e duração. A elegibilidade deve ser desabilitada para medir a aplicação, não
um terceiro.

```bash
docker run --rm --network host \
  -e BASE_URL=http://localhost:8080 \
  -e MODE=smoke \
  -v "$PWD/performance:/scripts" \
  grafana/k6 run --summary-trend-stats 'avg,min,med,max,p(90),p(95),p(99)' \
  /scripts/voting.js
```

## Resultado medido

Medição local em 28/07/2026, com aplicação e PostgreSQL 17 executados pelo
Docker Compose no mesmo host. A elegibilidade externa estava desabilitada.
O smoke usou 2 VUs e 10 iterações. A carga sustentada usou 50 VUs por 15
segundos; todos os thresholds foram aprovados.

| Métrica | Smoke | Load |
|---|---:|---:|
| throughput HTTP | 42,65 req/s | 787,53 req/s |
| p95 | 8,95 ms | 29,97 ms |
| p99 | 9,75 ms | 50,51 ms |
| taxa de erro HTTP | 0,00% | 0,00% |
| iterações/votos concluídos | 10 | 11.873 |

Uma amostra durante a carga registrou aplicação em 529,98% de CPU e 413,4 MiB,
PostgreSQL em 102,05% de CPU e 53,39 MiB, e k6 em 42,71% de CPU e 28,9 MiB.
Percentuais acima de 100% representam uso de múltiplos núcleos. É uma amostra
instantânea do host local, não um pico garantido nem uma estimativa para cloud.

## Expectativas e hipóteses

Espera-se que a escrita no PostgreSQL e o pool de conexões sejam os primeiros
limites; isso é hipótese, não medição. O índice composto favorece a constraint e a
contagem usa agregação SQL. Possíveis melhorias, somente após medir, incluem
ajustar Hikari, batch de métricas e capacidade da instância/banco. Cache de
resultado não ajuda enquanto a sessão recebe escritas e não foi adicionado.

# Deploy

## Arquitetura proposta

Render executa o container construído pelo `Dockerfile`; Neon fornece PostgreSQL
persistente com SSL. O Flyway migra o banco ao iniciar. `render.yaml` descreve o
serviço, health check e nomes das variáveis, sem secrets.

## Procedimento

1. Criar um projeto/database no Neon.
2. Montar `jdbc:postgresql://HOST/DATABASE?sslmode=require`.
3. Publicar o repositório no GitHub.
4. Criar um Blueprint/Web Service Render a partir de `render.yaml`.
5. Preencher `SPRING_DATASOURCE_URL`, usuário, senha e URL da elegibilidade.
6. Após o primeiro deploy, definir `PUBLIC_BASE_URL` com a URL HTTPS Render.
7. Confirmar `/actuator/health` e `/swagger-ui/index.html`.
8. Executar a collection com o ambiente cloud e `scripts/demo.sh URL`.
9. Fazer redeploy e confirmar que os registros continuam no Neon.

Secrets nunca devem ser colocados no repositório. `PORT` é lida automaticamente e
o servidor escuta `0.0.0.0`. Para instância pequena, começar com
`JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=70`.

## Smoke test

Validar health, criação de pauta, sessão, tela mobile, voto, duplicidade `409`,
resultado, voto fora da janela, Swagger HTTPS, URLs mobile públicas e persistência
após restart. Registrar no README a data e o commit/tag efetivamente publicados.

## Atualização e rollback

Cada imagem é reproduzível pelo commit. Render pode redeployar um commit anterior.
Migrations aplicadas devem ser compatíveis com a versão anterior; uma migration
destrutiva exige plano próprio e backup. Nesta versão há apenas `V1`.

## Limitações

O Render gratuito pode suspender o serviço e causar cold start. O filesystem é
efêmero. Neon/Render podem alterar limites. A configuração é adequada a avaliação,
não a produção de alta disponibilidade.

## Troubleshooting

- falha de startup: conferir URL JDBC, SSL, credenciais e logs Flyway;
- health `DOWN`: conferir conectividade e limite de conexões;
- links mobile errados: remover barra final e corrigir `PUBLIC_BASE_URL`;
- `503` em voto: conferir serviço e timeouts de elegibilidade;
- memória: reduzir pool Hikari ou ajustar `MaxRAMPercentage`.

## Remoção e contingência

Ao fim da avaliação, excluir o Web Service e o projeto/branch Neon para interromper
uso. Como contingência, `docker compose up --build` executa aplicação e banco
localmente com as mesmas migrations.

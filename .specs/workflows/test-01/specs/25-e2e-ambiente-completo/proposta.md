# Testes e2e do ambiente

Etapa 25 de 31 · Fase 4 · derivada da `SPEC-30` (`.specs/04-integration/SPEC-30-e2e-ambiente-completo.md`). Referências: `TEST_STRATEGY.md` §2 e `compose.yaml`. Escrita antes da entrega da etapa 24; será revisada contra o que ela entregou.

## Objetivo
Validar os fluxos completos pela interface real (navegador → Nginx → API → PostgreSQL) com Playwright sobre o `docker compose`, cobrindo login e os fluxos principais. **Sem alterar regras de negócio.**

## Ponto de partida (conferido no código antes da etapa 24)
- `compose.yaml` na raiz tem `postgres`, `api` (healthcheck de readiness) e `app` (Nginx, `/healthz`, porta `APP_PORT`, padrão 4200). `JWT_SECRET` é obrigatório; `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA` são repassados à API.
- A seed (`db/seed/V1000__seed_local.sql`: 1234 = F, idades 10/20; 4567 = M/F, idades 20/30; 6789 = M/F, idade 10) só é carregada no perfil `local` (`application-local.yml`). O compose atual **não ativa esse perfil** (usa `APP_ENV=local-docker`); portanto a seed ainda não chega ao ambiente do Compose.
- `app-autorizador-processo/package.json` não tem Playwright nem script `e2e`; há `scripts/gerar-api-types.mjs`. Não existe pasta `e2e/`.
- O guard de autenticação redireciona para `/login?retorno=<destino>`; a verificação contra o Compose foi adiada para esta etapa pelas etapas 20–23.
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Playwright (devDependency nova, justificada pela spec) em `app-autorizador-processo/`, projetos Chromium desktop e móvel, URL base `http://localhost:${APP_PORT:-4200}`, script `npm run e2e`.
2. Ambiente de teste: projeto Compose `e2e` com `.env` descartável (usuário *bootstrap*, `JWT_SECRET`, TTL curto), volume descartável, espera por `healthy` e `down -v` no final (global setup/teardown).
3. Seed do e2e disponibilizada ao Compose **sem alterar regra de negócio** (ver decisões).
4. Cenários 0 a 9 da SPEC-30, independentes entre si, com números únicos; os de seed leem dados da seed sem mutá-los.
5. Relatório HTML e JUnit e capturas em falha, fora do git (`.gitignore`).
6. Documentação de execução local (`npm run e2e`) no README do app.

## Fora do escopo
- Qualquer mudança em código de negócio, contrato, telas ou textos (falhas achadas viram registro, não correção silenciosa).
- Paridade com o baseline (fica para a etapa 26/SPEC-31), CI (SPEC-40), segurança e desempenho (SPEC-41/42).

## Critérios de aceitação
- `npm run e2e` passa os 10 cenários (0–9) em desktop e móvel contra o Compose limpo.
- Teardown remove contêineres e volume do projeto `e2e`; nenhum processo fica rodando.
- Relatório e capturas ficam fora do git; lint, build e testes unitários do app continuam verdes.
- Se o Docker estiver indisponível, bloqueio registrado e tarefas dependentes não marcadas.

## Decisões tomadas sem consulta
- **Seed no Compose via override de teste:** criar `compose.e2e.yaml` (na raiz ou em `app-autorizador-processo/e2e/`) que apenas define `SPRING_PROFILES_ACTIVE=local` na API, em vez de editar o `compose.yaml` principal. Alternativa descartada: mudar o perfil padrão do Compose. **[REVISAR]** se a etapa 24 ou o plano previr outro mecanismo de seed.
- **Playwright como devDependency** e instalação de Chromium: autorizadas pela própria SPEC-30; não há outra ferramenta no projeto.
- **Cenário 7 (API parada)** usa `docker compose stop api`/`start api` e espera por `healthy`; **cenário 6** força `?numero=` inexistente após login.
- **Cenário de expiração** usa `JWT_TTL_MINUTOS=1` no `.env` de teste e espera/avança o relógio do navegador (`page.clock`) em vez de dormir 60 s, se a API aceitar; caso contrário, aceita-se espera real isolada em um teste. Se TTL mínimo de 1 minuto for inviável, registrar.
- **Textos esperados** são lidos do catálogo entregue na etapa 24 (`core/i18n/mensagens.ts`) ou de seletores por papel/rótulo, para não duplicar texto frágil.
- **Pastas de relatório** (`playwright-report/`, `test-results/`) entram no `.gitignore` do app.

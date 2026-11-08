# Testes e2e do ambiente

Etapa 25 de 31 · Fase 4 · derivada da `SPEC-30` (`.specs/04-integration/SPEC-30-e2e-ambiente-completo.md`). Referências: `TEST_STRATEGY.md` §2 e `compose.yaml`. Revisada contra o código atual (etapa 24 entregue: catálogo `core/i18n/mensagens.ts` existe).

## Objetivo
Validar os fluxos completos pela interface real (navegador → Nginx → API → PostgreSQL) com Playwright sobre o `docker compose`, cobrindo login e os fluxos principais. **Sem alterar regras de negócio.**

## Ponto de partida (conferido no código antes da etapa 24)
- `compose.yaml` na raiz tem `postgres`, `api` (healthcheck de readiness) e `app` (Nginx, `/healthz`, porta `APP_PORT`, padrão 4200). `JWT_SECRET` é obrigatório; `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA` são repassados à API.
- A seed (`db/seed/V1000__seed_local.sql`: 1234 = F, idades 10/20; 4567 = M/F, idades 20/30; 6789 = M/F, idade 10) é carregada pelo `application-local.yml` (`flyway.locations` inclui `db/seed`). Conferido em `application.yml`: `spring.profiles.default: local`, e o `compose.yaml` não define `SPRING_PROFILES_ACTIVE`; logo o perfil `local` já é o ativo na API do Compose e **a seed já chega ao ambiente**. A premissa anterior (seed ausente) estava errada.
- `app-autorizador-processo/package.json` não tem Playwright nem script `e2e`; há `scripts/gerar-api-types.mjs`. Não existe pasta `e2e/`.
- O guard de autenticação redireciona para `/login?retorno=<destino>`; a verificação contra o Compose foi adiada para esta etapa pelas etapas 20–23.
- `.specs/06-execution-plan/` não existe; desvio a registrar na evidência.

## Escopo
1. Playwright (devDependency nova, justificada pela spec) em `app-autorizador-processo/`, projetos Chromium desktop e móvel, URL base `http://localhost:${APP_PORT:-4200}`, script `npm run e2e`.
2. Ambiente de teste: projeto Compose `e2e` com `.env` descartável (usuário *bootstrap*, `JWT_SECRET`, TTL curto), volume descartável, espera por `healthy` e `down -v` no final (global setup/teardown).
3. Seed do e2e: usar a que o perfil `local` já carrega no Compose; o setup valida (via login e leitura do painel) que ela está presente, sem alterar o `compose.yaml` nem regra de negócio.
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
- **Sem override de Compose para a seed:** como o perfil `local` é o padrão da API, não se cria `compose.e2e.yaml`. Se a execução provar o contrário, criar um override mínimo que só defina `SPRING_PROFILES_ACTIVE=local`. **[REVISAR]** se a equipe preferir um perfil explícito `e2e`.
- **Playwright como devDependency** e instalação de Chromium: autorizadas pela própria SPEC-30; não há outra ferramenta no projeto.
- **Cenário 7 (API parada)** usa `docker compose stop api`/`start api` e espera por `healthy`; **cenário 6** força `?numero=` inexistente após login.
- **Cenário de expiração** usa `JWT_TTL_MINUTOS=1` no `.env` de teste e espera/avança o relógio do navegador (`page.clock`) em vez de dormir 60 s, se a API aceitar; caso contrário, aceita-se espera real isolada em um teste. Se TTL mínimo de 1 minuto for inviável, registrar.
- **Textos esperados** são lidos do catálogo entregue na etapa 24 (`core/i18n/mensagens.ts`) ou de seletores por papel/rótulo, para não duplicar texto frágil.
- **Pastas de relatório** (`playwright-report/`, `test-results/`) entram no `.gitignore` do app.

## Registro do lote 1 (Playwright e ambiente)
- **Desvio `06-execution-plan`:** a pasta citada no índice não existe; ignorada.
- **Estado conferido:** `compose.yaml` com `postgres`/`api`/`app` e healthchecks; `/login` como destino sem sessão; sem `@playwright/test` antes do lote.
- **Docker Compose:** a máquina só tem o binário independente `docker-compose` v5.6.1 (o plugin `docker compose` não existe). `e2e/ambiente.ts` usa o plugin se houver e cai para `docker-compose`. Mesmo comportamento, projeto `-p e2e`.
- **Seed (2.2):** nenhum `compose.e2e.yaml` criado; perfil `local` é o padrão. A presença dos 3 procedimentos será validada pelo cenário 1 (lote seguinte).
- **`@types/node`** adicionado como devDependency (tipos para `e2e/`, que tem `tsconfig.json` próprio; o lint do Angular só cobre `src/`).
- **Portas do e2e:** API 18080 e PostgreSQL 15432 (sobrescrevíveis por `E2E_API_PORT`/`E2E_POSTGRES_PORT`) para não colidir com o ambiente de desenvolvimento; app em `APP_PORT` (padrão 4200).
- **`.env` descartável:** gerado em `e2e/.tmp/e2e.env` (ignorado pelo git; JWT_SECRET, senha do bootstrap e do banco sorteados por execução; TTL 1 min), passado por `--env-file`; removido no teardown. Setup que falha faz `down -v` antes de relançar o erro.
- **Verificação:** `e2e/fumaca.e2e.ts` (teste mínimo, mantido até os cenários chegarem) passou em desktop contra o Compose limpo; teardown removeu contêineres, rede e volume (0 recursos com o rótulo do projeto `e2e`).

## Registro do lote 3 (helpers e cenários 0–5)
- **Arquivos:** `e2e/helpers.ts` (login pela UI, `numeroUnico`, leitura do resumo da seed/painel pela API), `01-autenticacao`, `02-painel`, `03-procedimentos`, `04-autorizacoes` (`*.e2e.ts`). O cenário 6 fica para o lote seguinte.
- **Seletores:** os rótulos dos campos obrigatórios levam `*` (ex.: "Senha *"), então `getByLabel(..., {exact:true})` não casa; usa-se `getByRole('textbox'|'combobox', {name, exact:true})`.
- **Cenário 1 (seed):** os projetos rodam em série no mesmo banco; só o projeto `desktop` exige os totais exatos da seed (3/0/3). O móvel confere que a tela espelha a API.
- **Cenário 0b:** espera real de ~60 s (TTL 1 min) em um único teste, sem `page.clock`.
- **Setup:** passou a fazer `down -v` antes do `up`, porque restos de execução interrompida (volume com outra senha do banco) deixavam a API `unhealthy`.
- **Verificação:** `npx playwright test --project=desktop` → 11 testes passam (9 na execução completa; 2 do cenário 4/5 após corrigir o seletor do combobox). Execução no projeto móvel fica para a tarefa 6.1.

## Registro do lote 4 (cenários 6–9 e verificação)
- **Arquivo:** `e2e/05-resiliencia.e2e.ts` (cenários 6, 7, 8 e 9).
- **Cenário 6:** o produto ignora em silêncio um `?numero=` que não está na lista (não há erro de API nem diálogo). O "erro tratado" verificado é: nada pré-selecionado ("Selecione"), envio sem escolha mostra "Escolha um procedimento." e o painel de resultado continua no estado inicial. **[REVISAR]** se a equipe esperava aviso explícito para número forjado (mudança de produto, fora desta etapa).
- **Cenário 7:** o título "Não foi possível carregar" nas listas é um estado vazio (`app-empty-state`) com "Tentar novamente", não um modal (o `ErrorDialog` só é usado em login/cadastro). O teste valida esse texto com `docker compose stop api`, religa com `up -d --wait api` num `finally` e confirma a retomada via "Tentar novamente" (com repetição enquanto o Nginx reencontra a API).
- **Cenário 9:** 320 px em `/painel`, `/procedimentos`, `/solicitar` e `/autorizacoes`, comparando `scrollWidth` e `clientWidth` do documento.
- **Verificação (6.1/6.2):** `npm run e2e` → 30 testes passam (desktop e móvel, 4,2 min), sem falhas de produto. Depois do teardown: 0 contêineres, volumes e redes do projeto `e2e`, e `e2e/.tmp` removido.
- **6.3:** seção "Testes e2e (Playwright)" no README do app.
- **6.4:** `npm run lint` sem erros, `npm run build` ok, `npm test` 226 testes passam.
- Falta a tarefa 6.5 (evidência final e arquivamento), fora deste lote.

## Registro do lote 5
- 6.5: evidência por tarefa e resumo em `tarefas.md`; SPEC-30 movida com `git mv` para `.specs/archive/`. Sem bloqueios.

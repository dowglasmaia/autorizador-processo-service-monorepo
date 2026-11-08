# CI por projeto

Etapa 28 de 31 · Fase 4 · derivada da `SPEC-40` (`.specs/05-quality/SPEC-40-ci-pipeline.md`), com referências às SPEC-10 e SPEC-13 (Dockerfiles), SPEC-30 (e2e), SPEC-31 (paridade) e SPEC-32 (desvio de contrato). Revisada contra o que a etapa 27 entregou.

## Objetivo
Dar a cada projeto seu próprio workflow de GitHub Actions, disparado só por mudança no próprio caminho, mais um workflow de integração que valida a combinação. Mudou só o frontend → o CI do backend não roda, e vice-versa. Sem deploy e sem publicação de imagens.

## Ponto de partida (conferido após a etapa 27)
- Não existe `.github/` no repositório. `.specs/06-execution-plan/` também não existe (desvio a registrar na evidência).
- Existem `api-autorizador-processo/Dockerfile` e `app-autorizador-processo/Dockerfile` (um por projeto), `compose.yaml` e `README.md` na raiz, e `tools/parity/` (`replay.mjs`, `cases.json`, `RELATORIO.md`).
- Backend: Maven Wrapper; `ArchitectureTest` roda dentro de `./mvnw verify`. **Não existem** `check-architecture.mjs` nem `validate.mjs` citados na SPEC-40; não há `api-autorizador-processo/scripts`.
- Frontend: scripts `lint` (`ng lint`), `test` (`ng test --no-watch`), `build` (`ng build`), `api:types` e `e2e` (Playwright, em `app-autorizador-processo/e2e`, que sobe o Compose com `.env` descartável).
- A etapa 27 (SPEC-32) entregou `scripts/check-contract.mjs` (tipos × OpenAPI; usa o gerador do app, então exige `npm ci` no app) e `scripts/check-contract-compat.mjs` (compatibilidade com o instantâneo `tools/contract/baseline/`, usa `js-yaml` transitivo do app). O README raiz diz que a execução no CI "fica para a etapa de CI por projeto" e marca `.github/workflows` como "em construção".
- O e2e (`npm run e2e`) sobe e derruba o Compose sozinho (`global-setup.ts`) com `.env` descartável gerado; o CI só precisa de Node, Docker e `npx playwright install`.

## Escopo
1. `.github/workflows/backend-ci.yml`: `paths` = `api-autorizador-processo/**`, `tools/parity/**` e o próprio arquivo. Jobs: `verify` (JDK 25, cache Maven, `./mvnw verify` com Testcontainers, incluindo `ArchitectureTest` e a geração do contrato), build da imagem Docker do backend sem publicar, `paridade` (Compose com `postgres` + `api`, `replay.mjs --target v1`) e auditoria de dependências Maven.
2. `.github/workflows/frontend-ci.yml`: `paths` = `app-autorizador-processo/**` e o próprio arquivo. Jobs: `npm ci`, `lint`, `test`, `build`, build da imagem Docker do frontend sem publicar e `npm audit`.
3. `.github/workflows/integracao-ci.yml`: `paths` = `compose.yaml`, `.env.example`, `scripts/check-contract*.mjs`, `tools/contract/**`, os caminhos de ambos os projetos e o próprio arquivo. Jobs: `check-contract` (roda `check-contract.mjs` e `check-contract-compat.mjs`, após `npm ci` no app) e `e2e` (Compose completo + Playwright, relatório como artefato só em falha).
4. Em todos: `concurrency` cancelando execuções antigas do mesmo ref, `permissions: contents: read`, `workflow_dispatch`, cache de Maven e npm, nenhum segredo real (o `JWT_SECRET` e as credenciais de e2e/paridade são gerados em tempo de execução).
5. README raiz: badges por workflow, tabela "o que roda quando" e orientação sobre branch protection (checks com filtro de caminho ficam pendentes; usar job final que sempre roda ou não marcar como obrigatório).
6. Prova do isolamento: validar a lógica de `paths` e a sintaxe localmente; a execução real no GitHub fica **pendente**, registrada como tal.

## Fora do escopo
- Deploy, publicação de imagens ou artefatos, infraestrutura como código.
- Mudar código de produção, contrato OpenAPI ou testes existentes além do necessário para o CI rodar.
- Criar ou alterar os scripts `check-contract*.mjs` (etapa 27) ou novas verificações não listadas na SPEC-40.
- SPEC-41 e SPEC-43 (sucessoras): ficam para suas etapas.

## Critérios de aceitação
- Os 3 workflows existem, passam em `actionlint` (ou equivalente) e não referenciam arquivos do outro projeto nos `paths` de backend/frontend (exceto `tools/parity/**`).
- Mudança só em `app-autorizador-processo/src/**` → apenas `frontend-ci` (+ `integracao-ci`); só em `api-autorizador-processo/src/**` → apenas `backend-ci` (+ `integracao-ci`); só em `compose.yaml` → apenas `integracao-ci`.
- Cada Dockerfile é construído pelo seu CI; nada é publicado; nenhum segredo real versionado.
- Os comandos de cada job rodam localmente (`./mvnw verify`, `npm ci/lint/test/build`, `docker build`) sem falha, ou o bloqueio é registrado.
- README documenta badges, tabela de disparo e branch protection.

## Decisões tomadas sem consulta
- **Scripts inexistentes:** `check-architecture.mjs` e `validate.mjs` não existem; a arquitetura é coberta pelo `ArchitectureTest` dentro do `./mvnw verify` e a validação do OpenAPI pela geração de código do build. Não se cria script novo; o job `verify` é nomeado de forma a deixar isso explícito. **[REVISAR]**
- **Ferramenta de lint de workflow:** `actionlint` via Docker (`rhysd/actionlint`) se disponível; senão, validação de YAML e dos `paths` por script local, registrando a limitação.
- **Versões de ações:** `actions/checkout`, `setup-java` (Temurin 25), `setup-node` (Node 24) e `cache` em versões major estáveis, fixadas por tag major.
- **Auditoria de dependências:** `npm audit --audit-level=high --omit=dev` e, no Maven, `./mvnw dependency:tree` + verificação com plugin já presente; sem adicionar plugin novo, a auditoria Maven apenas lista dependências e fica **não bloqueante** (`continue-on-error`), registrada como limitação. **[REVISAR]**
- **Job `check-contract`:** os scripts existem (etapa 27); o job roda os dois, o de desvio e o de compatibilidade, e `tools/contract/**` entra nos `paths` da integração. O README raiz é atualizado para refletir que a execução no CI passa a existir.
- **Docker/Compose:** usa `docker compose` no runner; localmente aceita-se `docker-compose` na falta do plugin.
- **Execução real no GitHub** não é possível aqui: marcada como pendente, conforme `como-executar.md` §3.

## Decisões do lote 1 (workflows de backend e frontend)
- `.specs/06-execution-plan/` não existe (desvio registrado); `check-architecture.mjs`/`validate.mjs` também não: o job `verify` cobre `ArchitectureTest` e a geração do contrato via `./mvnw verify`.
- `backend-ci.yml`: jobs `verify`, `docker-build`, `auditoria-maven` (`continue-on-error`, só `dependency:tree`) e `paridade` (senhas/JWT gerados com `openssl` em tempo de execução e mascarados; `up --wait postgres api`, `replay.mjs --target v1`, `down -v` sempre). Portas 18080/15432 para evitar conflito no runner. **[REVISAR]** a senha de bootstrap gerada assume que a política de senha aceita hex + `Aa1!`; não validado em execução real no GitHub.
- `frontend-ci.yml`: jobs `instalar`, `lint`, `test`, `build`, cada um com seu `npm ci` e cache npm (Node 24), em paralelo. Os jobs de imagem Docker e `npm audit` (tarefa 3.3) ficam para o próximo lote.
- Ações fixadas por tag major (`checkout@v4`, `setup-java@v4`, `setup-node@v4`, `setup-buildx-action@v3`). Sintaxe validada apenas como YAML; `actionlint` fica na tarefa 6.1.

## Decisões do lote 2 e evidências
- **3.3** `frontend-ci.yml` ganhou `docker-build` (sem push) e `auditoria-npm` (`npm audit --audit-level=high --omit=dev`, bloqueante). Local: `npm audit` = 0 vulnerabilidades.
- **4.1/4.2** `integracao-ci.yml` criado: `paths` iguais em `push`/`pull_request`; jobs `check-contract` (`npm ci` no app, depois os dois scripts) e `e2e` (`npx playwright install --with-deps chromium` + `npm run e2e`; o global setup gera o `.env` descartável e sobe/derruba o Compose). Artefato `playwright-report` + `test-results` enviado só `if: failure()`.
- **5.1** README raiz: badges (URLs relativas), seção "CI por projeto" com tabela de disparo e orientação de branch protection; removidos os "em construção" de `tools/parity` e `.github/workflows` e a frase "fica para a etapa de CI".
- **6.1** `actionlint` (imagem Docker `rhysd/actionlint`) nos 3 workflows: sem erros (exit 0).
- **6.2** Script local (js-yaml do app, glob → regex) confirmou: `push` = `pull_request` em cada workflow; backend não cita o app; frontend não cita api/tools; cenários de aceite: só `app-…/src` → frontend + integração; só `api-…/src` → backend + integração; só `compose.yaml` → integração. **Execução real no GitHub: pendente** (não há como disparar aqui).
- **6.3** Local, sem falhas: `./mvnw verify` (239 testes, `ArchitectureTest` 23 ok, BUILD SUCCESS), `npm ci`, `npm run lint`, `npm test` (226 testes), `npm run build`, `check-contract.mjs`, `check-contract-compat.mjs`, `docker build` das duas imagens (imagens locais removidas depois). O job `paridade` e o `e2e` (Compose completo) não foram reexecutados nesta sessão; já foram validados nas etapas 25/26 e o e2e depende do runner do GitHub. **[REVISAR]** primeira execução real no GitHub.
- **6.4** Spec `SPEC-40` movida com `git mv` para `.specs/archive/`. Desvio: `.specs/06-execution-plan/` não existe; `check-architecture.mjs`/`validate.mjs` não existem (cobertos pelo `./mvnw verify`).
- Resumo: 3 workflows independentes entregues; Dockerfiles construídos por cada CI, sem publicação; sem segredos versionados.

# CI por projeto

Etapa 28 de 31 · Fase 4 · derivada da `SPEC-40` (`.specs/05-quality/SPEC-40-ci-pipeline.md`), com referências às SPEC-10 e SPEC-13 (Dockerfiles), SPEC-30 (e2e), SPEC-31 (paridade) e SPEC-32 (desvio de contrato). Escrita antecipadamente, antes da entrega da etapa 27; será revisada contra o que ela entregar.

## Objetivo
Dar a cada projeto seu próprio workflow de GitHub Actions, disparado só por mudança no próprio caminho, mais um workflow de integração que valida a combinação. Mudou só o frontend → o CI do backend não roda, e vice-versa. Sem deploy e sem publicação de imagens.

## Ponto de partida (conferido antes da etapa 27)
- Não existe `.github/` no repositório. `.specs/06-execution-plan/` também não existe (desvio a registrar na evidência).
- Existem `api-autorizador-processo/Dockerfile` e `app-autorizador-processo/Dockerfile` (um por projeto), `compose.yaml` e `README.md` na raiz, e `tools/parity/` (`replay.mjs`, `cases.json`, `RELATORIO.md`).
- Backend: Maven Wrapper; `ArchitectureTest` roda dentro de `./mvnw verify`. **Não existem** `check-architecture.mjs` nem `validate.mjs` citados na SPEC-40; não há `api-autorizador-processo/scripts`.
- Frontend: scripts `lint` (`ng lint`), `test` (`ng test --no-watch`), `build` (`ng build`), `api:types` e `e2e` (Playwright, em `app-autorizador-processo/e2e`, que sobe o Compose com `.env` descartável).
- `scripts/check-contract.mjs` é entregue pela etapa 27 (SPEC-32) e ainda não existe; o job `check-contract` depende dele.

## Escopo
1. `.github/workflows/backend-ci.yml`: `paths` = `api-autorizador-processo/**`, `tools/parity/**` e o próprio arquivo. Jobs: `verify` (JDK 25, cache Maven, `./mvnw verify` com Testcontainers, incluindo `ArchitectureTest` e a geração do contrato), build da imagem Docker do backend sem publicar, `paridade` (Compose com `postgres` + `api`, `replay.mjs --target v1`) e auditoria de dependências Maven.
2. `.github/workflows/frontend-ci.yml`: `paths` = `app-autorizador-processo/**` e o próprio arquivo. Jobs: `npm ci`, `lint`, `test`, `build`, build da imagem Docker do frontend sem publicar e `npm audit`.
3. `.github/workflows/integracao-ci.yml`: `paths` = `compose.yaml`, `.env.example`, `scripts/check-contract.mjs`, os caminhos de ambos os projetos e o próprio arquivo. Jobs: `check-contract` e `e2e` (Compose completo + Playwright, relatório como artefato só em falha).
4. Em todos: `concurrency` cancelando execuções antigas do mesmo ref, `permissions: contents: read`, `workflow_dispatch`, cache de Maven e npm, nenhum segredo real (o `JWT_SECRET` e as credenciais de e2e/paridade são gerados em tempo de execução).
5. README raiz: badges por workflow, tabela "o que roda quando" e orientação sobre branch protection (checks com filtro de caminho ficam pendentes; usar job final que sempre roda ou não marcar como obrigatório).
6. Prova do isolamento: validar a lógica de `paths` e a sintaxe localmente; a execução real no GitHub fica **pendente**, registrada como tal.

## Fora do escopo
- Deploy, publicação de imagens ou artefatos, infraestrutura como código.
- Mudar código de produção, contrato OpenAPI ou testes existentes além do necessário para o CI rodar.
- Criar o `check-contract.mjs` (etapa 27) ou novas verificações não listadas na SPEC-40.
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
- **Job `check-contract`:** depende da etapa 27; se o script não existir na revisão, o job é incluído condicionado à entrega e a tarefa é bloqueada, sem inventar o script.
- **Docker/Compose:** usa `docker compose` no runner; localmente aceita-se `docker-compose` na falta do plugin.
- **Execução real no GitHub** não é possível aqui: marcada como pendente, conforme `como-executar.md` §3.

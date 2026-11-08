# SPEC-40 — CI independente por projeto (backend e frontend)

| Campo | Valor |
|---|---|
| Domínio / projeto | Qualidade · `.github/workflows` |
| Prioridade | P2 |
| Depende de | SPEC-10, SPEC-13, SPEC-30, SPEC-31, SPEC-32 |
| Sucessoras | SPEC-41, SPEC-43 |
| Paralelismo | junto com SPEC-42 |

## 1. Objetivo
Cada projeto tem **seu próprio Dockerfile** (backend: SPEC-10; frontend: SPEC-13) e **seu próprio workflow de CI**, que **só roda quando há mudança naquele projeto**. Mudou só o frontend → o CI do backend **não** executa, e vice-versa. O repositório usa GitHub (links do autor no README) → GitHub Actions.

**Workflows (arquivos separados):**
| Arquivo | Disparo (`paths`) | Jobs |
|---|---|---|
| `backend-ci.yml` | `api-autorizador-processo/**`, `tools/parity/**`, `.github/workflows/backend-ci.yml` | build+testes (`./mvnw verify`, Testcontainers), `check-architecture`, validação OpenAPI, **build da imagem Docker do backend** (sem publicar), **paridade** (`tools/parity` contra a API), auditoria de dependências Maven |
| `frontend-ci.yml` | `app-autorizador-processo/**`, `.github/workflows/frontend-ci.yml` | `npm ci`, `lint`, `test`, `build` de produção, **build da imagem Docker do frontend** (sem publicar), auditoria npm |
| `integracao-ci.yml` | `compose.yaml`, `.env.example`, e os `paths` de **ambos** os projetos, `scripts/check-contract.mjs` | `check-contract` (tipos × OpenAPI) e `e2e` (Compose completo + Playwright) |

Regra: os dois primeiros **nunca** referenciam arquivos do outro projeto nos seus `paths` (exceto `tools/parity/**` no backend e o contrato OpenAPI, que fica dentro do backend). O fluxo de integração é a única verificação cruzada, e só dispara quando algo de back **ou** front **ou** do Compose mudou; ele é o que garante que a combinação funciona. Para que esse caso não bloqueie PRs sem relação, o `integracao-ci.yml` é um check separado (não faz parte do CI de cada projeto).

**Restrições:** sem segredos **reais** em workflow (o `JWT_SECRET` de CI/e2e é um valor descartável gerado em tempo de execução, nunca reaproveitado); cache de Maven e npm; `concurrency` cancelando execuções antigas do mesmo ref; permissões mínimas (`contents: read`); **não publicar imagens nem artefatos** (fora de escopo); `workflow_dispatch` habilitado em todos para execução manual; **atenção a branch protection:** checks obrigatórios com filtro de caminho não rodam e ficam "pendentes" — se o responsável ativar proteção, usar a técnica de job final que sempre roda ou não marcar o check como obrigatório (registrar a orientação no README).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `TEST_STRATEGY.md`, `SECURITY_REQUIREMENTS.md` SEC-09, `MONOREPO_STRUCTURE.md`, SPEC-10 e SPEC-13 (Dockerfiles)

## 3. Tarefas
- [ ] **T1.** `backend-ci.yml` conforme tabela: JDK 25, `./mvnw verify`, `check-architecture.mjs`, `validate.mjs`, `docker build api-autorizador-processo`, job `paridade` (sobe `postgres`+`api` e roda `replay.mjs --target v1`), auditoria de dependências.
- [ ] **T2.** `frontend-ci.yml`: Node suportado pelo Angular 21, `npm ci`, `npm run lint`, `npm test`, `npm run build`, `docker build app-autorizador-processo`, auditoria npm.
- [ ] **T3.** `integracao-ci.yml`: `check-contract.mjs`; job `e2e` com Compose + Playwright (artefato de relatório apenas em falha).
- [ ] **T4.** Filtros `paths` e `concurrency` em cada workflow; `workflow_dispatch`.
- [ ] **T5.** README raiz: badges por workflow, tabela "o que roda quando", orientação sobre branch protection.
- [ ] **T6.** **Prova do isolamento:** abrir PR/commit de teste só com mudança em `app-autorizador-processo/` (ex.: comentário em `README.md` do front) e confirmar que **apenas** `frontend-ci` (+ `integracao-ci`) executam; repetir só com `api-autorizador-processo/`; registrar links/prints das execuções. Sem acesso ao GitHub, validar a lógica de `paths` com `act`/`gh workflow` simulando eventos e marcar a execução real como **pendente** (`como-executar.md` §3).

**Aceite (G/W/T):** *Given* um commit que altera somente `app-autorizador-processo/src/**` · *Then* `backend-ci` **não** é disparado e `frontend-ci` passa. *Given* um commit só em `api-autorizador-processo/src/**` · *Then* `frontend-ci` **não** é disparado e `backend-ci` passa. *Given* mudança em `compose.yaml` · *Then* só `integracao-ci` roda.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | GitHub Actions; `java-spring-scaffold` e `angular-frontend-scaffold` (comandos canônicos) |

## 5. Verificação obrigatória
- Sintaxe: `actionlint` (ou equivalente) nos 3 arquivos → sem erros
- Prova do isolamento (T6) com execuções reais no GitHub, registrando os links
- Cada Dockerfile construído pelo seu CI (`docker build` verde)
- Definition of Done: 3 workflows versionados e executados, isolamento demonstrado nos dois sentidos, README documentado.

## 6. Resultado
_Preencher ao concluir._

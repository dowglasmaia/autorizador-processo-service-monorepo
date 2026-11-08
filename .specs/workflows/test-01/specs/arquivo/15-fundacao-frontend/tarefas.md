# Tarefas — Fundação do frontend

## 1. Ponto de partida
- [x] 1.1 Ler `.specs/INDEX.md`, `memory/`, `shared/como-executar.md`, SPEC-14, `TARGET_ARCHITECTURE.md` §3 e `MONOREPO_STRUCTURE.md`; registrar o desvio da pasta `06-execution-plan/` inexistente.
  > ✅ 2026-11-08 17:40 +00:00 — Leitura feita (INDEX, memory, como-executar, SPEC-14, arquitetura/monorepo). Desvio: `.specs/06-execution-plan/` não existe (`ls` confirma); ignorada.
- [x] 1.2 Confirmar Node ≥ 20.19 e que `app-autorizador-processo/` não existe; registrar os comandos.
  > ✅ 2026-11-08 17:40 +00:00 — `node -v` = v24.14.0 (≥ 20.19); raiz sem `app-autorizador-processo/` antes do scaffold.

## 2. Scaffold
- [x] 2.1 Rodar `scaffold.mjs --output <raiz> --project-name app-autorizador-processo` (sem `--force`, sem `ng new` manual).
  > ✅ 2026-11-08 17:40 +00:00 — `scaffold.mjs --output . --project-name app-autorizador-processo` da skill concluído (inclui `npm install`); sem `--force`.
- [x] 2.2 Rodar `validate.mjs app-autorizador-processo` (build de produção) e conferir `strict`, zoneless, porta 4200 explícita em `npm start`, `.postcssrc.json` e `doc.md`.
  > ✅ 2026-11-08 17:40 +00:00 — `validate.mjs app-autorizador-processo` → build de produção OK. Conferidos: `strict` e `strictTemplates` true, zoneless, `start` = `ng serve --port 4200`, `.postcssrc.json` com `@tailwindcss/postcss`, `features/home/doc.md` presente.

## 3. Testes e qualidade
- [x] 3.1 Confirmar na documentação do CLI 21 o executor de testes suportado e configurá-lo (`test` em modo de execução única).
  > ✅ 2026-11-08 17:40 +00:00 — CLI 21 suporta `@angular/build:unit-test` com runner Vitest (`vitest ^4`); target `test` criado, `npm test` = `ng test --no-watch`; devDeps `vitest` e `jsdom`.
- [x] 3.2 Criar teste mínimo do shell (componente raiz e rota inicial renderizam).
  > ✅ 2026-11-08 17:40 +00:00 — `src/app/app.spec.ts`: raiz renderiza `router-outlet` e rota `/` renderiza o `h1` da home. `npm test` → 2 testes passam.
- [x] 3.3 Configurar `angular-eslint` e o script `lint`, com regras de `OnPush` e `inject()`.
  > ✅ 2026-11-08 17:40 +00:00 — `ng add @angular-eslint/schematics@21` (angular-eslint 21.4.0); `eslint.config.js` com `prefer-on-push-component-change-detection` e `prefer-inject` como erro; script `lint` = `ng lint`. `npm run lint` passa.
- [x] 3.4 Definir `OnPush` como padrão nos schematics do `angular.json` e conferir `strictTemplates`.
  > ✅ 2026-11-08 17:40 +00:00 — `angular.json`: schematic de componente com `changeDetection: OnPush`; `strictTemplates: true` conferido. `npm run build` OK.

## 4. Ambiente e documentação
- [x] 4.1 Criar `environment.ts` (`/api/v1`) e `environment.development.ts` (`http://localhost:8080/api/v1`) com `fileReplacements`.
  > ✅ 2026-11-08 — `src/environments/environment.ts` e `environment.development.ts`; `fileReplacements` na configuração `development` do `angular.json`.
- [x] 4.2 Fazer a fronteira HTTP do shell ler `apiBaseUrl` do environment, sem emitir requisições.
  > ✅ 2026-11-08 — `API_BASE_URL` agora usa `environment.apiBaseUrl`; teste `api-base-url.token.spec.ts`; nenhuma requisição emitida.
- [x] 4.3 Escrever o `README.md` do frontend (comandos, estrutura, convenções `doc.md`, `OnPush`, Signals).
  > ✅ 2026-11-08 — `README.md` do frontend reescrito (comandos, ambientes, estrutura, convenções).
- [x] 4.4 Conferir `.gitignore`/`README.md` do monorepo quanto a `node_modules` e `dist` do frontend.
  > ✅ 2026-11-08 — `app-autorizador-processo/.gitignore` já ignora `/dist` e `/node_modules` (`git check-ignore`); `README.md` da raiz atualizado (frontend com fundação pronta).

## 5. Verificação e fechamento
- [x] 5.1 Rodar `npm ci`, `npm run lint`, `npm run build` e `npm test`; corrigir falhas.
  > ✅ 2026-11-08 — `npm ci`, `npm run lint`, `npm run build`, `npm test` (3 testes) verdes.
- [x] 5.2 Subir `npm start`, confirmar a porta 4200 e encerrar o processo.
  > ✅ 2026-11-08 — `npm start` → HTTP 200 em `localhost:4200`; processo encerrado (porta sem resposta depois).
- [x] 5.3 Registrar evidência por tarefa, preencher "Resultado" da SPEC-14 e arquivá-la em `.specs/archive/` com `git mv`; atualizar o `INDEX.md`.
  > ✅ 2026-11-08 — SPEC-14 com "Resultado" preenchido e movida com `git mv` para `.specs/archive/`; `INDEX.md` atualizado.

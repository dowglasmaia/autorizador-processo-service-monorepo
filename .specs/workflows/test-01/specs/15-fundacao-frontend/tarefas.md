# Tarefas — Fundação do frontend

## 1. Ponto de partida
- [ ] 1.1 Ler `.specs/INDEX.md`, `memory/`, `shared/como-executar.md`, SPEC-14, `TARGET_ARCHITECTURE.md` §3 e `MONOREPO_STRUCTURE.md`; registrar o desvio da pasta `06-execution-plan/` inexistente.
- [ ] 1.2 Confirmar Node ≥ 20.19 e que `app-autorizador-processo/` não existe; registrar os comandos.

## 2. Scaffold
- [ ] 2.1 Rodar `scaffold.mjs --output <raiz> --project-name app-autorizador-processo` (sem `--force`, sem `ng new` manual).
- [ ] 2.2 Rodar `validate.mjs app-autorizador-processo` (build de produção) e conferir `strict`, zoneless, porta 4200 explícita em `npm start`, `.postcssrc.json` e `doc.md`.

## 3. Testes e qualidade
- [ ] 3.1 Confirmar na documentação do CLI 21 o executor de testes suportado e configurá-lo (`test` em modo de execução única).
- [ ] 3.2 Criar teste mínimo do shell (componente raiz e rota inicial renderizam).
- [ ] 3.3 Configurar `angular-eslint` e o script `lint`, com regras de `OnPush` e `inject()`.
- [ ] 3.4 Definir `OnPush` como padrão nos schematics do `angular.json` e conferir `strictTemplates`.

## 4. Ambiente e documentação
- [ ] 4.1 Criar `environment.ts` (`/api/v1`) e `environment.development.ts` (`http://localhost:8080/api/v1`) com `fileReplacements`.
- [ ] 4.2 Fazer a fronteira HTTP do shell ler `apiBaseUrl` do environment, sem emitir requisições.
- [ ] 4.3 Escrever o `README.md` do frontend (comandos, estrutura, convenções `doc.md`, `OnPush`, Signals).
- [ ] 4.4 Conferir `.gitignore`/`README.md` do monorepo quanto a `node_modules` e `dist` do frontend.

## 5. Verificação e fechamento
- [ ] 5.1 Rodar `npm ci`, `npm run lint`, `npm run build` e `npm test`; corrigir falhas.
- [ ] 5.2 Subir `npm start`, confirmar a porta 4200 e encerrar o processo.
- [ ] 5.3 Registrar evidência por tarefa, preencher "Resultado" da SPEC-14 e arquivá-la em `.specs/archive/` com `git mv`; atualizar o `INDEX.md`.

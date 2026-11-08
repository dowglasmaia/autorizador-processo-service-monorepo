# SPEC-14 — Fundação do frontend (Angular 21)

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `app-autorizador-processo` |
| Prioridade | P0 |
| Depende de | SPEC-01 |
| Sucessoras | SPEC-13, SPEC-15, SPEC-16 |
| Paralelismo | pode rodar junto com SPEC-02 e SPEC-03 |

## 1. Objetivo
Criar o projeto Angular 21 com o gerador canônico da skill (TypeScript estrito, SCSS, Tailwind via PostCSS, standalone, zoneless, porta 4200, shell mínimo funcional) e acrescentar a infraestrutura de testes e qualidade que o gerador deliberadamente não inclui.
**Restrições:** nome do projeto **exatamente** `app-autorizador-processo` (informado nesta especificação); não usar `ng new` manual nem `--force`; sem SSR, sem biblioteca de estado, sem UI kit; sem proxy no `ng serve` (CORS tratado no backend, perfil `local`).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `TARGET_ARCHITECTURE.md` §3, `MONOREPO_STRUCTURE.md`; skill `angular-frontend-scaffold` (`references/scaffold-contract.md`), `angular-architect`

## 3. Tarefas
- [ ] **T1.** Confirmar Node ≥ 20.19 e que `app-autorizador-processo/` não existe.
- [ ] **T2.** `node <skill>/scripts/scaffold.mjs --output <raiz> --project-name app-autorizador-processo`.
- [ ] **T3.** Rodar `validate.mjs` (build de produção). Não executar `ng serve`/testes nesta etapa do gerador, conforme a skill.
- [ ] **T4.** Acrescentar o executor de testes padrão do CLI 21 (verificar na documentação do CLI a ferramenta suportada) com um teste mínimo do shell; scripts `test` e `lint` no `package.json`.
- [ ] **T5.** Configurar `environment.ts`/`environment.development.ts` com `apiBaseUrl` (`/api/v1` em produção; `http://localhost:8080/api/v1` em desenvolvimento).
- [ ] **T6.** `README.md` do frontend (comandos, estrutura, convenções `doc.md` por módulo).
- [ ] **T7.** Garantir `strict`, `strictTemplates`, `OnPush` como padrão nos componentes novos (regra documentada + lint).

**Aceite:** *Given* checkout limpo · *When* `npm ci && npm run build && npm test` · *Then* build de produção e o teste do shell passam; `npm start` serve na porta 4200.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T3 | `angular-frontend-scaffold` (obrigatória) |
| T4–T7 | `angular-architect` |

## 5. Verificação obrigatória
- `node <skill>/scripts/validate.mjs app-autorizador-processo` → sucesso
- `npm run build` e `npm test` em `app-autorizador-processo/` → verdes
- `grep -n "strict" app-autorizador-processo/tsconfig.json` → estrito ativo
- Definition of Done: projeto gerado pelo script oficial, testes executáveis, ambiente configurado, sem dependências além do previsto.

## 6. Resultado
**Status: concluída** em 2026-11-08 (etapa 15 do loop `test-01`).

**Evidência:**
- `scaffold.mjs --output . --project-name app-autorizador-processo` + `validate.mjs` → build de produção OK.
- `strict` e `strictTemplates` ativos; zoneless; `OnPush` por schematic e regra de lint; `npm start` = `ng serve --port 4200`.
- Testes: Vitest via `@angular/build:unit-test` (`npm test` = `ng test --no-watch`), 3 testes (shell, rota inicial e `API_BASE_URL`).
- Lint: `angular-eslint` 21 (`npm run lint` passa).
- Ambiente: `environment.ts` (`/api/v1`) e `environment.development.ts` (`http://localhost:8080/api/v1`) com `fileReplacements`; token `API_BASE_URL` lê o environment, sem requisições.
- `npm ci`, `npm run lint`, `npm run build`, `npm test` verdes; `npm start` serviu HTTP 200 na porta 4200 e o processo foi encerrado.
- Desvio: `.specs/06-execution-plan/` citada no índice não existe.

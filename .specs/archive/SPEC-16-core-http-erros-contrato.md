# SPEC-16 — Núcleo HTTP: tipos do contrato, interceptors e mapeamento de erros

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `core/http`, `shared/api` |
| Prioridade | P1 |
| Depende de | SPEC-14, SPEC-05 |
| Sucessoras | SPEC-17, SPEC-18, SPEC-19, SPEC-20, SPEC-23, SPEC-32 |
| Paralelismo | junto com SPEC-06 e SPEC-15 |

## 1. Objetivo
Estabelecer a fronteira tipada com a API: tipos gerados do OpenAPI (somente tipos), cliente HTTP base com URL configurável, interceptor funcional que converte falhas em `AppError` e mapeia `ProblemDetail.code`, e o tratamento especial do **`403 AUTORIZACAO_NEGADA` como resultado de negócio** (não como erro).
**Restrições:** não duplicar DTOs à mão; tipos gerados não validam runtime — validar na fronteira apenas o necessário (`code`, `status`); não transformar falha em sucesso vazio; sem biblioteca de estado; ADR-08 (geração só de tipos com `openapi-typescript` como devDependency; justificar no `doc.md`).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` §1–§3, `TARGET_ARCHITECTURE.md` §3; skills `angular-module-scaffold` (`references/contract-first.md`), `angular-architect`

## 3. Tarefas
- [ ] **T1.** Script `npm run api:types` gerando `src/app/shared/api/api-types.ts` a partir de `api-autorizador-processo/src/main/resources/openapi/` (resolver `$ref`; se necessário, agrupar com bundle); arquivo gerado versionado e marcado como gerado.
- [ ] **T2.** `core/http/api-client.ts`: `HttpClient` tipado, base `environment.apiBaseUrl`, helpers `get/post` com tipos do contrato.
- [ ] **T3.** `core/errors/app-error.ts`: `AppError { kind: 'validacao'|'negocio'|'nao-encontrado'|'rede'|'servidor'; code?; detail; campos? }`; `mapProblem(response)` por `code` (nunca por texto).
- [ ] **T4.** Interceptor funcional `errorInterceptor`: HTTP 0/timeouts → `rede`; `application/problem+json` → mapeado; resposta inesperada → `servidor` sem expor detalhes técnicos.
- [ ] **T5.** Utilitário `resultadoNegocio`: `AUTORIZACAO_NEGADA` retorna resultado `negado` ao chamador em vez de lançar (usado em SPEC-18).
- [ ] **T6.** Testes com `HttpTestingController`: cada `code` do catálogo, rede fora, 500 genérico, JSON inválido.
- [ ] **T7.** `core/http/doc.md`: contrato consumido (operações), decisão ADR-08, como regenerar tipos.

**Aceite (G/W/T):** *Given* resposta `403` com `code=PROCEDIMENTO_DUPLICADO` · *When* o interceptor processa · *Then* `AppError{kind:'negocio', code:'PROCEDIMENTO_DUPLICADO'}`. *Given* `403 AUTORIZACAO_NEGADA` na solicitação · *Then* o chamador recebe `{resultado:'negado'}` sem exceção. *Given* sem rede · *Then* `AppError{kind:'rede'}`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5, T7 | `angular-module-scaffold` (regras contract-first), `angular-architect` |
| T6 | `angular-architect` (testes) |

## 5. Verificação obrigatória
- `npm run api:types && git diff --exit-code app-autorizador-processo/src/app/shared/api` → sem diferença (tipos coerentes com o contrato)
- `npm run build && npm test && npm run lint` → verdes
- Definition of Done: 100 % dos `code` de `API_CONTRACTS.md` §3 cobertos por teste executado; nenhum DTO escrito à mão.

## 6. Resultado
_Preencher ao concluir._

## Resumo de conclusão (etapa 17 do loop test-01)
- **Resultado:** núcleo HTTP entregue: tipos gerados do OpenAPI (`npm run api:types`), `AppError`/`mapProblem` (12 códigos), `api-client`, `errorInterceptor`, `resultadoNegocio` e `core/http/doc.md`.
- **Validações:** `npm run lint`, `npm run build` e `npm test` verdes (62 testes); geração de tipos idempotente.
- **Limitações aceitas:** `git diff --exit-code` em `shared/api` verificado por comparação antes/depois (arquivos ainda não rastreados); catálogo de 12 códigos do contrato prevalece sobre os 7 da spec. Pasta `06-execution-plan/` inexistente, ignorada.

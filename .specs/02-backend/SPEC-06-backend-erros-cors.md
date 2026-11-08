# SPEC-06 — Tratamento de erros, validação e CORS

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · `shared` |
| Prioridade | P0 |
| Depende de | SPEC-03, SPEC-05 |
| Sucessoras | SPEC-07, SPEC-11, SPEC-22, SPEC-41 |
| Paralelismo | junto com SPEC-16 |

## 1. Objetivo
Implementar o kit transversal: `GlobalExceptionHandler` único (RFC 9457 + `code`), exceções de negócio base, mapeamento de validação para `errors[]`, ausência total de stack trace, CORS por allowlist. Corrige D1/D4/D5 (baseline).
**Restrições:** mensagens em pt-BR corrigidas (sem os erros de grafia originais); controllers sem `try/catch`; domínio sem Spring.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` §3, `SECURITY_REQUIREMENTS.md` (SEC-02, 03, 05); skill `java-spring-create-module` (`architecture.md`, `logging.md`)

## 3. Tarefas
- [ ] **T1.** `shared.error`: `BusinessRuleException` com `code`; subclasses específicas serão criadas pelos módulos.
- [ ] **T2.** `shared.web.GlobalExceptionHandler`: mapeia exceções de negócio, `MethodArgumentNotValidException`/`ConstraintViolationException` (→ `VALIDACAO_INVALIDA` + `errors[campo,mensagem]`), `HttpMessageNotReadableException` (→ `CORPO_INVALIDO`), `MethodArgumentTypeMismatch/MissingServletRequestParameter` (→ `PARAMETRO_INVALIDO`), `NoResourceFoundException` (404 genérico) e `Exception` (→ `ERRO_INTERNO`, log com `traceId`, corpo sem detalhes).
- [ ] **T3.** `application/problem+json` com `type`, `title`, `status`, `detail`, `instance`, `code`, `timestamp`.
- [ ] **T4.** `CorsConfig` lendo `CORS_ALLOWED_ORIGINS` (lista separada por vírgula); padrão vazio (nenhuma origem); perfil `local` define `http://localhost:4200`; métodos `GET,POST,OPTIONS`; cabeçalhos permitidos `Authorization` e `Content-Type`; sem `*`.
- [ ] **T5.** Testes `@WebMvcTest` com controller de teste (descartável) cobrindo cada `code`, ausência de `trace`/nome de classe no corpo, preflight CORS permitido/negado.

**Aceite:** *Given* origem `http://localhost:4200` no perfil `local` · *When* `OPTIONS /api/v1/procedimentos` · *Then* `200` com `Access-Control-Allow-Origin`; origem não listada → `403`. *Given* exceção inesperada · *Then* `500` `ERRO_INTERNO` sem stack trace.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5 | `java-spring-create-module` (módulo `shared`), `java-modern-engineering`; `openapi-rest-contract` (consistência do `ProblemDetail`) |

## 5. Verificação obrigatória
- `./mvnw verify` → testes do handler e CORS verdes; `ArchitectureTest` 0 violações
- `grep -rn "printStackTrace\|getStackTrace" api-autorizador-processo/src/main` → vazio
- Definition of Done: todos os `code` de `API_CONTRACTS.md` §3 cobertos por teste executado.

## 6. Resultado
_Preencher ao concluir._

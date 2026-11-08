# Tarefas — Erros, validação e CORS

## 1. Preparação
- [x] 1.1 Reconferir o `ProblemDetail`/`FieldViolation` de `openapi/common/responses.yaml` e o `pom.xml` entregues pela etapa 5 e ler `API_CONTRACTS.md` §3 e SEC-02/03/05; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Verificar se `@WebMvcTest` precisa do starter de teste de MVC no Boot 4 e adicioná-lo, com escopo `test`, só se faltar. Feito: `spring-boot-starter-webmvc-test` (test) adicionado ao `pom.xml`; o `spring-boot-starter-test` do Boot 4 não traz `@WebMvcTest`.

## 2. Exceções e tratamento global
- [x] 2.1 Criar `shared.error.BusinessRuleException` (sem Spring) com `code` e categoria de status, mensagens em pt-BR.
- [x] 2.2 Criar `shared.web.GlobalExceptionHandler` com os mapeamentos de negócio, validação (`VALIDACAO_INVALIDA` + `errors[]`), `CORPO_INVALIDO`, `PARAMETRO_INVALIDO` e `404` genérico.
- [x] 2.3 Mapear `Exception` para `500 ERRO_INTERNO` com log contendo `traceId` e corpo sem detalhes internos.
- [x] 2.4 Garantir `application/problem+json` com `type`, `title`, `status`, `detail`, `instance`, `code` e `timestamp`, alinhado ao `ProblemDetail` do contrato.

## 3. CORS
- [x] 3.1 Criar `shared.web.CorsConfig` lendo `CORS_ALLOWED_ORIGINS` (padrão vazio, sem `*`), métodos `GET,POST,OPTIONS`, cabeçalhos `Authorization` e `Content-Type`.
- [x] 3.2 Definir `http://localhost:4200` no `application-local.yml` e documentar `CORS_ALLOWED_ORIGINS` no `.env.example` da raiz.

## 4. Testes e verificação
- [x] 4.1 Criar controller de teste descartável (somente em `src/test`) que dispara cada tipo de erro.
  > ✅ 2026-11-08 — `ErroTesteController` em `src/test/.../shared/web` com um endpoint por tipo de erro. Verificação: compilou e foi exercitado pelos testes.
- [x] 4.2 Escrever testes `@WebMvcTest` por `code`, validando status, `Content-Type` e `errors[]`.
  > ✅ 2026-11-08 — `GlobalExceptionHandlerTest` com 8 casos por `code` (status, `application/problem+json`, `errors[]`). Verificação: 11/11 testes passam.
- [x] 4.3 Testar a ausência de `trace`, nome de classe e SQL no corpo do `500`.
  > ✅ 2026-11-08 — Teste do `500` confirma ausência de `trace`, `stackTrace`, nome de classe, pacote e SQL no corpo. Verificação: teste verde.
- [x] 4.4 Testar o preflight CORS: origem permitida no perfil `local` e origem negada (`403`).
  > ✅ 2026-11-08 — Preflight de `http://localhost:4200` (perfil `local`) retorna 200 com `Access-Control-Allow-Origin`; origem não listada retorna 403. Verificação: testes verdes.
- [x] 4.5 Rodar `./mvnw verify` e o `grep` de `printStackTrace|getStackTrace` em `src/main`; corrigir falhas e registrar bloqueio se o Docker faltar.
  > ✅ 2026-11-08 — `./mvnw verify` com Docker disponível: 48 testes, 0 falhas, BUILD SUCCESS (ArchitectureTest 19 e ModularityTests ok); grep `printStackTrace|getStackTrace` em `src/main` vazio. Desvio: o Maven só resolveu `spring-boot-starter-webmvc-test:4.1.2` com `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT` (falha PKIX do ambiente no truststore do JDK).
- [x] 4.6 Registrar a evidência por tarefa, o resumo e arquivar a spec com `git mv` para `.specs/archive/`.
  > ✅ 2026-11-08 — Evidência registrada; SPEC-06 movida com `git mv` de `.specs/02-backend/` para `.specs/archive/`, com a seção Resultado preenchida.

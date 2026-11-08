# Erros, validação e CORS

Etapa 6 de 31 · Fase 1 · derivada da `SPEC-06` (`.specs/02-backend/SPEC-06-backend-erros-cors.md`).

## Objetivo
Entregar o kit transversal de erros do backend no pacote `shared`: um único `GlobalExceptionHandler` que responde em `application/problem+json` (RFC 9457) com `code`, a exceção de negócio base, o mapeamento da validação para `errors[]`, ausência total de stack trace/SQL/nome de classe no corpo (SEC-02, SEC-03) e CORS por allowlist configurável (SEC-05). Não implementa autenticação.

## Ponto de partida (conferido contra o repositório após a entrega da etapa 5)
- `api-autorizador-processo/` existe com `shared/logging/RequestLoggingFilter` (MDC `requestId`/`traceId`), `shared/package-info.java` (módulo Modulith **aberto**), `ArchitectureTest`, `ModularityTests`, migrações Flyway (etapa 4) e testes com Testcontainers (`IntegrationTestBase`).
- O `pom.xml` tem `spring-boot-starter-webmvc`, `-validation`, `-actuator`, JPA/Flyway/Testcontainers e três execuções do `openapi-generator` (`procedimento`, `autorizacao`, `painel`, Jackson 3, `useSpringBoot4`); **não há** Spring Security (virá na SPEC-22) nem starter de teste de MVC explícito. Não existe controller, `GlobalExceptionHandler` nem `CorsConfig` em `src/main`.
- O contrato da etapa 5 existe: `openapi/common/responses.yaml` define `ProblemDetail` (obrigatórios `title`, `status`, `code`, `timestamp`; opcionais `type`, `detail`, `instance`, `errors[]` com `campo`/`mensagem`), `FieldViolation` e as respostas `400/403/404/500`; o enum de `code` tem 7 valores. O handler desta etapa **espelha** esse esquema (o contrato manda).
- `application.yml` não tem propriedade de CORS; `application-local.yml` define o perfil `local` (padrão). O `.env.example` da raiz ainda não cita `CORS_ALLOWED_ORIGINS`.
- `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).
- Fonte do catálogo: `API_CONTRACTS.md` §3; requisitos: `SECURITY_REQUIREMENTS.md` (SEC-02, 03, 05).

## Escopo
1. `shared.error`: `BusinessRuleException` (sem Spring) com `code` e categoria de status; as subclasses concretas (`PROCEDIMENTO_DUPLICADO`, `AUTORIZACAO_NEGADA`, `PROCEDIMENTO_NAO_ENCONTRADO`) ficam para os módulos (T1).
2. `shared.web.GlobalExceptionHandler` (`@RestControllerAdvice`) mapeando: exceções de negócio; `MethodArgumentNotValidException`/`ConstraintViolationException`/`HandlerMethodValidationException` → `400 VALIDACAO_INVALIDA` + `errors[{campo,mensagem}]`; `HttpMessageNotReadableException` → `400 CORPO_INVALIDO`; `MethodArgumentTypeMismatchException`/`MissingServletRequestParameterException` → `400 PARAMETRO_INVALIDO`; `NoResourceFoundException` → `404` genérico; `Exception` → `500 ERRO_INTERNO` (log com `traceId`, corpo sem detalhes) (T2).
3. Corpo `application/problem+json` com `type`, `title`, `status`, `detail`, `instance`, `code`, `timestamp` (+ `errors[]` quando houver), em pt-BR correto (T3).
4. `shared.web.CorsConfig` lendo `CORS_ALLOWED_ORIGINS` (lista separada por vírgula); padrão vazio (nenhuma origem); perfil `local` define `http://localhost:4200`; métodos `GET,POST,OPTIONS`; cabeçalhos `Authorization` e `Content-Type`; sem `*` (T4).
5. Testes `@WebMvcTest` com controller de teste descartável cobrindo cada `code` atingível nesta etapa, ausência de `trace`/nome de classe no corpo e preflight CORS permitido/negado (T5).

## Fora do escopo (fica para outras etapas)
- Autenticação, JWT, `401/429` e `NAO_AUTENTICADO`/`SESSAO_EXPIRADA`/`CREDENCIAIS_INVALIDAS`: etapa 7 (SPEC-22). A integração do CORS com a cadeia do Spring Security fica lá.
- Exceções de negócio concretas e controllers: etapas dos módulos (SPEC-07 em diante).
- Configuração/observabilidade extra e endurecimento: SPEC-11 e SPEC-41.
- Contrato OpenAPI: é da etapa 5; só corrigir inconsistência de `ProblemDetail`, se houver, e registrar.

## Critérios de aceitação
- *Dado* origem `http://localhost:4200` no perfil `local`, *quando* `OPTIONS /api/v1/procedimentos` (preflight), *então* resposta de sucesso com `Access-Control-Allow-Origin`; origem não listada → `403`.
- *Dada* exceção inesperada, *então* `500 ERRO_INTERNO` sem stack trace nem nome de classe; `traceId` apenas no log.
- Cada `code` do catálogo atingível (`VALIDACAO_INVALIDA`, `CORPO_INVALIDO`, `PARAMETRO_INVALIDO`, `ERRO_INTERNO` e, via exceção base de teste, os caminhos de negócio `403/404`) coberto por teste executado.
- `./mvnw verify` verde (`ArchitectureTest` e `ModularityTests` com 0 violações; testes com Testcontainers exigem Docker, senão registrar bloqueio); `grep -rn "printStackTrace\|getStackTrace" api-autorizador-processo/src/main` vazio; nenhum nome "legado/legacy".

## Riscos
- Boot 4 pode exigir o starter de teste de MVC (`spring-boot-starter-webmvc-test`) para `@WebMvcTest`: adicionar só se faltar, com escopo `test`.
- `NoResourceFoundException` e preflight negado podem ser tratados pelo Spring antes do handler: validar o comportamento real em teste.
- O ArchUnit proíbe framework no domínio: `BusinessRuleException` fica sem dependência de Spring; o mapeamento HTTP fica só no handler.
- Pode haver divergência entre o `ProblemDetail` do Spring e o esquema do contrato (`code`, `timestamp`, `errors`): o contrato manda.

## Decisões tomadas sem consulta
- **Aprovação:** o catálogo de `API_CONTRACTS.md` §3 e a SPEC-06 valem como aprovação; nenhum `code` novo é criado.
- **[REVISAR] `CorsConfig` via `WebMvcConfigurer`:** não há Spring Security ainda; a SPEC-22 deverá reaproveitar a mesma configuração na cadeia de segurança. Avaliar se já deve ser um `CorsConfigurationSource`.
- **Base do `ProblemDetail`:** usar o `ProblemDetail` do Spring estendido com propriedades (`code`, `timestamp`, `errors`), em vez de classe própria, salvo se divergir do contrato da etapa 5.
- **Revisão da proposta antecipada:** a proposta foi escrita antes da etapa 5 e reconferida agora contra o contrato entregue (`responses.yaml`); o ponto de partida foi atualizado, sem mudança de escopo.
- **Resposta 404 genérico:** `NoResourceFoundException` usa `code` `PROCEDIMENTO_NAO_ENCONTRADO` só quando vier de exceção de negócio; rota inexistente devolve 404 problem+json sem `code` do catálogo de negócio, e o enum do contrato não ganha valor novo (registrar na evidência se o contrato exigir `code`).

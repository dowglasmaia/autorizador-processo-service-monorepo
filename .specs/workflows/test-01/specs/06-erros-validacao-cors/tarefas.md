# Tarefas — Erros, validação e CORS

## 1. Preparação
- [ ] 1.1 Reconferir o estado após a etapa 5 (`ProblemDetail` do contrato, pacotes gerados, `pom.xml`) e ler `API_CONTRACTS.md` §3 e SEC-02/03/05; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Verificar se `@WebMvcTest` precisa do starter de teste de MVC no Boot 4 e adicioná-lo, com escopo `test`, só se faltar.

## 2. Exceções e tratamento global
- [ ] 2.1 Criar `shared.error.BusinessRuleException` (sem Spring) com `code` e categoria de status, mensagens em pt-BR.
- [ ] 2.2 Criar `shared.web.GlobalExceptionHandler` com os mapeamentos de negócio, validação (`VALIDACAO_INVALIDA` + `errors[]`), `CORPO_INVALIDO`, `PARAMETRO_INVALIDO` e `404` genérico.
- [ ] 2.3 Mapear `Exception` para `500 ERRO_INTERNO` com log contendo `traceId` e corpo sem detalhes internos.
- [ ] 2.4 Garantir `application/problem+json` com `type`, `title`, `status`, `detail`, `instance`, `code` e `timestamp`, alinhado ao `ProblemDetail` do contrato.

## 3. CORS
- [ ] 3.1 Criar `shared.web.CorsConfig` lendo `CORS_ALLOWED_ORIGINS` (padrão vazio, sem `*`), métodos `GET,POST,OPTIONS`, cabeçalhos `Authorization` e `Content-Type`.
- [ ] 3.2 Definir `http://localhost:4200` no `application-local.yml` e documentar a variável no `.env.example`, se existir.

## 4. Testes e verificação
- [ ] 4.1 Criar controller de teste descartável (somente em `src/test`) que dispara cada tipo de erro.
- [ ] 4.2 Escrever testes `@WebMvcTest` por `code`, validando status, `Content-Type` e `errors[]`.
- [ ] 4.3 Testar a ausência de `trace`, nome de classe e SQL no corpo do `500`.
- [ ] 4.4 Testar o preflight CORS: origem permitida no perfil `local` e origem negada (`403`).
- [ ] 4.5 Rodar `./mvnw verify` e o `grep` de `printStackTrace|getStackTrace` em `src/main`; corrigir falhas e registrar bloqueio se o Docker faltar.
- [ ] 4.6 Registrar a evidência por tarefa, o resumo e arquivar a spec com `git mv` para `.specs/archive/`.

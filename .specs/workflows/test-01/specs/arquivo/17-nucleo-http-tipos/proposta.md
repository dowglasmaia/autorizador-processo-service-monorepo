# Núcleo HTTP e tipos

Etapa 17 de 31 · Fase 3 · derivada da `SPEC-16` (`.specs/03-frontend/SPEC-16-core-http-erros-contrato.md`). Revisada contra a base entregue pela etapa 16 (design system e layout).

## Objetivo
Estabelecer a fronteira tipada com a API: tipos gerados do OpenAPI (somente tipos, sem DTOs à mão), cliente HTTP base com URL configurável, interceptor funcional que converte falhas em `AppError` mapeando `ProblemDetail.code`, e o `403 AUTORIZACAO_NEGADA` tratado como resultado de negócio (`negado`), não como erro.

## Ponto de partida (conferido no repositório)
- `app-autorizador-processo/` existe (etapa 15): Angular 21.2, Vitest, scripts `lint`/`build`/`test`; **não há** script `api:types` nem `openapi-typescript`.
- `core/http/` já tem `api-base-url.token.ts` (`API_BASE_URL` ← `environment.apiBaseUrl`), `api-error.ts` (`ApiError {status, message}` provisório) e `api-error.interceptor.ts`, registrado em `app.config.ts`. Serão **substituídos/evoluídos** por `AppError` e `errorInterceptor`, sem manter dois tratamentos de erro.
- Contrato em `api-autorizador-processo/src/main/resources/openapi/`: `common/responses.yaml` (`ProblemDetail`, `FieldViolation`), `common/security.yaml` e módulos `auth`, `autorizacao`, `painel`, `procedimento` (operações `autenticar`, `obterUsuarioAtual`, `solicitarAutorizacao`, `listarAutorizacoes`, `obterResumoPainel`, `listarProcedimentos`, `cadastrarProcedimento`, `obterProcedimento`), com `$ref` entre arquivos.
- O enum `ProblemDetail.code` do contrato real tem **12 códigos**: `VALIDACAO_INVALIDA`, `CORPO_INVALIDO`, `PARAMETRO_INVALIDO`, `PROCEDIMENTO_DUPLICADO`, `AUTORIZACAO_NEGADA`, `PROCEDIMENTO_NAO_ENCONTRADO`, `ERRO_INTERNO`, `CREDENCIAIS_INVALIDAS`, `NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `ACESSO_NEGADO`, `MUITAS_TENTATIVAS`. A SPEC-16 cita só os 7 do catálogo de `API_CONTRACTS.md` §3; vale o contrato (a spec exige cobrir 100 % dos `code`).
- Etapa 16 entregue: `shared/ui`, `layout/shell`, `core/feedback` (`NotificationService`, `LoadingService`, `ErrorDialog` com texto simples) e `core/api-status` (`ApiStatusService`, `GET /actuator/health` via `HttpClient`, que ignora o erro e marca "indisponível"). Esta etapa não altera `core/feedback`; `ApiError`/`apiErrorInterceptor` só são referenciados por `app.config.ts` (nenhuma outra feature os usa). Sem `.prettierignore`; o ESLint cobre `src/`, então o arquivo gerado precisa de exclusão.
- A pasta `.specs/06-execution-plan/` citada no `INDEX.md` não existe; desvio a registrar na evidência.

## Escopo
1. Script `npm run api:types` (`openapi-typescript` como devDependency, ADR-08) gerando `src/app/shared/api/api-types.ts` a partir do contrato (resolvendo `$ref`; bundle intermediário se necessário), versionado e marcado como gerado.
2. `core/http/api-client.ts`: `HttpClient` tipado com base `API_BASE_URL` e helpers `get/post` tipados pelo contrato.
3. `core/errors/app-error.ts`: `AppError { kind: 'validacao'|'negocio'|'nao-encontrado'|'rede'|'servidor'; code?; detail; campos? }` e `mapProblem` por `code` (nunca por texto), cobrindo os 12 códigos e o status HTTP como reserva.
4. `errorInterceptor` funcional: status 0/timeout → `rede`; `application/problem+json` → mapeado; resposta inesperada ou JSON inválido → `servidor` sem detalhes técnicos. Substitui `apiErrorInterceptor`.
5. Utilitário `resultadoNegocio`: `AUTORIZACAO_NEGADA` vira `{resultado:'negado'}` em vez de exceção (consumido pela feature de solicitação).
6. Testes com `HttpTestingController` para cada `code`, rede fora, 500 genérico e JSON inválido.
7. `core/http/doc.md`: operações consumidas, decisão ADR-08, como regenerar tipos.

## Fora do escopo (fica para outras etapas)
- Interceptor de autenticação/JWT, guards, tratamento de `NAO_AUTENTICADO`/`SESSAO_EXPIRADA` com redirecionamento (SPEC-23): aqui eles apenas são mapeados para `AppError`.
- Telas e serviços de feature de procedimentos, solicitação, autorizações e painel (SPEC-17 a SPEC-20); exibição do erro ao usuário (toast/dialog) fica com cada feature.
- Biblioteca de estado, validação runtime de DTOs além de `code`/`status`, geração de clientes (só tipos), alteração do contrato OpenAPI ou do backend.

## Critérios de aceitação
- *Given* `403` com `code=PROCEDIMENTO_DUPLICADO` · *Then* `AppError{kind:'negocio', code:'PROCEDIMENTO_DUPLICADO'}`.
- *Given* `403 AUTORIZACAO_NEGADA` na solicitação · *Then* o chamador recebe `{resultado:'negado'}` sem exceção.
- *Given* sem rede · *Then* `AppError{kind:'rede'}`; 500 sem corpo problem+json · *Then* `kind:'servidor'` sem expor detalhes.
- `npm run api:types` seguido de `git diff --exit-code app-autorizador-processo/src/app/shared/api` sem diferença.
- `npm run lint`, `npm run build` e `npm test` verdes; todos os `code` do contrato cobertos por teste; nenhum DTO escrito à mão.

## Decisões tomadas sem consulta
- **Catálogo de códigos:** vale o enum real do OpenAPI (12 códigos), não os 7 do `API_CONTRACTS.md`; classificação: 400 → `validacao`; 404 → `nao-encontrado`; `PROCEDIMENTO_DUPLICADO`/`AUTORIZACAO_NEGADA`/`ACESSO_NEGADO`/`MUITAS_TENTATIVAS`/`CREDENCIAIS_INVALIDAS`/`NAO_AUTENTICADO`/`SESSAO_EXPIRADA` → `negocio`; `ERRO_INTERNO` → `servidor`. [REVISAR]
- **Substituição do interceptor provisório:** `ApiError`/`apiErrorInterceptor` da etapa 15 são removidos e os testes ajustados.
- **Gerador:** `openapi-typescript` com bundle prévio dos `$ref` apenas se a resolução direta falhar; instalação das dependências é necessária nesta etapa e autorizada pela SPEC-16 (ADR-08). [REVISAR]
- **Pasta `06-execution-plan/`:** inexistente; ignorada e registrada.
- **Revisão feita:** conferida contra a etapa 16; sem conflito. `ApiStatusService` continua válido (consome só o erro). O `status` HTTP real de cada `code` (ex.: `PROCEDIMENTO_DUPLICADO` em 403, conforme `procedimento.yaml`) vale sobre a tabela de classificação acima quando divergirem; `mapProblem` decide por `code` e usa o status só como reserva.
- **Lote 1 — geração de tipos:** o contrato é dividido em 4 módulos (`modules/*.yaml`) e o `openapi-typescript` aceita uma entrada por vez; o script `scripts/gerar-api-types.mjs` gera cada módulo como `namespace` (`Auth`, `Autorizacao`, `Painel`, `Procedimento`) num único `api-types.ts`. Aliases em `shared/api/index.ts` (`ProblemDetail` vem do namespace `Procedimento`; é idêntico nos demais). [REVISAR]
- **Lote 1 — `app.config.ts`:** com a remoção de `apiErrorInterceptor` (2.3), `provideHttpClient()` fica sem interceptor até a tarefa 3.2 registrar o `errorInterceptor`.
- **Lote 1 — classificação:** `CREDENCIAIS_INVALIDAS`, `NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `ACESSO_NEGADO`, `MUITAS_TENTATIVAS` → `negocio`; `kind` `servidor` nunca expõe `detail` do backend (mensagem genérica).
- **Lote 2 — interceptor:** `errorInterceptor` (`core/http/error.interceptor.ts`) exporta também `paraAppError`; timeout do rxjs (`TimeoutError`) e status 0 viram `rede`; resposta 2xx com erro de parsing ou corpo que não é objeto vira `servidor`. Registrado em `app.config.ts` via `withInterceptors`.
- **Lote 2 — `resultadoNegocio`:** operador RxJS em `core/http/resultado-negocio.ts` devolvendo `{resultado:'ok', valor}` ou `{resultado:'negado', detail}`; demais erros propagam.
- **Lote 2 — 5.2:** `shared/api` ainda está não rastreado pelo git (o devkit comita depois), então `git diff --exit-code` seria vazio por definição; a idempotência foi verificada comparando o arquivo antes e depois de rodar `npm run api:types` duas vezes (idêntico). [REVISAR]
- **Lote 2 — verificação:** `npm run lint`, `npm run build` e `npm test` verdes (62 testes).

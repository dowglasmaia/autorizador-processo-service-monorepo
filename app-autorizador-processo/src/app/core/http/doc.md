# Núcleo HTTP e tipos

Fronteira tipada com a API do Autorizador.

## Peças

- `api-base-url.token.ts`: `API_BASE_URL`, lida de `environment.apiBaseUrl`.
- `api-client.ts`: `ApiClient` com `get/post` tipados; concatena a URL base ao caminho do contrato.
- `error.interceptor.ts`: `errorInterceptor` funcional. Converte toda falha em `AppError`:
  status 0/timeout → `rede`; corpo `problem+json` → `mapProblem` (por `code`, status só como reserva);
  corpo inesperado ou JSON inválido → `servidor` com mensagem genérica (sem detalhes técnicos).
- `resultado-negocio.ts`: operador `resultadoNegocio()`; `AUTORIZACAO_NEGADA` vira `{resultado:'negado'}` em vez de erro.
- `../errors/app-error.ts`: `AppError`, `mapProblem` e a classificação dos 12 `code` do contrato.

## Operações consumidas (contrato OpenAPI)

`autenticar`, `obterUsuarioAtual`, `solicitarAutorizacao`, `listarAutorizacoes`, `obterResumoPainel`,
`listarProcedimentos`, `cadastrarProcedimento`, `obterProcedimento`. Os serviços de cada uma ficam nas etapas das features.

## ADR-08 — tipos gerados do OpenAPI

Os tipos vêm do contrato com `openapi-typescript` (devDependency, só tipos, sem cliente gerado), em
`src/app/shared/api/api-types.ts`. Não se escrevem DTOs à mão; `shared/api/index.ts` apenas reexporta aliases.
O arquivo gerado é versionado e excluído do ESLint e do Prettier.

## Como regenerar os tipos

```
npm run api:types
git diff --exit-code src/app/shared/api   # não deve haver diferença
```

O script `scripts/gerar-api-types.mjs` gera cada módulo (`Auth`, `Autorizacao`, `Painel`, `Procedimento`) como `namespace`.

## Autenticação

`AppError.retryAfter` (segundos) é preenchido em `429` a partir de `Retry-After`. O `authInterceptor` (`core/auth`) vem antes do `errorInterceptor` na lista. Ver `core/auth/doc.md`.

# Tarefas — Núcleo HTTP e tipos

## 1. Tipos gerados do contrato
- [ ] 1.1 Adicionar `openapi-typescript` como devDependency e documentar a justificativa (ADR-08).
- [ ] 1.2 Criar o script `npm run api:types` gerando `src/app/shared/api/api-types.ts` a partir de `api-autorizador-processo/src/main/resources/openapi/` (resolvendo `$ref`; bundle se necessário).
- [ ] 1.3 Gerar e versionar `api-types.ts` com cabeçalho de arquivo gerado e exclusão no ESLint (`ignores`) e no Prettier (criar `.prettierignore`).
- [ ] 1.4 Criar aliases de tipos em `shared/api` (reexportando do arquivo gerado, sem redefinir DTOs).

## 2. Erros da aplicação
- [ ] 2.1 Criar `core/errors/app-error.ts` com `AppError` e `kind`.
- [ ] 2.2 Implementar `mapProblem` por `code` cobrindo os 12 códigos do contrato, com reserva por status HTTP.
- [ ] 2.3 Remover `ApiError`/`toApiError`/`apiErrorInterceptor` provisórios da etapa 15 (usados só em `app.config.ts`).

## 3. Cliente HTTP e interceptor
- [ ] 3.1 Criar `core/http/api-client.ts` com base `API_BASE_URL` e helpers `get/post` tipados.
- [ ] 3.2 Criar `errorInterceptor` funcional (rede/timeout, problem+json, resposta inesperada) e substituir `apiErrorInterceptor` em `app.config.ts`.
- [ ] 3.3 Criar `resultadoNegocio` tratando `AUTORIZACAO_NEGADA` como `{resultado:'negado'}`.

## 4. Testes
- [ ] 4.1 Testar `mapProblem` e o interceptor com `HttpTestingController` para cada `code` do contrato.
- [ ] 4.2 Testar rede fora, 500 genérico, JSON inválido e `errors[]` de validação em `campos`.
- [ ] 4.3 Testar `resultadoNegocio` e o `api-client` (URL base e tipos).

## 5. Documentação e verificação
- [ ] 5.1 Escrever `core/http/doc.md` (operações consumidas, ADR-08, como regenerar tipos).
- [ ] 5.2 Rodar `npm run api:types` e confirmar `git diff --exit-code` sem diferença em `shared/api`.
- [ ] 5.3 Rodar `npm run lint`, `npm run build` e `npm test` e corrigir falhas.
- [ ] 5.4 Registrar evidência por tarefa, resumo e `git mv` da spec para `.specs/archive/` conforme `como-executar.md`.

# Tarefas — Login no frontend

## 1. Autenticação (core/auth)
- [x] 1.1 Adicionar `retryAfter?` ao `AppError`, preenchido pelo `errorInterceptor` em `429` a partir de `Retry-After`, com teste.
  > ✅ 2026-11-08 18:10 +00:00 — `AppError.retryAfter` preenchido em 429; teste em `error.interceptor.spec.ts`.
- [x] 1.2 Criar `core/auth/auth.service.ts` com signals `usuario`, `token`, `autenticado`, `expiraEm`, `entrar` e `sair`.
  > ✅ 2026-11-08 18:10 +00:00 — `AuthService` com signals e `entrar`/`sair`; coberto por `auth.service.spec.ts`.
- [x] 1.3 Persistir a sessão em `sessionStorage` (com `try/catch`), restaurá-la no início e agendar o logout no `exp`.
  > ✅ 2026-11-08 18:10 +00:00 — `sessionStorage` com try/catch, restauração e logout no `exp`; testes de reload e expiração.
- [x] 1.4 Criar `core/auth/auth.interceptor.ts` (Bearer só para `apiBaseUrl`; `401` encerra sessão e redireciona) e registrá-lo em `app.config.ts` antes do `errorInterceptor`, sem duplicá-lo.
  > ✅ 2026-11-08 18:10 +00:00 — `authInterceptor` registrado antes do `errorInterceptor` em `app.config.ts`; testes passam.

## 2. Guards e rotas
- [x] 2.1 Criar `retornoSeguro(url)` pura, rejeitando `//x`, `http://`, `javascript:` e barra invertida.
  > ✅ 2026-11-08 18:10 +00:00 — `retornoSeguro` pura; testes de `//x`, `http://`, `javascript:` e `\`.
- [x] 2.2 Criar `authGuard` (`canMatch`) e `guestGuard` funcionais, preservando `retorno`.
  > ✅ 2026-11-08 18:10 +00:00 — `authGuard`/`guestGuard` preservam `retorno`; testes em `auth.guard.spec.ts`.
- [x] 2.3 Aplicar `authGuard` às rotas `painel`, `procedimentos`, `solicitar` e `autorizacoes` e registrar `/login` fora do shell.
  > ✅ 2026-11-08 18:10 +00:00 — Rotas protegidas e `/login` fora do shell; `app.spec.ts` verde.

## 3. Tela de login
- [x] 3.1 Criar `features/login` (rota, página, painel de marca + cartão) com formulário reativo tipado e componentes de `shared/ui`.
  > ✅ 2026-11-08 18:10 +00:00 — Página de login com formulário tipado e `shared/ui`; `login-page.spec.ts`.
- [x] 3.2 Implementar mostrar/ocultar senha (`aria-pressed`), estado de carregamento, foco inicial, `autocomplete` e envio por Enter.
  > ✅ 2026-11-08 18:10 +00:00 — Senha com `aria-pressed`, carregamento, foco inicial e `autocomplete`; testado.
- [x] 3.3 Criar catálogo de mensagens por `code` (`CREDENCIAIS_INVALIDAS`, `MUITAS_TENTATIVAS` com `Retry-After`, `400` por campo, rede/servidor com modal de nova tentativa) e o aviso de sessão expirada.
  > ✅ 2026-11-08 18:10 +00:00 — Catálogo `mensagens-login.ts` (401, 429 com Retry-After, 400 por campo, modal de rede); testado.
- [x] 3.4 Escrever `features/login/doc.md`.
  > ✅ 2026-11-08 18:10 +00:00 — `features/login/doc.md` escrito.

## 4. Shell e logout
- [x] 4.1 Adicionar ao shell o menu do usuário (e-mail + "Sair") acessível por teclado.
  > ✅ 2026-11-08 18:10 +00:00 — Menu do usuário no shell com e-mail e Sair, por teclado; `shell.spec.ts`.
- [x] 4.2 Fazer o logout limpar o estado em memória (incluindo contadores de solicitações, se existirem) e navegar ao login.
  > ✅ 2026-11-08 18:10 +00:00 — `sair()` executa limpezas registradas e navega ao login; testado.

## 5. Testes e verificação
- [x] 5.1 Testar `AuthService` com `HttpTestingController` (sucesso, 401, 429, rede, restauração após reload, expiração).
  > ✅ 2026-11-08 18:10 +00:00 — `auth.service.spec.ts` cobre sucesso, 401, 429, rede, restauração e expiração.
- [x] 5.2 Testar interceptor (host externo sem Bearer, 401 limpa sessão), guards e `retornoSeguro`.
  > ✅ 2026-11-08 18:10 +00:00 — Specs do interceptor, guards e `retornoSeguro` verdes.
- [x] 5.3 Testar a página de login e o menu do shell (acessibilidade básica, mensagens, redirecionamentos).
  > ✅ 2026-11-08 18:10 +00:00 — `login-page.spec.ts` e `shell.spec.ts` verdes.
- [x] 5.4 Atualizar `README.md` e `doc.md` do frontend (fluxo, armazenamento, riscos).
  > ✅ 2026-11-08 18:10 +00:00 — README ganhou seção Autenticação; criado `core/auth/doc.md`; `core/http/doc.md` atualizado.
- [x] 5.5 Rodar `npm run lint`, `npm run build`, `npm test` e o `grep` de `localStorage|console.log` em `core/auth`; corrigir falhas.
  > ✅ 2026-11-08 18:10 +00:00 — `npm run lint` sem erros; `npm run build` ok; `npm test` 14 arquivos/110 testes passando; grep de `localStorage|console.log` em `core/auth` vazio.
- [x] 5.6 Registrar evidência por tarefa, resumo e `git mv` da spec para `.specs/archive/` conforme `como-executar.md`.
  > ✅ 2026-11-08 18:10 +00:00 — Evidência registrada; spec movida para `.specs/archive/`. Desvio: `.specs/06-execution-plan/` inexistente, ignorada.

## Resumo
Login, `AuthService`, interceptor, guards, tela, menu e logout entregues. Lint, build e 110 testes verdes; e2e contra o Compose fica para a SPEC-30. Limitações: token em `sessionStorage` (risco XSS), sem refresh token.

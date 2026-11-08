# Login no frontend

Etapa 18 de 31 · Fase 3 · derivada da `SPEC-23` (`.specs/03-frontend/SPEC-23-frontend-login-autenticacao.md`). Escrita de forma antecipada, enquanto a etapa 17 (núcleo HTTP e tipos) ainda é implementada; será revisada contra o que ela entregar.

## Objetivo
Entregar a tela `/login` (e-mail e senha), o serviço de autenticação JWT, o armazenamento do token em `sessionStorage`, o interceptor de `Authorization`, os guards de rota e o logout (menu do usuário no shell), de modo que todas as rotas de funcionalidade passem a exigir sessão.

## Ponto de partida (conferido no repositório, antes da etapa 17)
- `app-autorizador-processo/` com Angular 21, zoneless, Vitest e scripts `lint`/`build`/`test`. Há `shared/ui` (Button, Field, Card, Dialog, Icon…), `layout/shell` (sidebar + barra superior, sem menu de usuário), `core/feedback` (`NotificationService`, `ErrorDialogService` com `retry`, `LoadingService`) e `core/api-status`.
- `app.routes.ts` tem as rotas `painel`, `procedimentos`, `solicitar`, `autorizacoes` (lazy, sem guards), `_ds` (só fora de produção) e `**`. Não existe `/login`, nem `core/auth`, nem guards.
- `core/http` tem `API_BASE_URL`, `ApiError` e `apiErrorInterceptor` provisórios; a etapa 17 os substitui por `AppError`, `errorInterceptor`, `api-client` e `shared/api/api-types.ts` gerado. **Esta etapa depende disso** e deve usar o que a 17 entregar (nomes exatos a conferir na revisão).
- Contrato `openapi/modules/auth.yaml` já existe: `autenticar` (`POST /auth/login`, 200 `TokenResponse{accessToken, tokenType, expiraEm, usuario}`, 400/401/429) e `obterUsuarioAtual` (`GET /auth/me`); `429` traz `Retry-After`. Backend (SPEC-22) já arquivada.
- O protótipo não tem tela de login; o desenho deriva dos tokens do shell (gradiente da sidebar) e do design system (SPEC-15).
- `.specs/06-execution-plan/` citada no `INDEX.md` não existe; desvio a registrar na evidência.

## Escopo
1. `core/auth/auth.service.ts`: signals `usuario`, `token`, `autenticado`, `expiraEm`; `entrar`, `sair`, restauração da sessão do `sessionStorage` (com `try/catch`) e logout agendado no `exp` (decodificação só para UX).
2. `core/auth/auth.interceptor.ts` funcional: anexa `Bearer` somente a URLs sob `apiBaseUrl`; em `401` (`SESSAO_EXPIRADA`/`NAO_AUTENTICADO`) encerra a sessão e vai a `/login?retorno=…` com aviso, sem duplicar o `errorInterceptor` da etapa 17.
3. Guards funcionais `authGuard` (`canMatch` em todas as rotas de funcionalidade) e `guestGuard` (`/login`); função pura `retornoSeguro(url)` (rejeita `//x`, `http://`, `javascript:`, barra invertida).
4. `features/login`: rota `/login` fora do shell, página (painel de marca + cartão), formulário reativo tipado, mostrar/ocultar senha com `aria-pressed`, botão em carregamento, `role="alert"`, `autocomplete` correto, foco inicial no e-mail; mensagens por `code` em catálogo; `doc.md`.
5. Shell: menu do usuário (e-mail + "Sair") acessível por teclado; logout limpa estado em memória (inclui contadores de `SessaoSolicitacoes`, se já existirem) e volta ao login.
6. Testes (serviço com `HttpTestingController`, interceptor, guards, `retornoSeguro`, página, shell) e atualização de `doc.md`/`README.md` do frontend.

## Fora do escopo (fica para outras etapas)
- Cadastro de usuário, recuperação de senha, "lembrar-me", papéis/perfis, refresh token (PEN-09/PEN-10).
- Telas das features de procedimentos, solicitação, autorizações e painel (etapas seguintes) e microcopy/acessibilidade global (SPEC-21).
- Execução e2e contra o Compose completo e capturas finais (SPEC-30); alterações no backend ou no contrato OpenAPI.

## Critérios de aceitação
- *Given* sem sessão · *When* abrir `/procedimentos` · *Then* redireciona a `/login?retorno=%2Fprocedimentos`.
- *When* login válido · *Then* navega ao `retorno` (ou `/painel`) e o menu mostra o e-mail.
- *When* a API responde `401 SESSAO_EXPIRADA` em rota protegida · *Then* vai ao login com "Sua sessão expirou. Entre novamente." e nenhum dado da sessão anterior permanece.
- *When* `?retorno=//evil.com` · *Then* ignora e vai a `/painel`.
- `401 CREDENCIAIS_INVALIDAS` → "E-mail ou senha incorretos."; `429` → mensagem com tempo de `Retry-After`; rede/servidor → modal com nova tentativa; `400` → erros por campo.
- Usuário autenticado em `/login` → redirecionado ao painel.
- `npm run lint`, `npm run build` e `npm test` verdes; `grep -rn "localStorage\|console.log" app-autorizador-processo/src/app/core/auth` vazio; Bearer nunca enviado a outro host.

## Decisões tomadas sem consulta
- **Dependência da etapa 17:** a proposta assume `AppError`/`errorInterceptor`/`api-client`/tipos gerados; nomes e ordem dos interceptors serão conferidos na revisão. [REVISAR]
- **Armazenamento:** `sessionStorage` (ADR-16), nunca `localStorage` nem URL; cookie `HttpOnly` descartado (exigiria backend).
- **Verificação contra o Compose:** adiada para a SPEC-30; aqui valem testes automatizados, e a execução manual só se o ambiente estiver disponível, registrando o resultado.
- **Rotas existentes:** `_ds` e `**` ficam sem guard; só as quatro rotas de funcionalidade são protegidas. [REVISAR]
- **Aviso de sessão expirada:** passado por parâmetro de rota/estado do serviço (sem texto livre na URL), para não permitir injeção de mensagem.
- **Pasta `06-execution-plan/`:** inexistente; ignorada e registrada.

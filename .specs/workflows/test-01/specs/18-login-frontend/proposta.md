# Login no frontend

Etapa 18 de 31 · Fase 3 · derivada da `SPEC-23` (`.specs/03-frontend/SPEC-23-frontend-login-autenticacao.md`). Revisada contra o código entregue pela etapa 17.

## Objetivo
Entregar a tela `/login` (e-mail e senha), o serviço de autenticação JWT, o armazenamento do token em `sessionStorage`, o interceptor de `Authorization`, os guards de rota e o logout (menu do usuário no shell), de modo que todas as rotas de funcionalidade passem a exigir sessão.

## Ponto de partida (conferido no repositório após a etapa 17 entregue)
- `app-autorizador-processo/` com Angular 21, zoneless, Vitest e scripts `lint`/`build`/`test`. Há `shared/ui` (Button, Field, Card, Dialog, Icon…), `layout/shell/shell.ts` (sidebar + barra superior, sem menu de usuário), `core/feedback` (`NotificationService`, `ErrorDialogService.show(message, retry?)`, `LoadingService`) e `core/api-status`.
- Etapa 17 entregue: `core/errors/app-error.ts` (`AppError{kind, code, detail, campos, status}`, `isAppError`, `mapProblem`, 12 `code` incluindo `CREDENCIAIS_INVALIDAS`, `NAO_AUTENTICADO`, `SESSAO_EXPIRADA`, `MUITAS_TENTATIVAS`), `core/http/error.interceptor.ts` (`errorInterceptor`, `paraAppError`), `core/http/api-client.ts` (`ApiClient.get/post`), `API_BASE_URL` e `shared/api/index.ts` com `LoginRequest`, `TokenResponse`, `Usuario`, `ProblemCode` gerados. `app.config.ts` registra só `errorInterceptor`.
- Lacunas que afetam esta etapa: (a) `AppError` **não carrega `Retry-After`**, e o `errorInterceptor` descarta os cabeçalhos; (b) `environment.apiBaseUrl` é **relativo** (`/api/v1`), então "URL sob a API" deve tratar caminho relativo e URL absoluta; (c) não existe `SessaoSolicitacoes` (nem contadores), logo a limpeza de estado em memória se limita ao que existir; (d) `app.routes.ts` tem `painel`, `procedimentos`, `solicitar`, `autorizacoes` (lazy, sem guards), `_ds` (só fora de produção) e `**`; não há `/login`, `core/auth` nem guards.
- Contrato `auth.yaml` já existe: `autenticar` (`POST /auth/login`, 200 `TokenResponse{accessToken, tokenType, expiraEm, usuario}`, 400/401/429) e `obterUsuarioAtual` (`GET /auth/me`); `429` traz `Retry-After` (segundos). Backend (SPEC-22) já arquivado.
- O protótipo não tem tela de login; o desenho deriva dos tokens do shell (gradiente da sidebar) e do design system (SPEC-15).
- `.specs/06-execution-plan/` citada no `INDEX.md` não existe; desvio a registrar na evidência.

## Escopo
1. `core/auth/auth.service.ts`: signals `usuario`, `token`, `autenticado`, `expiraEm`; `entrar`, `sair`, restauração da sessão do `sessionStorage` (com `try/catch`) e logout agendado no `exp` (decodificação só para UX).
2. `core/auth/auth.interceptor.ts` funcional, registrado **antes** do `errorInterceptor` (`withInterceptors([authInterceptor, errorInterceptor])`) para receber `AppError` na volta; anexa `Bearer` somente a URLs sob `apiBaseUrl` (relativa ou absoluta); em `401` (`SESSAO_EXPIRADA`/`NAO_AUTENTICADO`) encerra a sessão e vai a `/login?retorno=…` com aviso, sem duplicar o `errorInterceptor` da etapa 17.
2b. Ajuste mínimo no núcleo HTTP: `AppError` ganha `retryAfter?: number` (segundos), preenchido pelo `errorInterceptor` a partir do cabeçalho `Retry-After` em respostas `429`, com teste; sem mudar o restante do comportamento da etapa 17.
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
- **Dependência da etapa 17 (conferida):** usar `AppError`, `errorInterceptor`, `ApiClient` e os tipos `LoginRequest`/`TokenResponse`/`Usuario` já entregues; `authInterceptor` entra antes do `errorInterceptor` na lista.
- **`Retry-After`:** a etapa 17 não o preserva; em vez de ler `HttpErrorResponse` fora do núcleo, adiciona-se `retryAfter` opcional ao `AppError` (mudança pequena e compatível, restrita a esta necessidade). [REVISAR]
- **URL da API:** como `apiBaseUrl` é relativo, o Bearer vai a requisições cuja URL começa com `apiBaseUrl` (ou com a origem absoluta equivalente); qualquer outro host fica sem token.
- **Armazenamento:** `sessionStorage` (ADR-16), nunca `localStorage` nem URL; cookie `HttpOnly` descartado (exigiria backend).
- **Contadores `SessaoSolicitacoes`:** não existem ainda; o logout limpa apenas o estado de `core/auth` e expõe um ponto de extensão simples (ex.: método `limparEstado` chamado no `sair`) para a etapa das solicitações. [REVISAR]
- **Verificação contra o Compose:** adiada para a SPEC-30; aqui valem testes automatizados, e a execução manual só se o ambiente estiver disponível, registrando o resultado.
- **Rotas existentes:** `_ds` e `**` ficam sem guard; só as quatro rotas de funcionalidade são protegidas. [REVISAR]
- **Aviso de sessão expirada:** passado por parâmetro de rota/estado do serviço (sem texto livre na URL), para não permitir injeção de mensagem.
- **Pasta `06-execution-plan/`:** inexistente; ignorada e registrada.

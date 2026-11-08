# SPEC-23 — Tela de login e autenticação no frontend — nova feature

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `core/auth` + `features/login` |
| Prioridade | P1 |
| Depende de | SPEC-15, SPEC-16, SPEC-22 |
| Sucessoras | SPEC-17, SPEC-18, SPEC-19, SPEC-20, SPEC-21, SPEC-30, SPEC-41 |
| Paralelismo | nenhum com SPEC-17..20 (todas as rotas passam a ser protegidas) |

## 1. Objetivo
Implementar a **tela de login** (e-mail e senha) e a autenticação JWT no Angular: serviço de autenticação, armazenamento do token, interceptor, guards, tratamento de sessão expirada e saída. **O protótipo não tem tela de login** (EVIDÊNCIA: ausente em `Autorizador.dc.html`); o desenho é derivado dos tokens e do shell do protótipo (ADR-09), sem inventar regras.

**Tela `/login` (fora do shell):** painel de marca com o gradiente escuro do protótipo ("Autorizador · Procedimentos médicos") + cartão com **E-mail**, **Senha** (botão mostrar/ocultar com `aria-pressed`), botão **Entrar** (estado carregando), mensagem de erro acessível (`role="alert"`). Responsiva 320 px → desktop. `autocomplete="username"` e `current-password`; foco inicial no e-mail; Enter envia.
**Comportamentos:**
- Sucesso → guarda sessão e navega ao `retorno` (parâmetro `?retorno=`, **apenas caminho relativo interno validado**, proteção contra *open redirect*) ou `/painel`.
- `401 CREDENCIAIS_INVALIDAS` → "E-mail ou senha incorretos." (sem dizer qual); `429 MUITAS_TENTATIVAS` → mensagem com o tempo de espera (`Retry-After`); rede/servidor → modal padrão com nova tentativa; `400` → erros por campo.
- Qualquer `401` em rota protegida (`SESSAO_EXPIRADA`/`NAO_AUTENTICADO`) → encerra a sessão, limpa estado em memória (inclui contadores de `SessaoSolicitacoes`), vai a `/login?retorno=…` e mostra aviso "Sua sessão expirou. Entre novamente."
- **Sair** no shell (menu do usuário com e-mail e "Sair"): limpa tudo e volta ao login.
- Usuário já autenticado acessando `/login` → redireciona ao painel.
**Armazenamento do token (decisão, ADR-16):** `sessionStorage` (sobrevive a recarregar, some ao fechar a aba; não persiste entre sessões), nunca em `localStorage`, nunca em URL; leitura protegida por `try/catch`. Expiração conhecida pelo `exp` do token (decodificação **só para UX**; a verdade é o backend) com logout automático no vencimento. Alternativa descartada: cookie `HttpOnly` (exigiria mudar o backend; reavaliar em PEN-10).
**Restrições:** interceptor anexa `Authorization` **somente** a requisições para `apiBaseUrl`; nunca registrar token/senha em console; sem bibliotecas de autenticação; Reactive Forms tipados; sem lógica de autorização de negócio no cliente; sem "lembrar-me", recuperar senha ou cadastro (PEN-09).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` §6, `SECURITY_REQUIREMENTS.md` (SEC-13..SEC-20), `TARGET_ARCHITECTURE.md` §3, protótipo (tokens do shell e do hero), SPEC-15 (componentes) e SPEC-16 (erros/interceptors); skills `angular-module-scaffold`, `angular-architect`, `angular-tailwind-ui-specialist`, `ui-ux-pro-max`

## 3. Tarefas
- [ ] **T1.** `core/auth/auth.service.ts`: signals `usuario`, `token`, `autenticado`, `expiraEm`; `entrar(email, senha)`, `sair()`, restauração da sessão no início; agendamento do logout no `exp`.
- [ ] **T2.** `core/auth/auth.interceptor.ts` (funcional): anexa Bearer só à API; em `401` encerra sessão e redireciona; integra com `errorInterceptor` (SPEC-16) sem duplicar.
- [ ] **T3.** Guards funcionais: `authGuard` (`canMatch` em **todas** as rotas de funcionalidade) e `guestGuard` (login); função pura `retornoSeguro(url)` com testes (rejeita `//x`, `http://`, `javascript:`).
- [ ] **T4.** Feature `features/login`: rota `/login`, página, `doc.md`; erros por `code` via catálogo de mensagens.
- [ ] **T5.** Shell: menu do usuário (e-mail + Sair), acessível por teclado; rota de saída.
- [ ] **T6.** Tipos do contrato `autenticar/obterUsuarioAtual` (regerar `api-types.ts`) e testes de serviço com `HttpTestingController`.
- [ ] **T7.** Testes: login sucesso/credencial inválida/429/rede, guard bloqueia sem sessão e preserva `retorno`, 401 em rota protegida limpa sessão, `retornoSeguro`, logout limpa contadores, restauração após reload, expiração.
- [ ] **T8.** Atualizar `doc.md` e `README.md` do frontend (fluxo de autenticação, armazenamento, riscos).

**Aceite (G/W/T):** *Given* sem sessão · *When* abrir `/procedimentos` · *Then* redireciona a `/login?retorno=%2Fprocedimentos`. *When* login válido · *Then* volta a `/procedimentos` e o menu mostra o e-mail. *When* a API responde `401 SESSAO_EXPIRADA` · *Then* vai ao login com o aviso e **nenhum dado da sessão anterior** permanece. *When* `?retorno=//evil.com` · *Then* ignora e vai a `/painel`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T3, T5–T8 | `angular-module-scaffold`, `angular-architect` |
| T4 (visual), T5 | `angular-tailwind-ui-specialist`, `ui-ux-pro-max` |
| mensagens | `copywriting` |

## 5. Verificação obrigatória
- `npm run build && npm test && npm run lint` → verdes
- Execução contra o Compose (SPEC-10/13): fluxo do aceite com capturas em 320 e 1280 px
- `grep -rn "localStorage\|console.log" app-autorizador-processo/src/app/core/auth` → vazio
- Definition of Done: todas as rotas de funcionalidade protegidas; `401` tratado centralmente; sem vazamento de token; acessibilidade do formulário verificada.

## 6. Resultado
Concluída em 2026-11-08 pela etapa `18-login-frontend` do loop `test-01` (19 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/18-login-frontend/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.

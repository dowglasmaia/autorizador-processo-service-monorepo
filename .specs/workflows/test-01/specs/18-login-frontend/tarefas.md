# Tarefas — Login no frontend

## 1. Autenticação (core/auth)
- [ ] 1.1 Confirmar o que a etapa 17 entregou (`AppError`, `errorInterceptor`, `api-client`, `api-types.ts` com `autenticar`/`obterUsuarioAtual`) e ajustar a proposta.
- [ ] 1.2 Criar `core/auth/auth.service.ts` com signals `usuario`, `token`, `autenticado`, `expiraEm`, `entrar` e `sair`.
- [ ] 1.3 Persistir a sessão em `sessionStorage` (com `try/catch`), restaurá-la no início e agendar o logout no `exp`.
- [ ] 1.4 Criar `core/auth/auth.interceptor.ts` (Bearer só para `apiBaseUrl`; `401` encerra sessão e redireciona) e registrá-lo em `app.config.ts` sem duplicar o `errorInterceptor`.

## 2. Guards e rotas
- [ ] 2.1 Criar `retornoSeguro(url)` pura, rejeitando `//x`, `http://`, `javascript:` e barra invertida.
- [ ] 2.2 Criar `authGuard` (`canMatch`) e `guestGuard` funcionais, preservando `retorno`.
- [ ] 2.3 Aplicar `authGuard` às rotas `painel`, `procedimentos`, `solicitar` e `autorizacoes` e registrar `/login` fora do shell.

## 3. Tela de login
- [ ] 3.1 Criar `features/login` (rota, página, painel de marca + cartão) com formulário reativo tipado e componentes de `shared/ui`.
- [ ] 3.2 Implementar mostrar/ocultar senha (`aria-pressed`), estado de carregamento, foco inicial, `autocomplete` e envio por Enter.
- [ ] 3.3 Criar catálogo de mensagens por `code` (`CREDENCIAIS_INVALIDAS`, `MUITAS_TENTATIVAS` com `Retry-After`, `400` por campo, rede/servidor com modal de nova tentativa) e o aviso de sessão expirada.
- [ ] 3.4 Escrever `features/login/doc.md`.

## 4. Shell e logout
- [ ] 4.1 Adicionar ao shell o menu do usuário (e-mail + "Sair") acessível por teclado.
- [ ] 4.2 Fazer o logout limpar o estado em memória (incluindo contadores de solicitações, se existirem) e navegar ao login.

## 5. Testes e verificação
- [ ] 5.1 Testar `AuthService` com `HttpTestingController` (sucesso, 401, 429, rede, restauração após reload, expiração).
- [ ] 5.2 Testar interceptor (host externo sem Bearer, 401 limpa sessão), guards e `retornoSeguro`.
- [ ] 5.3 Testar a página de login e o menu do shell (acessibilidade básica, mensagens, redirecionamentos).
- [ ] 5.4 Atualizar `README.md` e `doc.md` do frontend (fluxo, armazenamento, riscos).
- [ ] 5.5 Rodar `npm run lint`, `npm run build`, `npm test` e o `grep` de `localStorage|console.log` em `core/auth`; corrigir falhas.
- [ ] 5.6 Registrar evidência por tarefa, resumo e `git mv` da spec para `.specs/archive/` conforme `como-executar.md`.

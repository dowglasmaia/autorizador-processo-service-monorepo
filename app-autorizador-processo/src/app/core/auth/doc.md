# Autenticação (core/auth)

- `auth.service.ts`: signals `usuario`, `token`, `autenticado`, `expiraEm`, `sessaoExpirada`; `entrar(credenciais)`, `sair()`, `registrarLimpeza(fn)` (ponto de extensão para limpar estado de outras features no logout).
- Armazenamento: `sessionStorage` com `try/catch`; restauração no início; logout agendado no `exp`. Nenhum outro armazenamento persistente.
- `auth.interceptor.ts`: Bearer só para URLs sob `apiBaseUrl` (relativa ou absoluta); `401` fora do login encerra a sessão e vai a `/login?retorno=…`. Registrado antes do `errorInterceptor`.
- `auth.guard.ts`: `authGuard` (`canMatch`) e `guestGuard`; `retorno-seguro.ts`: `retornoSeguro(url)` pura, devolve `/painel` para destinos inválidos.
- Riscos: token legível por XSS em `sessionStorage`; sem refresh token; a decodificação do JWT não valida assinatura.

# Login

Tela `/login`, fora do `Shell`. Autentica por e-mail e senha (`POST auth/login`) via `AuthService`.

## Peças

- `login.routes.ts`: rota carregada sob demanda; protegida por `guestGuard` (usuário autenticado vai ao painel).
- `pages/login-page.ts`: painel de marca (a partir de `lg`) e cartão com formulário reativo tipado.
- `mensagens-login.ts`: catálogo de mensagens decidido por `code` do `AppError` (nunca por texto).

## Comportamento

- Foco inicial no e-mail; Enter envia (formulário nativo); `autocomplete="username"`/`current-password`.
- Botão de mostrar/ocultar senha com `aria-pressed` e rótulo fixo "Mostrar senha".
- Envio mostra estado de carregamento no botão e bloqueia reenvio.
- Destino pós-login: `retorno` da URL, validado por `retornoSeguro` (padrão `/painel`).

## Mensagens

| Situação                  | Apresentação                                                          |
| ------------------------- | --------------------------------------------------------------------- |
| `CREDENCIAIS_INVALIDAS`   | alerta "E-mail ou senha incorretos." (sem dizer qual campo)           |
| `MUITAS_TENTATIVAS` / 429 | alerta com o tempo de `Retry-After`, quando houver                    |
| `400` com `errors[]`      | erro sob cada campo (`email`, `senha`)                                |
| rede / servidor           | modal com "Tentar novamente" (reenvia o formulário)                   |
| sessão expirada           | aviso `role="status"` acima do formulário, consumido no próximo login |

## Logout

Menu do usuário no shell (e-mail + "Sair"), acessível por teclado (Esc fecha). `AuthService.sair()` limpa
token, usuário e `sessionStorage`, executa as limpezas registradas por `registrarLimpeza` (hoje não há
contadores de solicitações no frontend) e navega a `/login`.

## Acessibilidade (etapa 24)

Textos vêm do catálogo `core/i18n/mensagens.ts`; a página é coberta por `src/app/a11y.spec.ts` (rótulos, nomes acessíveis, alvos ≥ 44 px). Detalhes e limitações em `shared/ui/doc.md`.

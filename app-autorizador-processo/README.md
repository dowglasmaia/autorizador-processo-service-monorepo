# app-autorizador-processo

Frontend do **Autorizador de Procedimentos Médicos**: Angular 21, TypeScript estrito, Tailwind CSS (via PostCSS), componentes standalone, zoneless, Signals e formulários reativos tipados.

## Pré-requisitos
- Node 20.19 ou superior (desenvolvido com Node 24) e npm.

## Comandos
| Comando | O que faz |
|---|---|
| `npm ci` | Instala as dependências a partir do `package-lock.json` |
| `npm start` | Servidor de desenvolvimento em `http://localhost:4200` |
| `npm run build` | Build de produção em `dist/` |
| `npm test` | Testes unitários (Vitest via `@angular/build:unit-test`), execução única |
| `npm run lint` | `angular-eslint` (exige `OnPush` e `inject()`) |

## Ambientes
- `src/environments/environment.ts` (produção): `apiBaseUrl = '/api/v1'` (mesma origem, atrás de proxy reverso).
- `src/environments/environment.development.ts`: `apiBaseUrl = 'http://localhost:8080/api/v1'`, aplicado por `fileReplacements` na configuração `development`.
- O valor é lido pelo token `API_BASE_URL` (`src/app/core/http/api-base-url.token.ts`). Não há proxy no `ng serve`: o CORS da origem 4200 é liberado pelo backend (perfil `local`).

## Estrutura
```
src/app/
├─ core/        infraestrutura transversal (fronteira HTTP, erros da API, autenticação)
├─ features/    uma pasta por funcionalidade (rotas, páginas, doc.md)
├─ app.config.ts, app.routes.ts, app.ts
src/environments/
```

## Convenções
- **`doc.md` por módulo:** cada pasta em `features/` mantém um `doc.md` junto ao código, descrevendo rotas, estado e decisões; atualize-o ao mudar o módulo.
- **`OnPush`** é o padrão (schematic no `angular.json` e regra de lint); componentes standalone.
- **Signals** para estado local e derivado; sem biblioteca de estado externa.
- **TypeScript estrito** e `strictTemplates` ativos.
- Textos de UI, mensagens e documentação em português.
- Tipos da API virão **gerados** do contrato OpenAPI do backend; não duplique DTOs à mão.

## Autenticação
- **Fluxo:** `/login` (e-mail e senha) → `POST /auth/login` → `AuthService` guarda o `TokenResponse`. O `authInterceptor` anexa `Authorization: Bearer` somente a URLs sob `apiBaseUrl`; o `authGuard` (`canMatch`) protege `painel`, `procedimentos`, `solicitar` e `autorizacoes`, e o `guestGuard` leva quem já entrou ao painel. O destino pós-login vem de `?retorno=`, validado por `retornoSeguro` (recusa `//x`, `http://`, `javascript:` e barra invertida).
- **Armazenamento:** a sessão fica em `sessionStorage` (some ao fechar a aba) e é restaurada no reload; o logout é agendado no `exp` do token (decodificação só para UX). Nada em armazenamento persistente, URL ou log.
- **Logout:** menu do usuário no shell ("Sair"); também ocorre em `401` fora do login, com o aviso "Sua sessão expirou. Entre novamente.".
- **Riscos aceitos:** token em `sessionStorage` é legível por XSS (cookie `HttpOnly` exigiria mudança no backend); sem refresh token (PEN-09/PEN-10); a validade real é sempre decidida pelo servidor.
- Detalhes: `src/app/core/auth/doc.md` e `src/app/features/login/doc.md`.

## Contêiner Nginx

O `Dockerfile` é multi-stage: `node:24-alpine` roda `npm ci` e `npm run build`; a imagem final é `nginxinc/nginx-unprivileged:stable-alpine` (usuário não-root, sem Node/npm) e contém só o `browser/` e a configuração em `nginx/`.

```bash
docker build -t app-autorizador-processo .      # imagem isolada (precisa da API `api` na rede para o proxy)
docker compose up -d --build                    # na raiz: banco + API + frontend em http://localhost:4200
```

- **Porta:** o Nginx escuta em `8080` dentro do contêiner; o Compose publica em `${APP_PORT:-4200}`.
- **Proxy:** `/api/` → `http://api:8080` (mesma origem, sem CORS; 404 da API não cai no `index.html`). Rotas da SPA (`/procedimentos`, `/login`…) usam `try_files` com fallback para `index.html`.
- **Healthcheck:** `GET /healthz` retorna `200 ok` (o Compose usa `wget` em `127.0.0.1`, pois `localhost` resolve para IPv6 na Alpine). `GET /actuator/health` repassa a saúde da API; os demais caminhos de `/actuator/` retornam 404.
- **Cache:** `index.html` sem cache; `.js`/`.css`/fontes com hash em `immutable` por 1 ano; imagens e favicon com 1 hora.
- **Cabeçalhos de segurança** (`nginx/security-headers.inc`, incluído em toda `location`): `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options: DENY` e CSP `default-src 'self'` (com `style-src 'unsafe-inline'` e `script-src-attr 'unsafe-inline'` exigidos pelo build do Angular).

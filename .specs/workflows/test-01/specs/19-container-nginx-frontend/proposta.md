# Contêiner Nginx do frontend

Etapa 19 de 31 · Fase 3 · derivada da `SPEC-13` (`.specs/03-frontend/SPEC-13-frontend-container-nginx.md`). Revisada contra o repositório após a entrega da etapa 18 (login).

## Objetivo
Empacotar o Angular em uma imagem Nginx sem Node que serve a SPA, faz proxy de `/api/` para o serviço `api` e fecha o serviço `app` do `compose.yaml` (porta 80 → `${APP_PORT:-4200}`), com cabeçalhos de segurança, cache correto e healthcheck.

## Ponto de partida (conferido no repositório após a etapa 18)
- `app-autorizador-processo/` não tem `Dockerfile`, `.dockerignore` nem `nginx/`. O build (`ng build`, `@angular/build:application`, sem SSR/prerender) gera `dist/app-autorizador-processo/browser` com `index.html`, `favicon.ico` e `chunk-*.js`/`main-*.js`/`styles-*.css` com hash; `dist/` local existe mas será ignorado pelo `.dockerignore`.
- `environment.ts` de produção usa `apiBaseUrl: '/api/v1'` (relativo, mesma origem); o de desenvolvimento aponta para `http://localhost:8080/api/v1`. Portanto o Nginx não exige CORS.
- `compose.yaml` já traz o serviço `app` (`build: ./app-autorizador-processo`, `${APP_PORT:-4200}:80`, `depends_on: api: service_healthy`, `restart`), porém **sem `healthcheck`** e com cabeçalho dizendo que o frontend "só existe após a etapa do frontend". `api` e `postgres` já têm healthcheck; `.env.example` já define `APP_PORT=4200`.
- O Dockerfile da API (`api-autorizador-processo/Dockerfile`) serve de referência de estilo (multi-stage, camada de dependências separada, comentários em português).
- O indicador de status da API no shell (`core/api-status`) faz `GET /actuator/health` (caminho relativo, `HEALTH_PATH`); a SPEC exige expor somente `location = /actuator/health`.
- Existe `scripts/gerar-api-types.mjs`; o build de produção não deve depender dele (tipos já versionados em `shared/api`).
- Rotas SPA atuais: `login`, `painel`, `procedimentos`, `solicitar`, `autorizacoes`, `**` — todas atendidas pelo fallback `try_files`; o `HEALTH_PATH` relativo depende do proxy `/actuator/health` do Nginx em produção.
- `.specs/06-execution-plan/` citada no `INDEX.md` não existe; desvio a registrar na evidência.

## Escopo
1. `Dockerfile` multi-stage: `node:24` (`npm ci` + `npm run build`) → `nginxinc/nginx-unprivileged` (não-root, porta 8080 interna ou ajuste equivalente) copiando só o `browser/`; `.dockerignore` (`node_modules`, `dist`, `.angular`, `.git`).
2. `nginx/default.conf`: raiz estática; `try_files $uri $uri/ /index.html`; `location /api/` com `proxy_pass http://api:8080` (cabeçalhos `Host`, `X-Forwarded-*`, timeouts) **sem** fallback para `index.html` (404 da API permanece 404); `gzip`; `location = /healthz` → 200 `ok`; `location = /actuator/health` (somente GET) proxied para a API; nenhum outro caminho do actuator.
3. Cache: `index.html` com `Cache-Control: no-store`/`no-cache`; arquivos com hash em `immutable` por 1 ano. Atenção ao `add_header` não herdado entre `location`s (repetir ou usar `include`).
4. Cabeçalhos: `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `X-Frame-Options: DENY`, CSP com `default-src 'self'` (ajustar apenas com evidência, ex.: estilos inline do Angular).
5. `compose.yaml`: `healthcheck` do `app` em `/healthz`, ajuste do mapeamento de porta se a imagem escutar em 8080, atualização do comentário de cabeçalho; manter `depends_on: api: service_healthy`.
6. Documentação no `README.md` do frontend e no raiz; evidência de verificação.

## Fora do escopo (fica para outras etapas)
- Pipeline de CI do frontend (SPEC-40/41; o plano proíbe CI nesta etapa) e publicação de imagem em registry.
- TLS/HTTPS, HSTS, deploy em nuvem e e2e completo contra o Compose (SPEC-30).
- Mudanças no código Angular, no backend ou no contrato OpenAPI.

## Critérios de aceitação
- *Given* `docker compose up -d --build` · *When* `GET http://localhost:4200/` · *Then* 200 com o app.
- `GET /procedimentos` (rota SPA) → 200 com `index.html`.
- `GET /api/v1/procedimentos` → resposta da API (401 sem token é aceitável); `GET /api/inexistente` → 404 da API, não `index.html`.
- `GET /healthz` → 200; `GET /actuator/health` → resposta da API; `GET /actuator/env` → não exposto.
- `curl -I /` mostra os cabeçalhos de segurança; `index.html` sem cache e assets com hash em cache longo.
- Os 3 serviços ficam `healthy`; imagem final sem Node/npm e processo não-root; nenhuma configuração de CORS necessária.
- `docker build app-autorizador-processo` e, em `app-autorizador-processo`, `npm run lint`, `npm run build` e `npm test` verdes. Sem Docker disponível: registrar bloqueio e não marcar como concluída.

## Decisões tomadas sem consulta
- **Imagem base:** `nginx-unprivileged` (usuário não-root) em vez de `nginx` comum, conforme a preferência da SPEC; mapeamento do compose será `${APP_PORT:-4200}:8080` se a imagem escutar em 8080.
- **Serviço `app` já existente no compose:** a etapa só o completa (healthcheck, comentários) em vez de recriá-lo.
- **Healthcheck:** `wget -qO- http://localhost:8080/healthz` (a imagem Alpine inclui `wget`; `curl` não será instalado).
- **Etapa 18 entregue:** conferido que o caminho de saída do build e o `apiBaseUrl` de produção (`/api/v1`) não mudaram; `/login` e demais rotas ficam cobertas pelo fallback. Nenhuma alteração de código Angular é prevista.
- **Arquivos hash:** o cache `immutable` será aplicado por extensão (`.js`, `.css`, fontes) e `index.html` terá `no-cache`; `favicon.ico` e demais arquivos sem hash ficam com cache curto.

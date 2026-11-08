# SPEC-13 — Contêiner do frontend com Nginx

| Campo | Valor |
|---|---|
| Domínio / projeto | Frontend · `app-autorizador-processo` + `compose.yaml` |
| Prioridade | P1 |
| Depende de | SPEC-14 (e SPEC-10 para o Compose) |
| Sucessoras | SPEC-30, SPEC-40, SPEC-41 |
| Paralelismo | junto com SPEC-15 e SPEC-03 |

## 1. Objetivo
Empacotar o Angular em imagem `nginx` que serve a SPA e faz proxy de `/api/` para o serviço `api`, fechando o serviço `app` do `compose.yaml` (porta 80 → `${APP_PORT:-4200}`).
**Restrições:** sem Node na imagem final; SPA com fallback para `index.html` **sem** mascarar 404 de `/api/`; `index.html` sem cache, arquivos com hash com cache longo; cabeçalhos de segurança; sem CORS (mesma origem); usuário não-root quando possível (`nginx-unprivileged` ou equivalente).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `TARGET_ARCHITECTURE.md` §1 e §4, `SECURITY_REQUIREMENTS.md` (SEC-06, 08), `compose.yaml`

## 3. Tarefas
- [ ] **T1.** `Dockerfile` multi-stage: `node` (build `npm ci && npm run build`) → `nginx`; `.dockerignore`.
- [ ] **T2.** `nginx/default.conf`: raiz estática, `try_files $uri $uri/ /index.html`, `location /api/ { proxy_pass http://api:8080; }` com cabeçalhos `Host`, `X-Forwarded-*`, timeouts; `gzip`; `location = /healthz { return 200 'ok'; }`; proxy **somente leitura** de `location = /actuator/health` para a API (usado pelo indicador de status da API no shell; nenhum outro caminho do actuator é exposto).
- [ ] **T3.** Cabeçalhos: `X-Content-Type-Options: nosniff`, `Referrer-Policy: strict-origin-when-cross-origin`, `X-Frame-Options: DENY`, CSP (coerente com fontes/ícones empacotados de SPEC-15 — `default-src 'self'`; ajustar apenas com evidência).
- [ ] **T4.** `compose.yaml`: `healthcheck` do `app` em `/healthz`; manter `depends_on: api: service_healthy`.
- [ ] **T5.** Documentar no `README.md` do frontend e no raiz.

**Aceite:** *Given* `docker compose up -d --build` · *When* `GET http://localhost:4200/` · *Then* 200 com o app; `GET /procedimentos` (rota SPA) → 200 `index.html`; `GET /api/v1/procedimentos` → resposta da API; `GET /api/inexistente` → 404 da API (não `index.html`).

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5 | Docker/Nginx; `angular-architect` (build de produção) |

## 5. Verificação obrigatória
- `docker build app-autorizador-processo` → sucesso
- `docker compose up -d --build` → 3 serviços `healthy`; `curl -i localhost:4200/api/v1/procedimentos` → `200`; `curl -I localhost:4200/` → cabeçalhos de segurança presentes
- Definition of Done: comportamento do aceite comprovado, sem CORS necessário, imagem sem ferramentas de build.

## 6. Resultado
_Preencher ao concluir._

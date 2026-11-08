# Tarefas — Contêiner Nginx do frontend

## 1. Imagem
- [ ] 1.1 Confirmar o diretório de saída do `ng build` (`dist/.../browser`) e que `npm ci && npm run build` funcionam em contêiner sem o gerador de tipos.
- [ ] 1.2 Criar `app-autorizador-processo/.dockerignore` (`node_modules`, `dist`, `.angular`, `.git`, `.env*`).
- [ ] 1.3 Criar `app-autorizador-processo/Dockerfile` multi-stage (`node:24` → `nginx-unprivileged`), copiando só o `browser/` e a configuração do Nginx.

## 2. Configuração do Nginx
- [ ] 2.1 Criar `nginx/default.conf` com raiz estática, `try_files $uri $uri/ /index.html` e `gzip`.
- [ ] 2.2 Adicionar `location /api/` com `proxy_pass http://api:8080`, cabeçalhos `Host`/`X-Forwarded-*` e timeouts, sem fallback para `index.html`.
- [ ] 2.3 Adicionar `location = /healthz` (200 `ok`) e `location = /actuator/health` somente leitura para a API, sem expor outros caminhos do actuator.
- [ ] 2.4 Configurar cache: `index.html` sem cache e arquivos com hash `immutable` de longa duração.
- [ ] 2.5 Configurar `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options` e CSP `default-src 'self'`, garantindo que os cabeçalhos valham em todas as `location`s.

## 3. Compose
- [ ] 3.1 Adicionar `healthcheck` ao serviço `app` em `/healthz`, ajustar a porta publicada e manter `depends_on: api: service_healthy`.
- [ ] 3.2 Atualizar o comentário de cabeçalho do `compose.yaml` (o serviço `app` agora existe).

## 4. Verificação
- [ ] 4.1 Rodar `docker build app-autorizador-processo` e confirmar que a imagem final não contém Node/npm e roda como não-root.
- [ ] 4.2 Subir `docker compose up -d --build` e confirmar os 3 serviços `healthy`.
- [ ] 4.3 Verificar com `curl` `/`, `/procedimentos`, `/api/v1/procedimentos`, `/api/inexistente`, `/healthz`, `/actuator/health` e bloqueio de `/actuator/env`.
- [ ] 4.4 Verificar cabeçalhos de segurança e de cache (`curl -I`) e carregar o app no navegador sem erros de CSP no console.
- [ ] 4.5 Rodar `npm run lint`, `npm run build` e `npm test` em `app-autorizador-processo`; derrubar o Compose ao final (`docker compose down`).

## 5. Documentação e encerramento
- [ ] 5.1 Documentar no `README.md` do frontend e no raiz (como subir, portas, proxy, cabeçalhos, healthcheck).
- [ ] 5.2 Registrar evidência por tarefa e o desvio da pasta `06-execution-plan`, e arquivar a spec conforme `como-executar.md`.

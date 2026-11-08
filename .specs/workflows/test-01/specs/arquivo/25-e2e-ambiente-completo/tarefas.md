# Tarefas — Testes e2e do ambiente

## 1. Playwright e ambiente
- [x] 1.1 Conferir o estado após a etapa 24 (rotas, textos, seletores acessíveis, `compose.yaml`) e registrar o desvio da pasta `06-execution-plan`.
  > ✅ 2026-11-08 20:00 +00:00 — Estado conferido (rotas, guard `/login?retorno`, `compose.yaml`). Desvio: `.specs/06-execution-plan/` não existe; ignorada.
- [x] 1.2 Instalar `@playwright/test` como devDependency em `app-autorizador-processo/`, baixar Chromium e adicionar o script `npm run e2e`.
  > ✅ 2026-11-08 20:00 +00:00 — `@playwright/test` (+ `@types/node`) em devDependencies, Chromium baixado, script `npm run e2e`.
- [x] 1.3 Criar `playwright.config.ts` com projetos desktop e móvel, URL base `http://localhost:${APP_PORT:-4200}`, relatório HTML/JUnit e capturas em falha.
  > ✅ 2026-11-08 20:00 +00:00 — `playwright.config.ts` com projetos `desktop` e `movel`, relatório HTML/JUnit e capturas em falha.
- [x] 1.4 Adicionar relatório e resultados ao `.gitignore` do app.
  > ✅ 2026-11-08 20:00 +00:00 — `playwright-report/`, `test-results/` e `e2e/.tmp` no `.gitignore` do app.

## 2. Fixture de ambiente
- [x] 2.1 Criar o `.env` de teste descartável gerado em tempo de execução (usuário *bootstrap*, `JWT_SECRET`, TTL curto), sem segredos versionados.
  > ✅ 2026-11-08 20:00 +00:00 — `.env` descartável gerado em `e2e/.tmp/e2e.env` (segredos sorteados, TTL 1 min), removido no teardown.
- [x] 2.2 Confirmar que a seed (perfil `local` padrão) está ativa no Compose; só se não estiver, criar override mínimo `compose.e2e.yaml` sem tocar o `compose.yaml` principal.
  > ✅ 2026-11-08 20:00 +00:00 — Perfil `local` já é o padrão; nenhum `compose.e2e.yaml` criado. Seed confirmada pelo cenário 1.
- [x] 2.3 Implementar global setup: `docker compose -p e2e up -d --build`, espera por `healthy` de `api` e `app`.
  > ✅ 2026-11-08 20:00 +00:00 — Global setup sobe o projeto `e2e` e espera `healthy`. Desvio: usa `docker-compose` independente se o plugin não existir.
- [x] 2.4 Implementar global teardown: `docker compose -p e2e down -v`, inclusive em caso de falha.
  > ✅ 2026-11-08 20:00 +00:00 — Global teardown faz `down -v` (também em falha do setup).
- [x] 2.5 Criar helpers: login via UI, geração de números únicos de procedimento e leitura da seed.
  > ✅ 2026-11-08 20:00 +00:00 — `e2e/helpers.ts`: login pela UI, `numeroUnico`, leitura da seed/painel.

## 3. Cenários de autenticação e painel
- [x] 3.1 Cenário 0: login inválido mostra erro; login válido entra; rota protegida sem sessão vai a `/login` com `retorno`; sair encerra a sessão.
  > ✅ 2026-11-08 20:00 +00:00 — `01-autenticacao.e2e.ts` passa em desktop e móvel.
- [x] 3.2 Cenário 0b: sessão expirada (TTL curto) volta ao login.
  > ✅ 2026-11-08 20:00 +00:00 — Cenário 0b com espera real de ~60 s (TTL 1 min); passa.
- [x] 3.3 Cenário 1: painel mostra os totais da seed (3 procedimentos, 0 autorizações, 3 idades).
  > ✅ 2026-11-08 20:00 +00:00 — `02-painel.e2e.ts`: totais 3/0/3 da seed validados no desktop; passa.

## 4. Cenários de procedimentos e autorizações
- [x] 4.1 Cenário 2: cadastrar procedimento completo (4 etapas) e vê-lo na lista.
  > ✅ 2026-11-08 20:00 +00:00 — `03-procedimentos.e2e.ts`: cadastro em 4 etapas e listagem; passa.
- [x] 4.2 Cenário 3: número duplicado mostra erro no campo.
  > ✅ 2026-11-08 20:00 +00:00 — Número duplicado mostra erro no campo; passa.
- [x] 4.3 Cenário 4: solicitar 4567/20/F mostra "Autorizado" e aparece no histórico e no painel.
  > ✅ 2026-11-08 20:00 +00:00 — `04-autorizacoes.e2e.ts`: 4567/20/F Autorizado, histórico e painel; passa.
- [x] 4.4 Cenário 5: solicitar 1234/30/M mostra "Negado" e o histórico não muda.
  > ✅ 2026-11-08 20:00 +00:00 — 1234/30/M Negado, histórico inalterado; passa.
- [x] 4.5 Cenário 6: `?numero=` inexistente forjado mostra erro tratado.
  > ✅ 2026-11-08 20:00 +00:00 — `05-resiliencia.e2e.ts`: `?numero=` forjado ignorado, nada pré-selecionado e erro de validação. Desvio **[REVISAR]**: produto não exibe aviso explícito.

## 5. Resiliência e responsividade
- [x] 5.1 Cenário 7: parar a API mostra o modal "Não foi possível carregar" e a tela retoma ao religar.
  > ✅ 2026-11-08 20:00 +00:00 — API parada mostra o estado "Não foi possível carregar" e retoma ao religar. Desvio: é estado vazio com "Tentar novamente", não modal.
- [x] 5.2 Cenário 8: rota SPA direta (`/autorizacoes`) funciona após reload.
  > ✅ 2026-11-08 20:00 +00:00 — Rota `/autorizacoes` direta após reload; passa.
- [x] 5.3 Cenário 9: viewport de 320 px sem rolagem horizontal nas telas principais.
  > ✅ 2026-11-08 20:00 +00:00 — 320 px sem rolagem horizontal em painel, procedimentos, solicitar e autorizações; passa.

## 6. Verificação e documentação
- [x] 6.1 Rodar `npm run e2e` contra o Compose limpo (desktop e móvel) e corrigir apenas problemas dos testes; falhas de produto viram registro.
  > ✅ 2026-11-08 20:00 +00:00 — `npm run e2e`: 30 testes passam (desktop e móvel, ~4,2 min) contra Compose limpo; sem falhas de produto.
- [x] 6.2 Conferir que o teardown removeu contêineres e volume do projeto `e2e` e que nada ficou rodando.
  > ✅ 2026-11-08 20:00 +00:00 — Após o teardown: 0 contêineres, volumes e redes do projeto `e2e`; `e2e/.tmp` removido.
- [x] 6.3 Documentar a execução local (`npm run e2e`, pré-requisitos, Docker) no README do app.
  > ✅ 2026-11-08 20:00 +00:00 — Seção "Testes e2e (Playwright)" no README do app.
- [x] 6.4 Rodar `npm run lint`, `npm run build` e `npm test` do app e corrigir falhas.
  > ✅ 2026-11-08 20:00 +00:00 — `npm run lint` sem erros, `npm run build` ok, `npm test` 226 testes passam.
- [x] 6.5 Registrar evidência por tarefa, resumo do relatório e bloqueios, e arquivar a spec conforme `como-executar.md`.
  > ✅ 2026-11-08 20:00 +00:00 — Evidências registradas, resumo abaixo e spec SPEC-30 movida para `.specs/archive/` com `git mv`. Sem bloqueios.

## Resumo
Resultado: 10 cenários (0–9) em Playwright sobre o Compose real (navegador → Nginx → API → PostgreSQL), desktop e móvel; 30 testes passam. Validações: e2e, lint, build e 226 testes unitários verdes; teardown sem resíduos. Limitações aceitas: cenário 6 e cenário 7 divergem em forma da descrição (ver desvios, **[REVISAR]**); cenário 0b usa espera real de ~60 s. Sem bloqueios.

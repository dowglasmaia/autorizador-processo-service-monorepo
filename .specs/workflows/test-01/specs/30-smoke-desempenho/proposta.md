# Smoke de desempenho

Etapa 30 de 31 · Fase 4 · derivada da `SPEC-42` (`.specs/05-quality/SPEC-42-performance-smoke.md`), com referência a `.specs/05-quality/NON_FUNCTIONAL_REQUIREMENTS.md` (NFR-02..04) e dependência da SPEC-30 (ambiente completo). Revisada contra a base entregue pela etapa 29 (SPEC-41 arquivada).

## Objetivo
**Medir** (não presumir) o desempenho no ambiente do Compose contra as metas provisórias NFR-02..04 e, só se alguma falhar, aplicar o menor ajuste com evidência (`EXPLAIN` e remedição). Registrar baseline, números e conclusões em `.specs/05-quality/PERFORMANCE_REPORT.md`. Sem mudança de comportamento e sem teste de carga pesado (limite do plano).

## Ponto de partida (conferido contra o código atual, após a etapa 29)
- `.specs/06-execution-plan/` não existe (desvio a registrar na evidência). `NON_FUNCTIONAL_REQUIREMENTS.md` e `SPEC-42` estão em `.specs/05-quality/`; `PERFORMANCE_REPORT.md` ainda não existe. O `DATABASE_DESIGN.md` §3 citado pela SPEC deve ser localizado em `.specs/` na execução.
- `compose.yaml` na raiz com `postgres` (16-alpine), `api` (readiness em `/actuator/health/readiness`; exige `JWT_SECRET`; usuário inicial via `AUTH_BOOTSTRAP_EMAIL`/`AUTH_BOOTSTRAP_SENHA`) e `app` (Nginx na porta 4200 com proxy `/api/`).
- Migrations Flyway V1..V4. Único índice secundário em V1: `ix_autorizacao_procedimento (procedimento_id)`. Listas paginadas por `page`/`size` (padrão 20, máximo 200) em `/procedimentos` e `/autorizacoes`; `/painel/resumo` agrega no banco. Rotas: `/api/v1/auth/login`, `/procedimentos`, `/autorizacoes`, `/painel/resumo`.
- `tools/` tem `contract`, `parity` e `security` (`sondagem.sh` + `sondagem.mjs`): o `.sh` sobe um Compose **descartável** (projeto, portas e `.env` próprios, segredos aleatórios) e derruba tudo com o volume, mesmo em falha. `tools/perf/` segue esse padrão. Não há Lighthouse nas dependências do frontend.
- Contramedidas da etapa 29 confirmadas: `LimitadorTentativasAdapter` (5 falhas/15 min por chave, em memória), `limit_req zone=login` no Nginx (`nginx/limites.conf.template`, só em `/api/v1/auth/login`) e `client_max_body_size`. A carga **não pode dispará-los**: faz **um** login válido e reutiliza o token; o desempenho da API é medido também direto na porta da API, e o Nginx só é usado para o bundle/LCP. Gzip já está ligado no Nginx.

## Escopo
1. **Dados sintéticos** (T1): script reproduzível em `tools/perf/` que gera 1.000 procedimentos e 10.000 autorizações no PostgreSQL do Compose, com dados fictícios, idempotente e sem dados reais. Os dados ficam em volume descartável (`docker compose down -v` ao fim).
2. **Carga HTTP simples** (T2): 20 conexões, 60 s por endpoint, em `GET /procedimentos`, `POST /autorizacoes`, `GET /autorizacoes` e `GET /painel/resumo`, com aquecimento prévio. Registrar p50/p95/p99, throughput e taxa de erro. Ferramenta sem instalação global (script Node 24 com `fetch`/`undici` ou contêiner efêmero); nada fica rodando depois.
3. **N+1 e planos** (T3): contar SQL por requisição nas listas (tamanhos de página diferentes → contagem constante; por log/estatísticas do Hibernate em teste ou `pg_stat_statements`) e `EXPLAIN (ANALYZE, BUFFERS)` das consultas do painel e das listas.
4. **Web** (T4): medir tamanho gzip do bundle inicial (build de produção) contra 250 kB e LCP em perfil móvel simulado (Lighthouse CLI via `npx`, sem adicionar dependência ao projeto, ou equivalente) servido pelo Nginx do Compose.
5. **Ajuste mínimo se houver falha** (T5): diagnóstico → menor correção (índice por migration **nova** V5, `@BatchSize`/fetch, lazy route, tamanho de página) → remedição. Nenhum ajuste sem número antes e depois.
6. **Relatório** (T6): `PERFORMANCE_REPORT.md` com ambiente (máquina, versões, parâmetros), baseline, resultados, conclusões e, se a base medida justificar, ajuste das metas em `NON_FUNCTIONAL_REQUIREMENTS.md`.

## Fora do escopo
- Testes de carga pesados, de estresse/soak, escala horizontal, tuning de JVM/pool além do necessário e monitoramento contínuo (limite do plano).
- Mudança de contrato OpenAPI, de comportamento ou de regras de negócio; novas funcionalidades.
- Rodar desempenho no CI (etapa 28) ou publicar imagens/deploy.
- Dados reais; migração de dados (SPEC-50).
- Documentação final e fechamento do projeto: SPEC-43, fica para a etapa 31.

## Critérios de aceitação
- `PERFORMANCE_REPORT.md` com medições reais executadas (sem estimativas), incluindo baseline antes de qualquer otimização.
- NFR-02 (p95 ≤ 200 ms em `GET /procedimentos` e `POST /autorizacoes` com 1.000/10.000 registros e 20 conexões), NFR-03 (SQL por requisição constante nas listas) e NFR-04 (bundle ≤ 250 kB gz, LCP ≤ 2,5 s) atendidos, ou justificados com plano.
- Qualquer índice ou consulta alterado está justificado por `EXPLAIN` e ganho medido; `./mvnw verify`, `npm run lint`, `npm run build` e `npm test` verdes após ajustes.
- Nenhum processo, contêiner ou volume deixado ativo. Se o Docker não estiver disponível, as medições no Compose ficam bloqueadas e registradas, sem marcar como concluídas.

## Decisões tomadas sem consulta
- **Ferramenta de carga:** script próprio em `tools/perf/` (Node, sem dependência nova) em vez de instalar k6/wrk, pela regra de não instalar dependências. Se o gerador de carga virar gargalo, registrar e trocar por contêiner efêmero. **[REVISAR]**
- **Lighthouse:** via `npx` pontual, sem alterar `package.json`; se indisponível, usar medição equivalente (tamanho gz do build + trace de carga) e registrar a limitação. **[REVISAR]**
- **Compose descartável próprio** (padrão de `tools/security/sondagem.sh`: projeto, portas e `.env` temporários, `down -v` em `trap`), sem tocar no `.env` nem nos contêineres do desenvolvedor. **[REVISAR]**
- **Dados sintéticos por SQL (`generate_series`) via `docker compose exec postgres psql`**, e não pela API, para não acionar o limitador de login; `POST /autorizacoes` é medido pela API. **[REVISAR]**
- Metas só são alteradas se a medição justificar, com o ajuste registrado no relatório e em `NON_FUNCTIONAL_REQUIREMENTS.md`.
- Desvio registrado: pasta `.specs/06-execution-plan/` inexistente.

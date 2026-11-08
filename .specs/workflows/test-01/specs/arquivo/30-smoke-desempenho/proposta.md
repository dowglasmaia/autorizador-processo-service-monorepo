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

## Evidência do lote 1 (tarefas 1.1 a 3.3)
- **Desvio:** `.specs/06-execution-plan/` não existe; usados `.specs/05-quality/` (SPEC-42, NFR) e `.specs/01-architecture/DATABASE_DESIGN.md` (§3).
- **Etapa 29 conferida:** `app-autorizador-processo/nginx/limites.conf.template`, `limit_req` só em `/auth/login` e `LimitadorTentativasAdapter`. A carga faz **1 login** e fala direto com a API (porta 18091), sem Nginx. Docker 29.9.2 disponível.
- **Criado:** `tools/perf/` (`perf.sh`, `dados.sql`, `carga.mjs`, `README.md`). Compose descartável `smoke-desempenho` (portas 18091/15441, perfil `prod`, segredos aleatórios), `down -v` em `trap`; confirmado 0 contêineres restantes.
- **Dados (2.1/2.2):** `generate_series`; contagens 1000 | 10000 e idem após reexecução (idempotente).
- **Baseline (3.2)** — 20 conexões, 60 s, 5 s de aquecimento, sem erros; máquina 16 CPU / 12 GB no Docker, gerador de carga e API na mesma máquina; páginas de 20 itens, banco e API em contêineres:

| Endpoint | req | rps | p50 | p95 | p99 |
|---|---|---|---|---|---|
| GET /procedimentos | 44866 | 747,6 | 23,8 ms | 48,3 ms | 69,5 ms |
| GET /autorizacoes | 33831 | 563,6 | 34,1 ms | 54,1 ms | 69,0 ms |
| GET /painel/resumo | 31228 | 520,2 | 37,1 ms | 54,7 ms | 68,5 ms |
| POST /autorizacoes | 36027 | 600,3 | 31,8 ms | 48,2 ms | 64,2 ms |

- **Comparação com NFR-02 (3.3):** p95 ≤ 200 ms **passa** em `GET /procedimentos` (48,3 ms) e `POST /autorizacoes` (48,2 ms); os demais também ficam abaixo. Taxa de erro 0 %. O POST roda por último e acrescenta ~36 mil linhas ao final, sem afetar as leituras já medidas.
- **Decisão [REVISAR]:** o `POST` retorna 201 mesmo quando a autorização é negada (`permitido=false`), por isso a taxa de erro conta só status ≠ 201.

## Evidência do lote 2 (tarefas 4.1 a 7.1)
- **4.1/6.x (NFR-03):** o modo `perf.sh consultas` conta SQL pelo log do PostgreSQL. Antes, `GET /procedimentos` ia de 4 a 12 SQL (size 5→200) e `GET /autorizacoes` de 5 a 11, por causa de `@BatchSize(50)` nas coleções `sexos`/`idades`. Ajuste: `@BatchSize(200)` (= tamanho máximo de página); depois, 4 e 5 constantes. Teste de regressão novo em `ProcedimentoConcorrenciaDesempenhoTest`; os testes de desempenho de listagem passam.
- **4.2:** `EXPLAIN (ANALYZE, BUFFERS)` das listas e do painel registrados no relatório; todas < 3 ms, nenhum índice (sem V5).
- **5.1:** bundle inicial 542,5 kB bruto / 131,3 kB gzip (meta 250 kB). **5.2:** Lighthouse 13.6.0 via `npx` + Chrome local contra o Nginx do Compose (`perf.sh web`): LCP 0,6–0,7 s.
- **7.1:** `.specs/05-quality/PERFORMANCE_REPORT.md` escrito; metas do `NON_FUNCTIONAL_REQUIREMENTS.md` mantidas.
- Nenhum contêiner restante (`docker ps -a --filter name=smoke-desempenho` = 0).

### Decisões adicionais do lote 2
- Contagem de SQL pelo log do Postgres (`log_statement=all`, só no Compose descartável) em vez de `pg_stat_statements`, que exigiria alterar o `compose.yaml`. **[REVISAR]**
- `@BatchSize` 50→200 tratado como "menor ajuste" para NFR-03, pois a contagem não era constante acima de 50 itens, mesmo sem falha de latência. **[REVISAR]**
- LCP medido só em `/login` (rota sem sessão). **[REVISAR]**

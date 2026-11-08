# SPEC-10 — Ambiente de desenvolvimento com Docker Compose (PostgreSQL + API)

| Campo | Valor |
|---|---|
| Domínio / projeto | Integração · raiz (`compose.yaml`, `.env.example`) + `api-autorizador-processo/Dockerfile` |
| Prioridade | P1 |
| Depende de | SPEC-03, SPEC-04 |
| Sucessoras | SPEC-13, SPEC-30, SPEC-40 |
| Paralelismo | junto com SPEC-06/SPEC-16 |

## 1. Objetivo
Tornar o `compose.yaml` e o `.env.example` existentes (não rastreados) **executáveis e verificados**: Dockerfile da API, `healthcheck` do `api`, valores padrão e documentação. Hoje o `app` exige `api: service_healthy`, mas o serviço `api` **não define `healthcheck`** → o Compose falha. Preservar o que já existe: nome `autorizador-processo`, `postgres:16-alpine`, volume `postgres-data`, variáveis `DB_URL/DB_USERNAME/DB_PASSWORD/APP_ENV/LOG_LEVEL_APP`, portas `API_PORT`/`APP_PORT`.
**Restrições:** não apagar o volume do usuário; `.env` real ignorado pelo git; imagem final sem root, sem ferramentas de build; testes de integração **não** dependem do Compose (Testcontainers).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `compose.yaml`, `.env.example`, `TARGET_ARCHITECTURE.md` §1, `SECURITY_REQUIREMENTS.md` (SEC-01, 08)

## 3. Tarefas
- [ ] **T1.** `api-autorizador-processo/Dockerfile` multi-stage (build Maven com JDK 25 → runtime JRE 25 enxuto), usuário não-root, `EXPOSE 8080`, `.dockerignore`.
- [ ] **T2.** `compose.yaml`: adicionar `healthcheck` ao `api` (consulta a `/actuator/health` (scaffold) por ferramenta presente na imagem; SPEC-11 refina para `readiness`), `start_period` adequado; repassar ao `api` as variáveis de autenticação (`JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA`); manter `postgres` com `pg_isready`.
- [ ] **T3.** Corrigir o cabeçalho de comentários do compose: referências a "SPEC-10 §1; SPEC-13 §4" passam a apontar para SPEC-10 e SPEC-13 deste conjunto; incluir comando para subir só o banco.
- [ ] **T4.** `.env.example`: manter chaves existentes; acrescentar `CORS_ALLOWED_ORIGINS` (vazio no compose), **`JWT_SECRET`**, `JWT_TTL_MINUTOS` (60), `AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA` — valores **apenas de desenvolvimento**, claramente marcados no arquivo; `JWT_SECRET` é obrigatório (sem padrão no `compose.yaml`: `${JWT_SECRET:?defina JWT_SECRET no .env}`); nenhum segredo de produção.
- [ ] **T5.** Documentar no `README.md` raiz: `docker compose up -d postgres api`, portas, limpeza (`down`, `down -v`).
- [ ] **T6.** Verificar `docker compose config` e subida de `postgres` + `api`.

**Aceite:** *Given* Docker · *When* `docker compose up -d --build postgres api` · *Then* `postgres` e `api` ficam `healthy` em ≤ 90 s e `curl localhost:8080/actuator/health` retorna `UP`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | Docker/Compose; `java-spring-scaffold` (convenções de execução); nenhuma skill de UI |

## 5. Verificação obrigatória
- `docker compose config` → sem erros (serviço `app` ainda sem Dockerfile só é construído em SPEC-13; usar `up postgres api`)
- `docker compose up -d --build postgres api && docker compose ps` → ambos `healthy`
- `docker compose down` → remove contêineres; volume preservado
- Definition of Done: comportamento reproduzível a partir de checkout limpo; nenhum segredo versionado.

## 6. Resultado
_Preencher ao concluir._

# Docker Compose do backend

Etapa 13 de 31 · Fase 2 · derivada da `SPEC-10` (`.specs/04-integration/SPEC-10-ambiente-docker-compose.md`). Proposta escrita antecipadamente, enquanto a etapa 12 (config e observabilidade) é implementada; será revisada contra o que ela entregar.

## Objetivo
Tornar o `compose.yaml` e o `.env.example` da raiz **executáveis e verificados** para PostgreSQL + API: Dockerfile da API (`eclipse-temurin:25`), `healthcheck` do serviço `api`, repasse das variáveis de autenticação e documentação de uso. Hoje o serviço `app` exige `api: service_healthy`, mas o `api` não tem `healthcheck`, e a pasta `api-autorizador-processo/` não tem `Dockerfile`; com isso `docker compose up` do ambiente completo falha.

## Ponto de partida (conferido no repositório)
- `compose.yaml` e `.env.example` já estão versionados na raiz. O compose define `postgres` (`postgres:16-alpine`, `pg_isready`, volume `postgres-data`), `api` (build em `./api-autorizador-processo`, `DB_URL/DB_USERNAME/DB_PASSWORD/APP_ENV/LOG_LEVEL_APP`, porta `API_PORT`) e `app` (build em `./app-autorizador-processo`, que ainda não existe).
- O `api` **não** repassa `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA` nem `CORS_ALLOWED_ORIGINS`, embora o `application.yml` os consuma e a API não inicie sem `JWT_SECRET` de no mínimo 32 bytes.
- O `.env.example` já traz essas chaves, mas `JWT_SECRET` está vazio e sem aviso de "só desenvolvimento"; o cabeçalho do compose cita "SPEC-10 §1; SPEC-13 §4" (referências do conjunto antigo).
- Não há `Dockerfile` nem `.dockerignore` no backend. O `pom.xml` usa o Maven Wrapper (`mvnw`, `.mvn/`) e `spring-boot-starter-actuator`; `application.yml` expõe `health,info,metrics` e `SecurityConfig` libera `/actuator/health/**`. O perfil padrão do Spring é `local`.
- A etapa 12 (em andamento) altera `application*.yml`: `SERVER_PORT`, exposição só de `health,info` e grupos `liveness`/`readiness`. Esta etapa precisa seguir o que ela entregar (ver decisões).

## Escopo
1. `api-autorizador-processo/Dockerfile` multi-stage: build com JDK 25 (`eclipse-temurin:25-jdk`) via `./mvnw -DskipTests package`; runtime `eclipse-temurin:25-jre`, usuário não-root, `EXPOSE 8080`, sem ferramentas de build. `.dockerignore` (`target/`, `.git`, `.env`, IDE).
2. `compose.yaml`: `healthcheck` no `api` (ferramenta presente na imagem de runtime, `start_period` adequado), repasse de `JWT_SECRET` (obrigatório: `${JWT_SECRET:?...}`), `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA` e `CORS_ALLOWED_ORIGINS`; `postgres` mantido com `pg_isready`.
3. Cabeçalho do compose corrigido (referências a SPEC-10/SPEC-13), com o comando para subir só o banco e para subir `postgres api`.
4. `.env.example`: manter as chaves; marcar como valores **somente de desenvolvimento**; nenhum segredo real.
5. `README.md` raiz: `docker compose up -d --build postgres api`, portas, `down` e `down -v`.
6. Verificação com Docker: `docker compose config` e subida real de `postgres` + `api`.

## Fora do escopo (fica para outras etapas)
- Serviço `app` (Nginx, Dockerfile do frontend): fica para a SPEC-13 (frontend); o compose mantém o bloco `app` sem construí-lo nesta etapa.
- CI por projeto (SPEC-40), e2e no ambiente completo (SPEC-30), hardening e varredura de segredos (SPEC-41).
- Refinar health para `readiness` além do que a etapa 12 entregar; publicação de imagens em registry; deploy.
- Alterar código Java, contrato OpenAPI ou migrations.

## Critérios de aceitação
- `docker compose config` sem erros (com `JWT_SECRET` definido; sem ele, falha com a mensagem pedida).
- `docker compose up -d --build postgres api` → `postgres` e `api` `healthy` em até 90 s; `curl localhost:8080/actuator/health` retorna `UP`.
- Contêiner da API roda sem root; imagem final sem Maven/JDK de build.
- `docker compose down` remove contêineres e **preserva** o volume `postgres-data`.
- Nenhum segredo real versionado; `.env` ignorado pelo git; `./mvnw verify` do backend continua verde (sem alteração de código).
- Sem Docker disponível: registrar bloqueio nas tarefas de verificação e não marcá-las.

## Decisões tomadas sem consulta
- **Healthcheck por `wget`/`curl`?** A imagem `eclipse-temurin:25-jre` (Ubuntu) traz `curl`? Não é garantido; decisão provisória: conferir na execução e, se faltar, instalar `curl` na camada de runtime ou usar um `java` mínimo; registrar o escolhido. [REVISAR]
- **Endpoint do healthcheck:** usar `/actuator/health` agora e trocar para `/actuator/health/readiness` se a etapa 12 entregar o grupo, para que o compose siga o que a API realmente expõe. Revisar na reconciliação.
- **`JWT_SECRET` obrigatório no compose (`:?`)**, conforme a SPEC-10, e valor de desenvolvimento só em `.env.example` com aviso; o `.env.example` hoje o deixa vazio e passa a trazer um valor claramente marcado como de desenvolvimento (≥ 32 bytes). [REVISAR]
- **Porta:** o contêiner continua ouvindo em 8080; se a etapa 12 trocar `PORT` por `SERVER_PORT`, o compose não precisa mudar enquanto o padrão for 8080.
- **Perfil do contêiner:** o perfil padrão é `local`; manter, sem criar perfil novo nesta etapa, e deixar a revisão para a etapa 12 e a SPEC-41.

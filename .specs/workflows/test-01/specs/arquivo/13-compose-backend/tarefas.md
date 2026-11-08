## 1. Dockerfile da API
- [x] 1.1 Criar `api-autorizador-processo/Dockerfile` multi-stage: estágio de build `eclipse-temurin:25-jdk` com `./mvnw -DskipTests package` e estágio de runtime `eclipse-temurin:25-jre`.
- [x] 1.2 Rodar como usuário não-root, `EXPOSE 8080`, `ENTRYPOINT` com `java -jar`, sem ferramentas de build na imagem final.
- [x] 1.3 Criar `api-autorizador-processo/.dockerignore` (`target/`, `.git`, `.env`, arquivos de IDE).

## 2. Compose e variáveis
- [x] 2.1 Reconferir em `application.yml` e `SecurityConfig` as variáveis e o endpoint `/actuator/health/readiness` (entregues pela etapa 12) antes de escrever o healthcheck.
- [x] 2.2 Adicionar `healthcheck` ao serviço `api` com ferramenta presente na imagem e `start_period` adequado; manter o `pg_isready` do `postgres`.
- [x] 2.3 Repassar ao `api` `JWT_SECRET` (obrigatório, `${JWT_SECRET:?defina JWT_SECRET no .env}`), `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA` e `CORS_ALLOWED_ORIGINS`.
- [x] 2.4 Corrigir o cabeçalho do `compose.yaml` (referências a SPEC-10/SPEC-13) e incluir os comandos para subir só o banco e `postgres api`.
- [x] 2.5 Atualizar o `.env.example`: valores marcados como somente de desenvolvimento, `JWT_SECRET` de desenvolvimento com 32 bytes ou mais, sem segredo real.

## 3. Documentação
- [x] 3.1 Documentar no `README.md` raiz `docker compose up -d --build postgres api`, as portas, `down` e `down -v`, e o aviso de que `app` só existe na etapa do frontend.
  - Evidência: `README.md` raiz: seção "Como subir com Compose" reescrita (comandos `up -d postgres`, `up -d --build postgres api`, `down`, `down -v`, tabela de portas, aviso de que `app` só existe na etapa do frontend). 2026-11-08 (BRT).

## 4. Verificação
- [x] 4.1 Rodar `docker compose config` com e sem `JWT_SECRET` e registrar o resultado.
  - Evidência: `docker-compose config` (v5.6.1; `docker compose` não existe nesta máquina) sem `JWT_SECRET` falha com "defina JWT_SECRET no .env"; com `JWT_SECRET` resolve os serviços postgres, api, app. 2026-11-08 (BRT).
- [x] 4.2 Subir `docker compose up -d --build postgres api` e confirmar `healthy` em até 90 s e `curl localhost:8080/actuator/health` = `UP`.
  - Evidência: `up -d --build postgres api` construiu a imagem; a API ficou `healthy` em ~11 s (limite 90 s) e `/actuator/health` = `UP` (db, readiness, liveness UP). As portas 5432/8080/8081 estavam ocupadas por outros projetos do host, então a verificação usou `POSTGRES_PORT=15432`/`API_PORT=18080`. 2026-11-08 (BRT).
- [x] 4.3 Confirmar que o processo da API roda sem root e que `docker compose down` preserva o volume `postgres-data`.
  - Evidência: `id` no contêiner: `uid=999(app)`, processo `java -jar /app/app.jar` como `app` (não-root). `down` removeu contêineres e rede e manteve o volume `postgres-data` (listado após o `down`). 2026-11-08 (BRT).
- [x] 4.4 Rodar `./mvnw verify` em `api-autorizador-processo` com o JDK 25 e confirmar que segue verde.
  - Evidência: `./mvnw verify` com JDK 25.0.2: 229 testes, 0 falhas, `BUILD SUCCESS` (os stack traces no log são de cenários de erro esperados nos testes). 2026-11-08 (BRT).
- [x] 4.5 Garantir que nenhum processo ou contêiner fique ativo, registrar a evidência por tarefa e arquivar a spec com `git mv`.
  - Evidência: Contêineres/volume/imagem da verificação removidos (`down -v`, `rmi`); nenhum contêiner do projeto ativo (o Ryuk do Testcontainers se remove sozinho). SPEC-10 movida com `git mv` para `.specs/archive/`. 2026-11-08 (BRT).

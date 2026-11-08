## 1. Dockerfile da API
- [ ] 1.1 Criar `api-autorizador-processo/Dockerfile` multi-stage: estágio de build `eclipse-temurin:25-jdk` com `./mvnw -DskipTests package` e estágio de runtime `eclipse-temurin:25-jre`.
- [ ] 1.2 Rodar como usuário não-root, `EXPOSE 8080`, `ENTRYPOINT` com `java -jar`, sem ferramentas de build na imagem final.
- [ ] 1.3 Criar `api-autorizador-processo/.dockerignore` (`target/`, `.git`, `.env`, arquivos de IDE).

## 2. Compose e variáveis
- [ ] 2.1 Conferir, após a etapa 12, as variáveis e o endpoint de health realmente expostos pela API e ajustar o plano de acordo.
- [ ] 2.2 Adicionar `healthcheck` ao serviço `api` com ferramenta presente na imagem e `start_period` adequado; manter o `pg_isready` do `postgres`.
- [ ] 2.3 Repassar ao `api` `JWT_SECRET` (obrigatório, `${JWT_SECRET:?defina JWT_SECRET no .env}`), `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA` e `CORS_ALLOWED_ORIGINS`.
- [ ] 2.4 Corrigir o cabeçalho do `compose.yaml` (referências a SPEC-10/SPEC-13) e incluir os comandos para subir só o banco e `postgres api`.
- [ ] 2.5 Atualizar o `.env.example`: valores marcados como somente de desenvolvimento, `JWT_SECRET` de desenvolvimento com 32 bytes ou mais, sem segredo real.

## 3. Documentação
- [ ] 3.1 Documentar no `README.md` raiz `docker compose up -d --build postgres api`, as portas, `down` e `down -v`, e o aviso de que `app` só existe na etapa do frontend.

## 4. Verificação
- [ ] 4.1 Rodar `docker compose config` com e sem `JWT_SECRET` e registrar o resultado.
- [ ] 4.2 Subir `docker compose up -d --build postgres api` e confirmar `healthy` em até 90 s e `curl localhost:8080/actuator/health` = `UP`.
- [ ] 4.3 Confirmar que o processo da API roda sem root e que `docker compose down` preserva o volume `postgres-data`.
- [ ] 4.4 Rodar `./mvnw verify` em `api-autorizador-processo` com o JDK 25 e confirmar que segue verde.
- [ ] 4.5 Garantir que nenhum processo ou contêiner fique ativo, registrar a evidência por tarefa e arquivar a spec com `git mv`.

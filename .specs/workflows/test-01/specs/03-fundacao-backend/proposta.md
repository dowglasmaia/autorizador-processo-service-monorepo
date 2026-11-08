# Fundação do backend

Etapa 3 de 31 · Fase 1 · derivada da `SPEC-03` (`.specs/02-backend/SPEC-03-backend-foundation.md`).

## Objetivo
Gerar o projeto Maven `api-autorizador-processo` (modular monolith, Hexagonal Pragmática, Java 25, Spring Boot 4.1.2, contract-first) e **remover o código original da raiz** depois que o baseline (etapa 2) existir. Resultado: o projeto compila, testa e sobe com `/actuator/health` = `UP`, **sem regras de negócio**.

## Ponto de partida (observado antes da etapa 2 terminar)
- A raiz ainda tem `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` e `src/` (Spring Boot 2.2.6 / H2), além de `compose.yaml` e `README.md` ("em construção").
- `api-autorizador-processo/` ainda não existe; a tag `v0-original` e a movimentação do protótipo vieram da etapa 1.
- A etapa 2 (`tools/parity/` com `cases.json` e `replay.mjs`) estava em andamento; esta etapa **só remove o código da raiz se o baseline estiver entregue** (tarefa 1.2). Revisar esta proposta contra o que a etapa 2 entregou.
- `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).

## Escopo
1. Toolchain: JDK 25.0.2 em `C:\jdk-25.0.2` (`JAVA_HOME`, `PATH`); Maven Wrapper usando esse JDK.
2. Conferir compatibilidade de Spring Modulith, Maven e OpenAPI Generator 7.26.0 com Boot 4.1.2 (sem rebaixar o Boot); divergências vão para `DECISIONS.md` (PRM-01).
3. Gerar com a skill `java-spring-scaffold` (`scaffold.mjs --contract-first true`, sem Spring AI) em diretório novo, via `--output` na raiz.
4. Ajustar identidade: `groupId org.maia`, `artifactId api-autorizador-processo`, pacote `org.maia.autorizador`, `spring.application.name`, porta 8080, JAR, `<parent>` = `spring-boot-starter-parent:4.1.2` com `<relativePath/>`.
5. Remover o módulo de exemplo (`example`) e seus testes, mantendo `shared`, logging estruturado, health e Modulith `verify`.
6. `ArchitectureTest` (ArchUnit) com as 10 regras de `TARGET_ARCHITECTURE.md` §2.1, incluindo a que proíbe "legado/legacy"; confirmar leitura de bytecode Java 25.
7. `git rm -r pom.xml mvnw mvnw.cmd .mvn src` na raiz (conteúdo permanece em `v0-original`); ajustar `README.md` raiz se citar comandos antigos.
8. `.gitignore` e `README.md` do backend (stack, requisitos, como rodar, `DB_URL/DB_USERNAME/DB_PASSWORD/APP_ENV/LOG_LEVEL_APP`).
9. `validate.mjs` da skill e `./mvnw verify`.

## Fora do escopo (fica para outras etapas)
- Banco, Flyway, Testcontainers e migrações: etapa 4 (SPEC-04). Sem H2: o `verify` desta etapa não pode depender de banco; o contexto de teste não deve exigir datasource.
- Contratos OpenAPI de negócio: etapa 5. Tratamento global de erros e CORS: etapa 6. Autenticação/JWT: etapa 7.
- Dockerfile e CI do backend: SPEC-10 / etapa 40; ajuste do `compose.yaml`: SPEC-10.
- Qualquer módulo de negócio (`procedimento`, `autorizacao`, `painel`).

## Critérios de aceitação
- Em checkout limpo, `./mvnw verify` em `api-autorizador-processo` termina em `BUILD SUCCESS`, com `ArchitectureTest` e Modulith sem violações.
- Aplicação iniciada responde `{"status":"UP"}` em `/actuator/health`.
- `git ls-files | grep -E "^(src|pom.xml)"` na raiz → vazio; `git grep -il "legado\|legacy" -- api-autorizador-processo` → vazio.
- `validate.mjs` sem falhas; nenhum segredo versionado; processos encerrados ao final.

## Riscos
- Boot 4.1.2 diferente da matriz da skill (4.1.1): validar o build após trocar para o parent.
- ArchUnit pode não ler bytecode Java 25 em versões antigas: subir para versão compatível.
- Se o health depender de banco, o `UP` falha sem Postgres: desativar o indicador de banco/autoconfiguração de datasource nesta etapa, sem H2.
- Remoção da raiz é irreversível no working tree, mas recuperável pela tag `v0-original`; só executar com o baseline entregue.

## Decisões tomadas sem consulta
- **[REVISAR]** Condicionar a remoção do código da raiz (tarefa 4.1) à presença de `tools/parity/cases.json` e `replay.mjs` da etapa 2; se faltarem, a tarefa fica bloqueada e o restante segue. A remoção é coberta pela SPEC-03 e pela tag `v0-original`, por isso não se pediu `aprovacao.md`.
- Manter a ordem do plano (etapa 3 antes de 4): o projeto sobe sem datasource; a configuração de banco fica na etapa 4.
- Manter as 10 regras ArchUnit mesmo sem módulos de negócio ainda; regras sem classes candidatas usam `allowEmptyShould(true)`.
- Não dividir tarefas: a spec tem 8 tarefas, agrupadas em 5 blocos.

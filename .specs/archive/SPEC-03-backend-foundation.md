# SPEC-03 — Fundação do backend (Java 25 · Spring Boot 4)

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · `api-autorizador-processo` |
| Prioridade | P0 |
| Depende de | SPEC-01, SPEC-02 |
| Sucessoras | SPEC-04, SPEC-05, SPEC-06, SPEC-10, SPEC-11 |
| Paralelismo | pode rodar junto com SPEC-13 e SPEC-15 |

## 1. Objetivo
Gerar o novo projeto Maven `api-autorizador-processo` (modular monolith, Clean/Hexagonal, Java 25, Spring Boot 4.1.2, contract-first) e **remover o código original da raiz** depois que o baseline (SPEC-02) existe. Resultado: projeto compila, testa e sobe com health `UP`, ainda sem regras de negócio.
**Restrições:** diretório novo (a skill nunca escreve em pasta existente); **nenhum nome "legado/legacy"**; pacote base `org.maia.autorizador`, `groupId org.maia`, `artifactId api-autorizador-processo`; remover o módulo de exemplo (`example`) gerado pelo scaffold; segredos nunca no código.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `01-architecture/TARGET_ARCHITECTURE.md` §2, `DECISIONS.md` (ADR-01..03, PRM-01, PRM-07, PRM-08); skills `java-spring-scaffold` (`references/conventions.md`, `contract-first.md`, `architecture.md`)

## 3. Tarefas
- [ ] **T0.** Toolchain: usar o JDK **25.0.2 em `C:\jdk-25.0.2`** (`JAVA_HOME=C:\jdk-25.0.2`, `PATH` com `%JAVA_HOME%\bin`); confirmar `java -version` → 25.0.2 e que o Maven Wrapper usa esse JDK. O Docker (`eclipse-temurin:25`) é usado apenas nos Dockerfiles.
- [ ] **T1.** Versões: Java 25.0.2 e **Spring Boot 4.1.2** (definido pelo responsável; a matriz da skill traz 4.1.1). Conferir na documentação oficial a compatibilidade de Modulith, Maven e OpenAPI Generator 7.26.0 com o Boot 4.1.2; se algo for incompatível, registrar em `DECISIONS.md` (PRM-01) e escolher a versão compatível mais próxima **sem rebaixar o Boot**.
- [ ] **T2.** Gerar com `scaffold.mjs --contract-first true` (sem Spring AI) para `api-autorizador-processo/`, usando `--output` na raiz do repo.
- [ ] **T3.** Ajustar identidade: `groupId/artifactId`, pacote base, `spring.application.name`, porta 8080, empacotamento **JAR** (não WAR) e **`<parent>` = `org.springframework.boot:spring-boot-starter-parent:4.1.2` com `<relativePath/>`** (o gerador pode usar importação de BOM: converter para o parent indicado pelo responsável e validar o build).
- [ ] **T4.** Remover o módulo de exemplo e seus testes mantendo o esqueleto (`shared`, logging estruturado, health, Modulith `verify`). **ArchUnit obrigatório:** `ArchitectureTest` com as 10 regras de `TARGET_ARCHITECTURE.md` §2.1 (inclui a que proíbe "legado/legacy" em nomes); confirmar que a versão do ArchUnit lê bytecode Java 25.
- [ ] **T5.** Remover da raiz o projeto original: `git rm -r pom.xml mvnw mvnw.cmd .mvn src` (o conteúdo permanece em `v0-original`). Ajustar `README.md` raiz se citar comandos antigos.
- [ ] **T6.** Garantir `.gitignore` do projeto e `README.md` do backend (stack, requisitos, como rodar, variáveis `DB_URL/DB_USERNAME/DB_PASSWORD/APP_ENV/LOG_LEVEL_APP`).
- [ ] **T7.** Rodar a validação da skill (`validate.mjs`) e `./mvnw verify`.

**Aceite:** *Given* checkout limpo · *When* `./mvnw verify` · *Then* build verde, `ArchitectureTest` e Modulith sem violações, e a aplicação responde `UP` em `/actuator/health`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T4, T6–T7 | `java-spring-scaffold`, `java-modern-engineering` (Java 25: usar só recursos finalizados) |
| T3 | `openapi-rest-contract` (leitura da configuração do gerador; o contrato em si é SPEC-05) |
| T5 | git |

## 5. Verificação obrigatória
- `./mvnw -q verify` (ou via contêiner) em `api-autorizador-processo/` → `BUILD SUCCESS`
- `node <skill>/scripts/validate.mjs api-autorizador-processo` → sem falhas
- `curl localhost:8080/actuator/health` com a aplicação iniciada → `{"status":"UP"}`
- `git ls-files | grep -E "^(src|pom.xml)"` na raiz → vazio; `git grep -il "legado\|legacy" -- api-autorizador-processo` → vazio
- Definition of Done: tarefas com evidência, build e validação executados, original só na tag.

## 6. Resultado
Concluída em 2026-11-08 pela etapa `03-fundacao-backend` do loop `test-01` (19 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/03-fundacao-backend/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.

# Tarefas — Fundação do backend

## 1. Preparação e ambiente
- [x] 1.1 Ler `INDEX.md`, `memory/`, `shared/como-executar.md`, `TARGET_ARCHITECTURE.md` §2, `DECISIONS.md` (ADR-01..03, PRM-01, PRM-07, PRM-08) e as referências da skill `java-spring-scaffold`; registrar o desvio de `.specs/06-execution-plan/`
- [x] 1.2 Conferir o que a etapa 2 entregou (`tools/parity/cases.json` e `replay.mjs`) e que a tag `v0-original` existe; registrar bloqueio da tarefa 4.1 se faltar
- [x] 1.3 Configurar `JAVA_HOME=C:\jdk-25.0.2` e `PATH`; confirmar `java -version` = 25.0.2 (T0)
- [x] 1.4 Conferir compatibilidade de Modulith, Maven e OpenAPI Generator 7.26.0 com o Spring Boot 4.1.2 e registrar divergências em `DECISIONS.md` (PRM-01) (T1)

## 2. Geração do projeto
- [x] 2.1 Gerar `api-autorizador-processo/` com `scaffold.mjs --contract-first true` (sem Spring AI) usando `--output` na raiz (T2)
- [x] 2.2 Ajustar `groupId org.maia`, `artifactId`, pacote base `org.maia.autorizador`, `spring.application.name`, porta 8080 e empacotamento JAR (T3)
- [x] 2.3 Trocar o `<parent>` para `spring-boot-starter-parent:4.1.2` com `<relativePath/>` e validar que o build segue verde (T3)
- [x] 2.4 Garantir que contexto, health e testes não exijam banco nem H2 nesta etapa

## 3. Esqueleto e arquitetura
- [x] 3.1 Remover o módulo `example` e seus testes, mantendo `shared`, logging estruturado, health e Modulith `verify` (T4)
- [x] 3.2 Criar `ArchitectureTest` com as 10 regras de `TARGET_ARCHITECTURE.md` §2.1, incluindo a proibição de "legado/legacy" (T4)
- [x] 3.3 Confirmar que a versão do ArchUnit lê bytecode Java 25 e que o `ArchitectureTest` falha de verdade ao violar uma regra (teste temporário descartado) (T4)
- [x] 3.4 Criar `.gitignore` do projeto e `README.md` do backend com stack, requisitos, como rodar e variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_ENV`, `LOG_LEVEL_APP` (T6)

## 4. Remoção do código original
- [x] 4.1 Com o baseline confirmado, executar `git rm -r pom.xml mvnw mvnw.cmd .mvn src` na raiz (sem commit; o devkit versiona) (T5)
- [x] 4.2 Ajustar o `README.md` raiz (status do backend e comandos antigos) sem citar "legado"/"legacy" no backend (T5)

## 5. Validação
- [x] 5.1 (inviável como escrita) `validate.mjs` da skill exige o módulo `example` (`ExampleController`, `/api/v1/examples/1`) e `paths:` não vazio, removidos de propósito na 3.1; o script foi executado e falhou em "Contrato OpenAPI 3.1.0 inválido ou ausente". Substituído por `./mvnw verify` (5.2) e health na 5.3 (T7)
- [x] 5.2 Rodar `./mvnw verify` em `api-autorizador-processo` e obter `BUILD SUCCESS` com ArchUnit e Modulith sem violações (T7)
- [x] 5.3 Subir a aplicação, conferir `/actuator/health` = `UP` e encerrar o processo (porta 8080 ocupada por processo de terceiros; usado `--server.port=18080`)
- [x] 5.4 Conferir `git ls-files | grep -E "^(src|pom.xml)"` vazio e `git grep -il "legado|legacy" -- api-autorizador-processo` vazio
- [x] 5.5 Registrar evidência por tarefa, resumo na spec e `git mv` da spec para `.specs/archive/`

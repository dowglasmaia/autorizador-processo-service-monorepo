# Contexto técnico (leitura obrigatória em toda spec)

Resumo curto. Fontes: `.specs/01-architecture/` e `.specs/05-quality/`.

| Item | Decisão |
|---|---|
| Monorepo | `api-autorizador-processo/` (Maven) · `app-autorizador-processo/` (Angular) · `compose.yaml` e `.env.example` na raiz |
| Java | **25.0.2 (LTS)** em `C:\jdk-25.0.2` (confirmado: `java -version` → 25.0.2). Definir `JAVA_HOME=C:\jdk-25.0.2` e incluir `%JAVA_HOME%\bin` no `PATH` |
| Spring Boot | **4.1.2**, via `spring-boot-starter-parent` 4.1.2 como `<parent>` do `pom.xml` (definido pelo responsável; a matriz da skill traz 4.1.1, então SPEC-03 ajustou o `pom` gerado e validou a compatibilidade de Modulith e OpenAPI Generator) |
| Build | Maven Wrapper 3.9.x · OpenAPI Generator 7.26.0 (contract-first, `interfaceOnly`) |
| Banco | **PostgreSQL 16** · Flyway · Testcontainers (sem H2) |
| Arquitetura backend | Hexagonal Pragmática, pacote por funcionalidade (`java-spring-create-module`): `procedimento`, `autorizacao`, `painel`, `shared` |
| Frontend | Angular **21** · TypeScript estrito · Tailwind CSS · standalone, zoneless, Signals, Reactive Forms tipados |
| Servidor web | Nginx serve o Angular e faz proxy de `/api/` para a API |
| Autenticação | Login por e-mail e senha; **JWT** (Spring Security resource server, HS256); todas as rotas exceto `POST /api/v1/auth/login` e health exigem token; sem papéis na v1 (SPEC-22, SPEC-23) |
| Arquitetura (testes) | **ArchUnit obrigatório** (`ArchitectureTest`) valida a arquitetura; regras em `TARGET_ARCHITECTURE.md` §2.1 |
| Contrato | OpenAPI 3.1 em `api-autorizador-processo/src/main/resources/openapi/` (fonte de verdade); erros RFC 9457 |
| Ambiente do desenvolvedor | JDK 25.0.2 em `C:\jdk-25.0.2`; Node 24 e Docker 29 instalados. Imagens Docker usam `eclipse-temurin:25` |
| Skills | `java-persistence-performance` e `angular-tailwind-ui-specialist` valem **para este projeto por inteiro**, não só para o produto onde nasceram. Onde citam o outro produto, usar o equivalente daqui: `.spec/memory/*` → `.specs/memory/*`; tokens "Grafite Ciano" → tokens do Autorizador (SPEC-15, derivados do protótipo); regra de `owner_id` → sem multi-tenant, mas **toda rota exige usuário autenticado** |
| Pasta de specs | As skills citam `.spec/`; neste repositório é `.specs/` |
| Execução | `shared/como-executar.md`. `06-execution-plan/` nunca existiu; estado final em `.specs/05-quality/RELATORIO_FINAL.md` |
| Estado | Modernização **concluída e verificada** (backend, frontend, Compose, CI, e2e, paridade, contrato). Specs arquivadas em `.specs/archive/` |

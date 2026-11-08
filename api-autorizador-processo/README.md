# api-autorizador-processo

Backend do **Autorizador de Procedimentos Médicos**: monólito modular em Java 25 e Spring Boot 4.1.2, com Hexagonal Pragmática (pacote por funcionalidade), contrato OpenAPI 3.1 primeiro e logs estruturados.

> Estado atual (etapa 4, fundação do banco): PostgreSQL 16, Flyway e auditoria (Envers) configurados, sem funcionalidades de negócio. Contrato, funcionalidades e autenticação JWT chegam nas próximas etapas.

## Stack

| Área | Tecnologia |
|---|---|
| Linguagem | Java 25 |
| Framework | Spring Boot 4.1.2, Spring Web MVC |
| Modularidade | Spring Modulith 2.1.1 (`ApplicationModules.verify()`) |
| Arquitetura | ArchUnit 1.5.1 (`ArchitectureTest`, regras de `TARGET_ARCHITECTURE.md` §2.1) |
| Build | Maven 3.9.16 via Maven Wrapper |
| Contrato | OpenAPI 3.1 + OpenAPI Generator 7.26.0 (`src/main/resources/openapi/`) |
| Logs | Logback com JSON estruturado nativo do Spring Boot |
| Observabilidade | Actuator, Micrometer, OpenTelemetry (OTLP, desligado por padrão) |
| Banco | PostgreSQL 16, Spring Data JPA, Flyway, Hibernate Envers, Testcontainers (sem H2) |
| Testes | JUnit 5, Spring Boot Test, Mockito, ArchUnit, Modulith |

## Requisitos

- JDK 25 (`JAVA_HOME` configurado).
- Acesso à internet no primeiro build (o Maven Wrapper baixa o Maven e as dependências).
- No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

## Como rodar

```sh
./mvnw clean verify        # compila, gera o contrato, roda testes, ArchUnit e Modulith
./mvnw spring-boot:run     # sobe a API em http://localhost:8080
```

O perfil padrão é `local` (logs em texto legível). Ele aponta para o PostgreSQL em `localhost:5432` (suba com `docker compose up -d postgres` na raiz, exportando `DB_PASSWORD` conforme o `.env.example`) e acrescenta `classpath:db/seed` ao Flyway, criando 3 procedimentos de exemplo. Fora do perfil `local` a seed nunca roda. Os testes de integração usam PostgreSQL 16 via Testcontainers e exigem Docker. Verifique em `GET /actuator/health` (`UP`).

## Variáveis de ambiente

Copie `.env.example` e exporte o que precisar. Todas têm valor padrão, exceto `DB_PASSWORD` e `JWT_SECRET` (obrigatórias) e as variáveis `AUTH_BOOTSTRAP_*` (opcionais, sem padrão).

| Variável | Padrão | Uso |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/autorizador` | URL JDBC do PostgreSQL  |
| `DB_USERNAME` | `autorizador` | Usuário do banco  |
| `DB_PASSWORD` | sem padrão versionado | Senha do banco; nunca versionar |
| `APP_ENV` | `production` | Nome do ambiente nos logs e na telemetria |
| `LOG_LEVEL_APP` | `INFO` (`DEBUG` no perfil `local`) | Nível de log do pacote `org.maia.autorizador` |
| `LOG_LEVEL_ROOT` | `INFO` | Nível de log raiz |
| `LOG_FORMAT` | `ecs` | Formato do JSON: `ecs`, `logstash` ou `gelf` (o perfil `local` imprime texto) |
| `PORT` | `8080` | Porta HTTP |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` | Fração de traces amostrados |
| `OTEL_EXPORT_ENABLED` | `false` | Exporta traces e métricas por OTLP |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | Endereço do Collector |
| `JWT_SECRET` | sem padrão (obrigatória) | Segredo HS256 do JWT; mínimo de 32 bytes. Ausente ou curto, a aplicação não inicia. Nunca versionar |
| `JWT_TTL_MINUTOS` | `60` | Validade do token de acesso em minutos |
| `AUTH_BOOTSTRAP_EMAIL` | sem padrão | E-mail do primeiro usuário; criado só se a tabela `usuario` estiver vazia e as duas variáveis `AUTH_BOOTSTRAP_*` estiverem definidas |
| `AUTH_BOOTSTRAP_SENHA` | sem padrão | Senha do primeiro usuário (guardada apenas como hash BCrypt); nunca registrada em log nem versionada |

## Arquitetura

Cada funcionalidade é um módulo Modulith com `api` (adaptador de entrada), `application` (`usecase` e `port`), `domain` (sem framework) e `infrastructure` (adaptadores de saída).

```text
org.maia.autorizador
├── Application.java
├── generated/        interfaces e DTOs gerados do OpenAPI (módulo aberto)
└── shared/           código técnico transversal (logging; módulo aberto)
```

Funcionalidades previstas: `procedimento`, `autorizacao`, `painel`. As fronteiras são verificadas por `ModularityTests` e as regras de camadas e nomenclatura por `ArchitectureTest`; qualquer violação falha o `verify`. Nenhum pacote ou classe pode ter "legado" ou "legacy" no nome.

## Contrato primeiro

Edite `src/main/resources/openapi/openapi.yaml` e rode `./mvnw clean verify`. O Maven gera interfaces e modelos em `target/generated-sources/openapi`. Nunca edite os arquivos gerados.

## Logs e observabilidade

Os logs saem em JSON na saída padrão (texto no perfil `local`). `RequestLoggingFilter` aceita ou cria `X-Request-Id`, devolve o cabeçalho na resposta e registra uma linha `request completed` por requisição (método, caminho, status, duração), sem query string, cabeçalhos nem corpo. Nunca registre senhas, tokens ou dados pessoais. A exportação OTLP fica desligada até `OTEL_EXPORT_ENABLED=true`.

## Autenticação

`POST /api/v1/auth/login` (e-mail e senha) devolve um JWT HS256 (`iss=autorizador-processo`, `sub`=id do usuário). Todas as demais rotas exigem `Authorization: Bearer <token>`; sem token válido a resposta é `401 problem+json` (`NAO_AUTENTICADO` ou `SESSAO_EXPIRADA`). A 6ª falha de login em 15 minutos para o mesmo e-mail e IP responde `429` com `Retry-After`. Não há cadastro de usuários: o primeiro é criado pelo bootstrap (`AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA`).

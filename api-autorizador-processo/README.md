# api-autorizador-processo

Backend do **Autorizador de Procedimentos Médicos**: monólito modular em Java 25 e Spring Boot 4.1.2, com Hexagonal Pragmática (pacote por funcionalidade), contrato OpenAPI 3.1 primeiro e logs estruturados.

> Estado: entregue e verificado (`./mvnw verify`: 247 testes, `ArchitectureTest` 23/23). Contrato OpenAPI v1 com 8 operações, autenticação JWT, painel, procedimentos e autorizações.

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
./mvnw clean verify        # compila, gera o contrato, roda testes, ArchUnit e Modulith (exige Docker)
./mvnw spring-boot:run     # sobe a API em http://localhost:8080
```

O perfil padrão é `local` (logs em texto legível). Ele aponta para o PostgreSQL em `localhost:5432` (suba com `docker compose up -d postgres` na raiz, exportando `DB_PASSWORD` conforme o `.env.example`) e acrescenta `classpath:db/seed` ao Flyway, criando 3 procedimentos de exemplo. Fora do perfil `local` a seed nunca roda. Os testes de integração usam PostgreSQL 16 via Testcontainers e exigem Docker. Verifique em `GET /actuator/health` (`UP`).

## Variáveis de ambiente

Copie `.env.example` e exporte o que precisar. Valores padrão de conexão existem só no perfil `local`; `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` não têm padrão nos demais perfis, e as variáveis `AUTH_BOOTSTRAP_*` são opcionais e sem padrão.

| Variável | Padrão | Uso |
|---|---|---|
| `DB_URL` | sem padrão (`jdbc:postgresql://localhost:5432/autorizador` no perfil `local`) | URL JDBC do PostgreSQL |
| `DB_USERNAME` | sem padrão (`autorizador` no perfil `local`) | Usuário do banco |
| `DB_PASSWORD` | sem padrão versionado | Senha do banco; nunca versionar |
| `CORS_ALLOWED_ORIGINS` | vazio (`http://localhost:4200` no perfil `local`) | Origens CORS separadas por vírgula; o curinga `*` é ignorado |
| `APP_ENV` | `production` | Nome do ambiente nos logs e na telemetria |
| `LOG_LEVEL_APP` | `INFO` (`DEBUG` no perfil `local`) | Nível de log do pacote `org.maia.autorizador` |
| `LOG_LEVEL_ROOT` | `INFO` | Nível de log raiz |
| `LOG_FORMAT` | `ecs` | Formato do JSON: `ecs`, `logstash` ou `gelf` (o perfil `local` imprime texto) |
| `SERVER_PORT` | `8080` | Porta HTTP do processo |
| `TRACING_SAMPLING_PROBABILITY` | `0.1` | Fração de traces amostrados |
| `OTEL_EXPORT_ENABLED` | `false` | Exporta traces e métricas por OTLP |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | Endereço do Collector |
| `JWT_SECRET` | sem padrão (obrigatória) | Segredo HS256 do JWT; mínimo de 32 bytes. Ausente ou curto, a aplicação não inicia. Nunca versionar |
| `JWT_TTL_MINUTOS` | `60` | Validade do token de acesso em minutos |
| `AUTH_BOOTSTRAP_EMAIL` | sem padrão | E-mail do primeiro usuário; criado só se a tabela `usuario` estiver vazia e as duas variáveis `AUTH_BOOTSTRAP_*` estiverem definidas |
| `AUTH_BOOTSTRAP_SENHA` | sem padrão | Senha do primeiro usuário (guardada apenas como hash BCrypt); nunca registrada em log nem versionada |

## Configuração e operação

**Perfis.** O perfil padrão é `local` (`spring.profiles.default`): logs em texto, detalhes de health, documentação da API e seed de exemplo. Fora dele (contêiner/produção, por exemplo `SPRING_PROFILES_ACTIVE=prod`) valem as regras restritivas: sem padrões de conexão, sem seed, sem documentação e health sem detalhes. Variáveis e padrões estão na tabela acima; segredos (`DB_PASSWORD`, `JWT_SECRET`, `AUTH_BOOTSTRAP_*`) nunca têm padrão versionado.

**Health.** Só `health` e `info` são expostos pelo Actuator; `env`, `metrics`, `beans` e demais respondem `404`/`401`. Rotas públicas: `/actuator/health/**`.

| Rota | Uso | Resposta |
|---|---|---|
| `/actuator/health/liveness` | processo vivo | `200 UP` |
| `/actuator/health/readiness` | pronto para tráfego (inclui o banco) | `200 UP`; `503 DOWN` com o PostgreSQL parado |
| `/actuator/health` | resumo | `UP`; componentes só no perfil `local` |

**Logs.** JSON ECS na saída padrão (`LOG_FORMAT` troca para `logstash` ou `gelf`); texto legível em `local`. Cada linha de requisição traz `requestId` (cabeçalho `X-Request-Id`), `traceId` e `spanId`. E-mails aparecem mascarados e nenhum log contém senha, token ou JWT. Métricas Micrometer ficam internas (sem endpoint exposto); a exportação OTLP só liga com `OTEL_EXPORT_ENABLED=true`.

**Documentação da API.** Somente no perfil `local`: `http://localhost:8080/swagger-ui.html` serve o contrato estático de `openapi/` (a página carrega a Swagger UI de um CDN, exigindo internet). Nos demais perfis `/swagger-ui*` e `/v3/api-docs*` respondem `404`.

## Arquitetura

Cada funcionalidade é um módulo Modulith com `api` (adaptador de entrada), `application` (`usecase` e `port`), `domain` (sem framework) e `infrastructure` (adaptadores de saída).

```text
org.maia.autorizador
├── Application.java
├── generated/        interfaces e DTOs gerados do OpenAPI (módulo aberto)
└── shared/           código técnico transversal (logging; módulo aberto)
```

Funcionalidades: `autenticacao`, `procedimento`, `autorizacao`, `painel`. As fronteiras são verificadas por `ModularityTests` e as regras de camadas e nomenclatura por `ArchitectureTest`; qualquer violação falha o `verify`. Nenhum pacote ou classe pode ter "legado" ou "legacy" no nome.

## Endpoints (`/api/v1`)

Contrato em `src/main/resources/openapi/modules/`. Todos exigem `Authorization: Bearer <token>`, exceto o login. Erros seguem RFC 9457 (`application/problem+json`).

| Método e caminho | Operação | Uso |
|---|---|---|
| `POST /auth/login` | `autenticar` | e-mail e senha → JWT (público) |
| `GET /auth/me` | `obterUsuarioAtual` | usuário autenticado |
| `GET /procedimentos` | `listarProcedimentos` | listagem paginada, com filtro e `sort` |
| `POST /procedimentos` | `cadastrarProcedimento` | cadastra procedimento |
| `GET /procedimentos/{id}` | `obterProcedimento` | detalhe |
| `POST /autorizacoes` | `solicitarAutorizacao` | solicita autorização (critérios de idade e sexo) |
| `GET /autorizacoes` | `listarAutorizacoes` | lista autorizações |
| `GET /painel/resumo` | `obterResumoPainel` | indicadores do painel |

## Testes

`./mvnw verify` roda testes unitários, de integração (PostgreSQL 16 via Testcontainers, sem H2), de contrato (`ContratoRespostasApiTest` valida respostas reais contra o YAML), de segurança (`VarreduraSegurancaTest`), de log, de concorrência e o `ArchitectureTest`. Sem Docker os testes de integração não rodam. O smoke de desempenho fica em `tools/perf/`.

## ArchUnit

`ArchitectureTest` (23 regras) é obrigatório e falha o `verify`: camadas da Hexagonal Pragmática, domínio sem framework, um caso de uso por operação, portas só de saída, dependências entre módulos e nomenclatura. A regra 9 proíbe "legado" ou "legacy" em pacote ou classe; por isso o termo só aparece nela e nesta documentação.

## Riscos aceitos

- Sem refresh token (PEN-10): o JWT dura 60 minutos e não há revogação antes do vencimento.
- Limitador de login em memória (PEN-11): vale por instância; zera no reinício.
- Sem papéis/perfis (PEN-12), cadastro de usuários nem recuperação de senha: fora do escopo.
- HTTP puro no Compose: em ambiente real, exige terminador TLS na frente (veja o README da raiz, seção Segurança).

## Contrato primeiro

Edite `src/main/resources/openapi/openapi.yaml` e rode `./mvnw clean verify`. O Maven gera interfaces e modelos em `target/generated-sources/openapi`. Nunca edite os arquivos gerados.

## Logs e observabilidade

Os logs saem em JSON na saída padrão (texto no perfil `local`). `RequestLoggingFilter` aceita ou cria `X-Request-Id`, devolve o cabeçalho na resposta e registra uma linha `request completed` por requisição (método, caminho, status, duração), sem query string, cabeçalhos nem corpo. Nunca registre senhas, tokens ou dados pessoais. A exportação OTLP fica desligada até `OTEL_EXPORT_ENABLED=true`.

## Autenticação

`POST /api/v1/auth/login` (e-mail e senha) devolve um JWT HS256 (`iss=autorizador-processo`, `sub`=id do usuário). Todas as demais rotas exigem `Authorization: Bearer <token>`; sem token válido a resposta é `401 problem+json` (`NAO_AUTENTICADO` ou `SESSAO_EXPIRADA`). A 6ª falha de login em 15 minutos para o mesmo e-mail e IP responde `429` com `Retry-After`. Não há cadastro de usuários: o primeiro é criado pelo bootstrap (`AUTH_BOOTSTRAP_EMAIL` e `AUTH_BOOTSTRAP_SENHA`).

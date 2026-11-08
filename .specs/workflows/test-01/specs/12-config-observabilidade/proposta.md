# Config e observabilidade

Etapa 12 de 31 · Fase 2 · derivada da `SPEC-11` (`.specs/02-backend/SPEC-11-backend-config-observabilidade.md`). Proposta escrita antecipadamente e **revisada contra o código após as etapas 10 e 11** (painel entregue, sem impacto em configuração/observabilidade).

## Objetivo
Fechar a operação do backend: perfis (`local` e padrão/contêiner), variáveis de ambiente, health `liveness`/`readiness`, logs estruturados com correlação e métricas básicas, sem stack externa de monitoramento. Corrige D6 (sem health) e D9 (segredos versionados). Referências: NFR-01, NFR-07, NFR-08, SEC-07.

## Ponto de partida (conferido no código após a etapa 11)
Boa parte já veio com o scaffold e com as etapas 4–10; esta etapa **audita, completa e prova com testes**, não recria.
- `application.yml`: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_*`, `APP_ENV`, `LOG_LEVEL_APP`, `LOG_LEVEL_ROOT` e `LOG_FORMAT` (padrão `ecs`) já existem. Falta `SERVER_PORT` (hoje `application-local.yml` usa `PORT`).
- `management.endpoints.web.exposure.include` é `health,info,metrics`; a SPEC-11 manda expor só `health,info`. Não há grupos `liveness`/`readiness` explícitos nem `show-details`.
- Logs: ECS por padrão; `application-local.yml` troca para texto legível com `requestId`/`traceId`. `RequestLoggingFilter` (`shared/logging`) gera/propaga `X-Request-Id` no MDC e loga o acesso (ignora `/actuator`); `MascaraEmail` existe. `TracingCorrelationTests` e `RequestLoggingFilterTest` cobrem parte. `GlobalExceptionHandler` registra o `traceId` só no log.
- Tracing/OTLP: `spring-boot-starter-opentelemetry` no `pom.xml`, exportação desligada por padrão (`OTEL_EXPORT_ENABLED=false`); sem agente nem coletor.
- Documentação da API: `SecurityConfig` já libera `/swagger-ui*` e `/v3/api-docs/**` só no perfil `local`, mas **não há dependência de UI** (springdoc) no `pom.xml` nem página servindo o contrato estático de `openapi/`.
- `SecurityConfig` libera `/actuator/health/**`; o restante do actuator cai em `authenticated`. O `.env.example` da raiz traz as variáveis; o `README.md` do backend tem as seções "Variáveis de ambiente" e "Logs e observabilidade" a revisar.
- Eventos de negócio já são logados com e-mail mascarado (`AutenticarUsuarioUseCase`, `UsuarioInicialRunner`) e ids de procedimento (`SolicitarAutorizacaoUseCase`); o `.env.example` não traz `SERVER_PORT` nem `LOG_LEVEL_ROOT`/`LOG_FORMAT`.
- Não há teste de readiness com banco parado nem de formato JSON do log.
- `.specs/06-execution-plan/` e `check-architecture.mjs` não existem (desvio; vale o `ArchitectureTest`).

## Escopo
1. Variáveis e perfis (T1): alinhar `application*.yml` à lista da SPEC-11 e ao `.env.example`; valores padrão só em `local`; segredos sem padrão.
2. Actuator (T2): expor só `health,info`; grupos `liveness` e `readiness` (readiness com `db`); `show-details=never` fora de `local`.
3. Logs (T3): confirmar JSON ECS no perfil padrão e texto no `local`; correlação `requestId`/`traceId`/`spanId`; revisar mensagens de log (sem senha, token, e-mail completo).
4. Métricas básicas: métricas nativas do Micrometer (JVM, HTTP, pool) apenas internas, sem endpoint exposto nem coletor (ver decisão abaixo).
5. Documentação da API (T4): UI do contrato estático só no perfil `local`; `404` no perfil padrão.
6. Testes (T5) e seção "Configuração e operação" no `README.md` (T6).

## Fora do escopo (fica para outras etapas)
- Dockerfile, Compose e healthcheck do contêiner (SPEC-10); CI (SPEC-13).
- Prometheus, Grafana, coletor OpenTelemetry, agente e qualquer stack externa (limite do plano).
- Medição de desempenho (SPEC-42); hardening e varredura final de segredos (SPEC-41).
- Novos endpoints de negócio e mudança de contrato OpenAPI.

## Critérios de aceitação
- PostgreSQL parado → `GET /actuator/health/readiness` = `503 DOWN`; ativo → `200 UP`; `liveness` = `200 UP`.
- Perfil padrão: `/swagger-ui*` e `/v3/api-docs*` → `404`; perfil `local`: UI acessível servindo `openapi/`.
- `/actuator/env`, `/actuator/metrics` e `/actuator/beans` não expostos; `health` sem detalhes fora de `local`.
- Log do perfil padrão em JSON ECS com `traceId` e `requestId`; nenhum log contém senha, token ou JWT.
- Todas as variáveis do `.env.example` consumidas; `git grep -i "password" -- api-autorizador-processo` só mostra nomes de variável.
- `./mvnw verify` verde (incluindo `ArchitectureTest`, `ModularityTests` e regressão das etapas anteriores); sem "legado/legacy". Testcontainers exige Docker; sem Docker, registrar bloqueio.

## Riscos
- Parar o banco em Testcontainers pode afetar outros testes: usar contêiner isolado e restaurar.
- A biblioteca de UI (springdoc) pode não ter versão compatível com Spring Boot 4.1; alternativa: página estática mínima só em `local`.
- Remover `metrics` da exposição pode quebrar teste existente.
- Mudar o formato do log ou a ordem dos filtros pode quebrar `RequestLoggingFilterTest`/`TracingCorrelationTests`.

## Decisões tomadas sem consulta
- **[REVISAR]** Conflito entre "métricas básicas" (plano) e "actuator expõe só `health` e `info`" (SPEC-11): seguir a SPEC e o limite "sem stack externa". Métricas Micrometer ficam internas e o endpoint `metrics` **não** é exposto. Se quiser `/actuator/metrics` autenticado, decidir depois.
- O que já existe é tratado como ponto de partida; a etapa entrega só a diferença.
- `SERVER_PORT` passa a ser a variável da porta do processo; `API_PORT`/`APP_PORT` continuam sendo do Compose e não são lidas pela aplicação.
- UI da API só em `local`, servindo o contrato estático; se a biblioteca for incompatível, usar página estática mínima e registrar na evidência.
- O `traceId` permanece no log e em `X-Request-Id`; não entra no corpo RFC 9457 (contrato não muda nesta etapa).
- A revisão pós-etapa 11 confirmou que o ponto de partida segue válido; a tarefa 3.3 virou verificação (eventos de negócio já existem) e só cria o que faltar.

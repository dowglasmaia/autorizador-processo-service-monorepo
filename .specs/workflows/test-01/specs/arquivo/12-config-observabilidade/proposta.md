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

### Lote 1 (tarefas 1.1–3.1)
- Auditoria 1.1: faltavam `SERVER_PORT` (usava-se `PORT`), `LOG_LEVEL_ROOT` e `LOG_FORMAT` no `.env.example` e `CORS_ALLOWED_ORIGINS` na tabela do README; `DB_URL`/`DB_USERNAME` tinham padrão no perfil base. `API_PORT`/`APP_PORT`/`POSTGRES_*` são do Compose, não da aplicação.
- Padrões de conexão (`DB_URL`, `DB_USERNAME`) movidos para `application-local.yml`; `DB_PASSWORD`, `JWT_SECRET` e `AUTH_BOOTSTRAP_*` seguem sem padrão. `SERVER_PORT` tem padrão `8080` no perfil base (porta fixa do contêiner, sem segredo).
- Health: `show-details`/`show-components` = `never` no base e `always` em `local`; grupos `liveness` (`livenessState`) e `readiness` (`readinessState,db`) com probes habilitadas.
- 2.4: `SecurityConfig` já libera apenas `/actuator/health` e `/actuator/health/**`; nada a alterar.
- 3.1: ECS é o formato do perfil base e `local` usa padrão de texto; adicionado `spanId` ao padrão local (já havia `requestId` e `traceId`). Cobertura por teste fica nas tarefas 5.x.
- `./mvnw verify` verde (223 testes).

### Lote 2 (tarefas 3.2–5.3)
- 3.2/3.3: revisadas as 7 chamadas `log.*` e o `RequestLoggingFilter` (só método, caminho sem query, status, duração): e-mail sempre via `MascaraEmail`, sem senha/token/JWT; eventos de login e de autorização concedida/negada já têm dados não sensíveis. Nada a corrigir nem criar.
- 3.4: métricas só internas confirmadas (exposição `health,info`; OTLP de métricas/logs/tracing desligado por padrão).
- 4.1: springdoc não foi adicionado (evita dependência de compatibilidade com Boot 4.1 e duplicar o contrato). `DocumentacaoApiController` (`@Profile("local")`, pacote `shared/docs`) serve `/swagger-ui.html` (página mínima com Swagger UI via CDN `unpkg`, exige internet só em `local`) e o contrato estático em `/v3/api-docs/{modules|common}/{arquivo}.yaml`, espelhando a árvore para os `$ref` relativos funcionarem.
- 4.2: `SecurityConfig` passou a liberar as rotas de documentação em todos os perfis; fora de `local` não há handler, então a resposta é `404` (antes seria `401`).
- 5.x: testes `SaudeReadinessTest` (contêiner Postgres isolado, pool com timeout de 2 s), `ActuatorExposicaoTest` (env/metrics/beans = 401 ou 404, nunca 200; `health` sem `components`/`details`), `DocumentacaoPerfilPadraoTest` e `DocumentacaoPerfilLocalTest`. Os testes usam o perfil `test` (não `local`) como "perfil padrão", porque `spring.profiles.default=local`.
- `./mvnw verify` verde (228 testes).

### Lote 3 (tarefas 5.4–6.4)
- 5.4: `LogJsonEcsTest` faz login com `X-Request-Id` e confere a linha `request completed` em JSON ECS (`@timestamp`, `log.level`, `ecs.version`, `traceId` de 32 hex e `requestId`) e a ausência de senha, e-mail completo e JWT em toda a saída. O `LoggingSystem` é singleton da JVM e já iniciado por outro contexto (formato do perfil `test` é texto); por isso o teste faz `cleanUp()` + `initialize()` com formato `ecs` e restaura o formato anterior no `@AfterEach`.
- 6.1: seção "Configuração e operação" no `README.md` (perfis, health, logs, documentação da API; variáveis na tabela existente).
- 6.2: `./mvnw verify` com JDK 25.0.2 verde (229 testes, incluindo `ArchitectureTest` e `ModularityTests`).
- 6.3: Postgres do Compose (`docker-compose up -d --wait postgres`, porta 55432; o plugin `docker compose` não existe aqui, usou-se o binário `docker-compose`) e API em `local` na porta 18080: `/actuator/health` `UP`, `readiness` `UP` com `db`, `/actuator/env` → **401** (não exposto; sem credencial o Spring Security responde 401 antes do 404, aceito pelo `ActuatorExposicaoTest`), `/swagger-ui.html` → 200. O volume antigo do Compose tinha checksum de migração divergente (`001`); para não apagar dados, usou-se um banco temporário `verif_etapa12`, removido ao fim. Processos da API e contêiner encerrados.
- 6.4: `git grep -i password -- api-autorizador-processo` mostra só nomes de variável (`DB_PASSWORD`, `MVNW_PASSWORD` do wrapper), `PasswordEncoder` e a propriedade `password: ${DB_PASSWORD:}`; nenhum valor de segredo. "legado/legacy" só aparece na regra do README e no `ArchitectureTest` que a impõe.
- Arquivamento da spec: fica a cargo do passo de arquivamento do devkit (a pasta da spec está em `.specs/workflows`, não em `.specs/`).

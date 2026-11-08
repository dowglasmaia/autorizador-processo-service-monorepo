# SPEC-11 — Perfis de configuração, health e observabilidade

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · operação |
| Prioridade | P2 |
| Depende de | SPEC-03, SPEC-06 |
| Sucessoras | SPEC-41 |
| Paralelismo | junto com SPEC-07 |

## 1. Objetivo
Fechar a operação do backend: perfis (`local`, padrão/contêiner), variáveis de ambiente, health liveness/readiness, logs estruturados com correlação, rastreamento nativo e documentação interativa da API somente em desenvolvimento. Corrige D6 (sem health) e D9 (segredos versionados).
**Restrições:** actuator expõe só `health` e `info`; sem agente OpenTelemetry nem coletor (a skill proíbe); swagger UI só nos perfis `local`/desenvolvimento, servindo o **contrato estático**; nada de segredo no repositório.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `NON_FUNCTIONAL_REQUIREMENTS.md` (NFR-01, 07, 08), `SECURITY_REQUIREMENTS.md` (SEC-07); skill `java-spring-scaffold` (`architecture.md` §Logging/Observability)

## 3. Tarefas
- [ ] **T1.** `application.yml`/`application-local.yml`: variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `APP_ENV`, `LOG_LEVEL_APP`, `CORS_ALLOWED_ORIGINS`, `SERVER_PORT`, `JWT_SECRET`, `JWT_TTL_MINUTOS`, `AUTH_BOOTSTRAP_EMAIL`, `AUTH_BOOTSTRAP_SENHA`; valores padrão só para `local`.
- [ ] **T2.** `management.endpoints.web.exposure.include=health,info`; grupos `liveness` e `readiness` (readiness inclui o banco); `show-details=never` fora de `local`.
- [ ] **T3.** Logs: JSON ECS por padrão, texto legível em `local`; `traceId/spanId` e correlação por requisição; verificar que nenhuma mensagem contém parâmetros sensíveis.
- [ ] **T4.** Documentação da API: UI servindo `openapi/` apenas em `local`; desabilitada no perfil padrão.
- [ ] **T5.** Testes: `health` `UP` e `readiness` `DOWN` quando o banco cai (Testcontainers parado), UI inexistente no perfil padrão, formato de log JSON.
- [ ] **T6.** Seção "Configuração e operação" no `README.md` do backend.

**Aceite:** *Given* PostgreSQL parado · *When* `GET /actuator/health/readiness` · *Then* `503 DOWN`; com o banco ativo → `200 UP`. Perfil padrão: `/swagger-ui*` → `404`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T6 | `java-spring-scaffold` (convenções de logging/observabilidade), `java-modern-engineering` |

## 5. Verificação obrigatória
- `./mvnw verify` → verde
- Subir com `docker compose up -d postgres` e perfil `local`; `curl /actuator/health` → `UP`; `curl /actuator/env` → `404`
- Definition of Done: todas as variáveis do `.env.example` consumidas; nenhum segredo no repositório (`git grep -i "password" -- api-autorizador-processo` só mostra nomes de variável).

## 6. Resultado
Concluída em 2026-11-08 pela etapa `12-config-observabilidade` do loop `test-01` (21 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/12-config-observabilidade/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.

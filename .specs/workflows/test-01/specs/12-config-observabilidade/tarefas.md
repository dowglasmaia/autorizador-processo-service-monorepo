# Tarefas — Config e observabilidade

## 1. Variáveis e perfis
- [ ] 1.1 Auditar `application.yml`/`application-local.yml` contra a lista da SPEC-11 e o `.env.example`; listar variáveis faltantes ou não consumidas.
- [ ] 1.2 Adotar `SERVER_PORT` (em vez de `PORT`); valores padrão somente no perfil `local`; `JWT_SECRET` e `AUTH_BOOTSTRAP_*` sem padrão.
- [ ] 1.3 Atualizar `.env.example` e a tabela do README conforme as variáveis consumidas.

## 2. Actuator e health
- [ ] 2.1 Reduzir `management.endpoints.web.exposure.include` para `health,info`.
- [ ] 2.2 Configurar os grupos `liveness` e `readiness` (readiness com `db`) e habilitar as probes.
- [ ] 2.3 Definir `show-details=never` fora de `local` e detalhes apenas em `local`.
- [ ] 2.4 Conferir em `SecurityConfig` que só `/actuator/health/**` é público.

## 3. Logs e correlação
- [ ] 3.1 Verificar JSON ECS no perfil padrão e texto legível em `local`, com `requestId`, `traceId` e `spanId`.
- [ ] 3.2 Revisar as chamadas de log (`git grep "log\."`) para garantir ausência de senha, token e e-mail sem máscara; corrigir o que houver.
- [ ] 3.3 Conferir que os eventos de negócio (login, autorização concedida/negada) já logados têm campos estruturados sem dado sensível; criar só o que faltar.
- [ ] 3.4 Confirmar métricas Micrometer só internas (sem endpoint exposto, OTLP desligado).

## 4. Documentação da API
- [ ] 4.1 Servir a UI do contrato estático de `openapi/` somente em `local` (springdoc se compatível; senão página estática mínima).
- [ ] 4.2 Garantir `404` em `/swagger-ui*` e `/v3/api-docs*` no perfil padrão e alinhar `SecurityConfig`.

## 5. Testes
- [ ] 5.1 Teste de integração: `readiness` `200 UP` com banco ativo e `503 DOWN` com o banco parado (Testcontainers isolado); `liveness` `200`.
- [ ] 5.2 Teste: `/actuator/env`, `/actuator/metrics` e `/actuator/beans` indisponíveis e `health` sem detalhes fora de `local`.
- [ ] 5.3 Teste: `/swagger-ui*` e `/v3/api-docs*` → `404` no perfil padrão; acessível em `local`.
- [ ] 5.4 Teste do formato de log JSON ECS (`traceId`, `requestId`) e ausência de dado sensível numa requisição de login.

## 6. Documentação e verificação
- [ ] 6.1 Escrever a seção "Configuração e operação" no `README.md` do backend (perfis, variáveis, health, logs, doc da API).
- [ ] 6.2 Rodar `./mvnw verify` com o JDK 25 e corrigir falhas, incluindo `ArchitectureTest` e `ModularityTests`.
- [ ] 6.3 Verificação manual: Postgres do Compose com perfil `local`; `curl /actuator/health` → `UP` e `/actuator/env` → `404`; encerrar os processos.
- [ ] 6.4 Rodar `git grep -i "password" -- api-autorizador-processo`, confirmar só nomes de variável, registrar evidência e arquivar a spec.

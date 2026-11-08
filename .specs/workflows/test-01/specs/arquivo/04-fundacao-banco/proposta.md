# Fundação do banco

Etapa 4 de 31 · Fase 1 · derivada da `SPEC-04` (`.specs/02-backend/SPEC-04-database-foundation.md`), com o esquema de `.specs/01-architecture/DATABASE_DESIGN.md`.

## Objetivo
Introduzir PostgreSQL 16, Flyway, Testcontainers e a auditoria (RN10) em `api-autorizador-processo`, com as migrações `V1__schema_base.sql` e `V2__auditoria.sql`, perfis de configuração e seed local. **Sem módulos de negócio.** Risco tratado aqui: compatibilidade do `hibernate-envers` com o Hibernate do Boot 4.1.2 e Java 25 (spike).

## Ponto de partida (conferido contra o código da etapa 3)
- `api-autorizador-processo/` existe: Boot 4.1.2, Java 25, `spring-modulith`, `archunit-junit5`, starters webmvc/actuator/validation/opentelemetry; pacote base `org.maia.autorizador` (`Application`, `shared/logging`, `generated`); `ArchitectureTest`, `ModularityTests`, `ApplicationTests` (`@SpringBootTest` sem banco).
- **Sem** datasource, JPA, Flyway, Testcontainers nem `src/main/resources/db`; `pom.xml` sem H2. `application.yml` define `spring.profiles.default: local`, então o perfil `local` é o padrão de execução; `src/test/resources/application-test.yml` desliga `spring.ai.model.chat`.
- `.env.example` já traz `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` (vazia); a raiz tem `compose.yaml` com `postgres:16-alpine` (porta 5432, healthcheck) e `README.md`; o backend tem `.github/workflows/build.yml`.
- Docker 29.9.2 disponível no ambiente. `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).

## Escopo
1. Dependências: driver PostgreSQL, `spring-boot-starter-data-jpa`, Flyway (+ módulo PostgreSQL), Testcontainers (PostgreSQL 16), `hibernate-envers` (versão do BOM).
2. `application.yml`: `spring.datasource.*` por `DB_URL/DB_USERNAME/DB_PASSWORD`, `ddl-auto=none`, `open-in-view=false`, Flyway ativo; perfil `local` (já é o padrão, ver ponto de partida) aponta para `localhost:5432` (Compose) e acrescenta `classpath:db/seed` a `spring.flyway.locations`.
3. `V1__schema_base.sql` e `V2__auditoria.sql` exatamente como em `DATABASE_DESIGN.md` (V1: `procedimento`, `procedimento_sexo`, `procedimento_idade`, `autorizacao`; V2: `revinfo` e tabelas `*_audit`).
4. Spike do Envers com entidade mínima descartável, em PostgreSQL via Testcontainers. Se funcionar, fixar `org.hibernate.envers.audit_table_suffix=_audit`; se falhar, plano B com gatilhos PostgreSQL em `V2`. Resultado registrado em `DECISIONS.md`.
5. `db/seed/V1000__seed_local.sql` (1234 F [10,20]; 4567 M,F [20,30]; 6789 M,F [10]), só no perfil `local`, fora de `src/main/java`.
6. Classe-base de teste de integração com contêiner PostgreSQL 16 singleton e utilitário de limpeza entre testes.
7. Testes de persistência: migração limpa aplica V1+V2; UNIQUE de `numero_procedimento`; PK composta em `procedimento_sexo/idade`; CHECKs; FK impede apagar procedimento com autorização; auditoria registra INSERT.
8. Ajustar o `README.md` do backend (variáveis de banco, perfil `local`) e `.env.example` se necessário, sem segredos.

## Fora do escopo (fica para outras etapas)
- Entidades, repositórios e casos de uso de negócio: etapas 7 a 9 (SPEC-07..09). A entidade do spike é descartável e não permanece em `src/main`.
- `V3__usuario.sql` (SPEC-22) e `V4__catalogo_estendido.sql` (SPEC-12): não criar agora.
- Contrato OpenAPI (etapa 5), erros/CORS (etapa 6), ajuste do `compose.yaml` e Dockerfile (SPEC-10), observabilidade (SPEC-11), migração de dados (SPEC-50).

## Critérios de aceitação
- *Dado* banco vazio, *quando* a aplicação inicia, *então* o Flyway aplica V1–V2 e `flyway_schema_history` lista 2 versões com sucesso.
- No perfil `local`, a seed cria 3 procedimentos (`select count(*) from procedimento` = 3).
- `./mvnw verify` verde, com testes de persistência em PostgreSQL 16 real; `ArchitectureTest` e Modulith continuam sem violações.
- Nenhuma dependência de H2; migrações versionadas e imutáveis; decisão de auditoria registrada em `DECISIONS.md`.

## Riscos
- Envers pode não funcionar com o Hibernate efetivo do Boot 4.1.2 / Java 25: o spike decide; plano B por gatilhos.
- Docker indisponível impede Testcontainers e a verificação do perfil `local`: registrar bloqueio nas tarefas afetadas, não marcá-las como concluídas.
- A etapa 3 subiu o contexto sem datasource; ao adicionar JPA/Flyway, o teste de contexto e o health passam a precisar de banco (ou de Testcontainers): ajustar sem usar H2.
- Container do Compose deve ser encerrado ao final; nenhum processo deixado rodando.

## Decisões tomadas sem consulta
- **[REVISAR]** Spec derivada sem alterações de escopo da SPEC-04; 7 tarefas originais detalhadas em grupos, sem dividir além disso.
- **[REVISAR]** Se o spike do Envers falhar, adotar o plano B (gatilhos em `V2`) sem pedir aprovação, pois a SPEC-04 e a ADR-05 já preveem a alternativa.
- Entidade do spike fica apenas em `src/test`, para não antecipar módulos de negócio.
- Seed com senhas inexistentes: nenhum segredo; credenciais do banco só por variável de ambiente, com valores de exemplo apenas em `.env.example`.
- Testes de contexto passam a usar o contêiner Testcontainers (via `@ServiceConnection`), nunca H2.
- Lote 1: `.specs/06-execution-plan/` não existe (desvio registrado); Docker 29.9.2 disponível, sem bloqueio nas tarefas 4.x, 5.x e 6.x.
- Lote 1: dependências do Boot 4 — `spring-boot-starter-data-jpa`, `spring-boot-starter-flyway`, `flyway-database-postgresql`, `hibernate-envers` (7.4.13), `postgresql` (runtime), `spring-boot-testcontainers`, `testcontainers-postgresql` e `testcontainers-junit-jupiter` (test), todas pelo BOM. Sem H2.
- Lote 1: `application.yml` usa `DB_URL/DB_USERNAME/DB_PASSWORD` com padrões locais (senha vazia, sem segredo); `application-local.yml` soma `classpath:db/seed` aos locais do Flyway. Constraints nomeadas explicitamente na V1 (`uq_`, `ck_`, `pk_`, `fk_`).
- Lote 1: o Maven falhou com PKIX no ambiente; resolvido sem desligar validação, usando `MAVEN_OPTS=-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT` (apenas na execução local). `test-compile` passou.
- Lote 1: as migrações ainda não foram executadas contra o banco (testes entram nos lotes seguintes); contexto `ApplicationTests` precisará do contêiner na tarefa 5.3.
- Lote 2: spike do Envers **passou** (Hibernate 7.4.13, Java 25, PostgreSQL 16). Plano B (4.3) não aplicado. `V2` inclui `create sequence revinfo_seq increment by 50` porque o Envers do Hibernate 7 usa sequência (não identity, como previa o `DATABASE_DESIGN.md`); `revinfo.rev` ficou `integer primary key`. O desvio está em ADR-05 (`DECISIONS.md`).
- Lote 2: `audit_table_suffix=_audit` fixado em `spring.jpa.properties` do `application.yml`. Tabelas `*_audit` têm FK para `revinfo` e PK `(rev, ids)`; `revtype smallint`.
- Lote 2: entidade do spike em `src/test/java/spike/auditoria` (fora de `org.maia.autorizador`, para não ser varrida pelos demais testes nem pelo Modulith); mapeia a tabela real `procedimento`, provando que a `V2` bate com o mapeamento. O teste usa contexto próprio (`@EnableAutoConfiguration` + `@EntityScan`).
- Lote 2: seed `V1000__seed_local.sql` usa `insert ... select` por `numero_procedimento` (ids são `generated always`). Ainda não executada em banco: validação em 6.5/7.2.
- Lote 2: `IntegrationTestBase` e `LimpezaBanco` em `org.maia.autorizador.support` (test); contêiner `postgres:16-alpine` singleton iniciado em bloco estático com `@ServiceConnection`; limpeza por `truncate ... restart identity cascade` de todas as tabelas exceto `flyway_schema_history`, em `@BeforeEach`. Os testes `@SpringBootTest` existentes só passam a usar a base na tarefa 5.3 (até lá exigem banco).
- Lote 3: `IntegrationTestBase` agora usa `@ActiveProfiles("test")` (só V1 e V2, sem seed), pois o perfil padrão é `local`. `ApplicationTests` e `TracingCorrelationTests` estendem a base; `ModularityTests` não sobe contexto e ficou como está; `RequestLoggingFilterTest` não usa banco.
- Lote 3: `SeedLocalTest` (perfil `local`) usa contêiner próprio: a V1000 no mesmo banco dos contextos `test` faria o Flyway rejeitar a migração aplicada e ausente.
- Lote 3: `AuditoriaTest` reutiliza a entidade `SpikeProcedimento` (src/test) via `@TestConfiguration` + `@EntityScan`, até existir entidade real no módulo `procedimento`.
- Lote 3: `./mvnw verify` → BUILD SUCCESS, 37 testes (ArchUnit e Modulith sem violações). Testes novos em `org.maia.autorizador.persistence`.
- Lote 3 (7.2): o plugin `docker compose` não existe no shell; usei o binário `docker-compose` v5.6.1. A porta 5432 estava ocupada por outro projeto do usuário, então subi o serviço `postgres` com `POSTGRES_PORT=5433`, projeto `-p autorizador-lote3` (volume novo, removido com `down -v`) e app com `DB_URL` apontando para 5433 e `PORT=18080`. Flyway aplicou V1, V2 e V1000; `select count(*) from procedimento` = 3. App e contêiner encerrados.
- Lote 4 (7.3): `git grep -il "h2" -- api-autorizador-processo/pom.xml` → vazio. `git grep -il "legado\|legacy" -- api-autorizador-processo` retorna apenas `README.md` e `ArchitectureTest.java`, que citam os termos para descrever/impor a proibição (regra 9); nenhum pacote, classe ou identificador de negócio os usa. Considerado conforme.
- Lote 4 (7.4): resumo acrescentado à SPEC-04; a SPEC-04 foi movida com `git mv` para `.specs/archive/`.

# Fundação do banco

Etapa 4 de 31 · Fase 1 · derivada da `SPEC-04` (`.specs/02-backend/SPEC-04-database-foundation.md`), com o esquema de `.specs/01-architecture/DATABASE_DESIGN.md`.

## Objetivo
Introduzir PostgreSQL 16, Flyway, Testcontainers e a auditoria (RN10) em `api-autorizador-processo`, com as migrações `V1__schema_base.sql` e `V2__auditoria.sql`, perfis de configuração e seed local. **Sem módulos de negócio.** Risco tratado aqui: compatibilidade do `hibernate-envers` com o Hibernate do Boot 4.1.2 e Java 25 (spike).

## Ponto de partida (baseado no plano; a etapa 3 ainda não foi entregue)
- `api-autorizador-processo/` ainda não existe no working tree; a etapa 3 vai gerá-lo (Boot 4.1.2, Java 25, Modulith, ArchUnit, sem datasource, sem H2, `/actuator/health` UP).
- `.specs/01-architecture/DATABASE_DESIGN.md` e a SPEC-04 existem. `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).
- Esta proposta será revisada contra o que a etapa 3 entregar (nomes de pacote, `application.yml`, versões no `pom.xml`, `compose.yaml` da raiz).

## Escopo
1. Dependências: driver PostgreSQL, `spring-boot-starter-data-jpa`, Flyway (+ módulo PostgreSQL), Testcontainers (PostgreSQL 16), `hibernate-envers` (versão do BOM).
2. `application.yml`: `spring.datasource.*` por `DB_URL/DB_USERNAME/DB_PASSWORD`, `ddl-auto=none`, `open-in-view=false`, Flyway ativo; perfil `local` aponta para `localhost:5432` (Compose) e acrescenta `classpath:db/seed` a `spring.flyway.locations`.
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
- A etapa 3 exigia contexto sem datasource; ao adicionar JPA/Flyway, o teste de contexto e o health passam a precisar de banco (ou de Testcontainers): ajustar sem usar H2.
- Container do Compose deve ser encerrado ao final; nenhum processo deixado rodando.

## Decisões tomadas sem consulta
- **[REVISAR]** Spec derivada sem alterações de escopo da SPEC-04; 7 tarefas originais detalhadas em grupos, sem dividir além disso.
- **[REVISAR]** Se o spike do Envers falhar, adotar o plano B (gatilhos em `V2`) sem pedir aprovação, pois a SPEC-04 e a ADR-05 já preveem a alternativa.
- Entidade do spike fica apenas em `src/test`, para não antecipar módulos de negócio.
- Seed com senhas inexistentes: nenhum segredo; credenciais do banco só por variável de ambiente, com valores de exemplo apenas em `.env.example`.
- Testes de contexto passam a usar o contêiner Testcontainers (via `@ServiceConnection`), nunca H2.

# Tarefas — Fundação do banco

## 1. Preparação
- [ ] 1.1 Ler `INDEX.md`, `memory/`, `shared/como-executar.md`, `DATABASE_DESIGN.md`, `DECISIONS.md` (ADR-05, ADR-14) e as referências `flyway-migrations.md` e `testing-and-observability.md` da skill `java-persistence-performance`; registrar o desvio de `.specs/06-execution-plan/`
- [ ] 1.2 Conferir o estado atual de `api-autorizador-processo` (`pom.xml`, `application.yml` com perfil padrão `local`, `application-test.yml`, pacote `org.maia.autorizador`, `ArchitectureTest`, `ModularityTests`); configurar `JAVA_HOME=C:\jdk-25.0.2`
- [ ] 1.3 Verificar `docker version`; se indisponível, registrar bloqueio nas tarefas 4.x, 5.x e 6.x que dependem de contêiner

## 2. Dependências e configuração
- [ ] 2.1 Adicionar ao `pom.xml` driver PostgreSQL, `spring-boot-starter-data-jpa`, Flyway com módulo PostgreSQL, Testcontainers (PostgreSQL) e `hibernate-envers`, com versões do BOM; confirmar ausência de H2 (T1)
- [ ] 2.2 Configurar `application.yml`: datasource por `DB_URL/DB_USERNAME/DB_PASSWORD`, `ddl-auto=none`, `open-in-view=false`, Flyway ativo (T2)
- [ ] 2.3 Criar `application-local.yml` apontando para `localhost:5432` e acrescentando `classpath:db/seed` aos locais do Flyway (T2)
- [ ] 2.4 Atualizar `.env.example` e o `README.md` do backend com as variáveis de banco e o perfil `local`, sem segredos

## 3. Migrações e seed
- [ ] 3.1 Criar `db/migration/V1__schema_base.sql` conforme `DATABASE_DESIGN.md` (tabelas, PKs, FKs, CHECKs, UNIQUE e índice `ix_autorizacao_procedimento`) (T3)
- [ ] 3.2 Criar `db/migration/V2__auditoria.sql` com `revinfo` e as quatro tabelas `*_audit` (T3)
- [ ] 3.3 Criar `db/seed/V1000__seed_local.sql` com 1234, 4567 e 6789 (sexos e idades), fora de `src/main/java` e só no perfil `local` (T5)

## 4. Spike de auditoria
- [ ] 4.1 Criar entidade mínima descartável em `src/test` com `@Audited` e executá-la em PostgreSQL 16 via Testcontainers com Java 25 (T4)
- [ ] 4.2 Se o Envers funcionar, fixar `org.hibernate.envers.audit_table_suffix=_audit` e confirmar que as tabelas de `V2` batem com o mapeamento (T4)
- [ ] 4.3 Se falhar, implementar o plano B (gatilhos PostgreSQL em `V2` gravando as `*_audit`) mantendo o mesmo esquema (T4)
- [ ] 4.4 Registrar o resultado do spike e a decisão em `DECISIONS.md` (ADR-05) (T4)

## 5. Infraestrutura de testes
- [ ] 5.1 Criar classe-base de integração com contêiner PostgreSQL 16 singleton reutilizável (`@ServiceConnection`) (T6)
- [ ] 5.2 Criar utilitário de limpeza do banco entre testes (T6)
- [ ] 5.3 Ajustar `ApplicationTests`, `ModularityTests` e os testes que sobem contexto para usar o contêiner, sem H2

## 6. Testes de persistência
- [ ] 6.1 Teste: migração limpa aplica V1 e V2 e `flyway_schema_history` lista 2 versões com sucesso (T7)
- [ ] 6.2 Testes: UNIQUE de `numero_procedimento`, PK composta rejeitando duplicata em `procedimento_sexo/idade` e CHECKs (`numero > 0`, sexo `M/F`, idade 0–150) (T7)
- [ ] 6.3 Teste: FK impede apagar procedimento com autorização; `ON DELETE CASCADE` em sexos/idades (T7)
- [ ] 6.4 Teste: auditoria registra INSERT em `*_audit` com `revinfo` (T7)
- [ ] 6.5 Teste com perfil `local`: a seed cria 3 procedimentos com sexos e idades esperados

## 7. Validação e encerramento
- [ ] 7.1 Rodar `./mvnw verify` em `api-autorizador-processo` e obter `BUILD SUCCESS` (ArchUnit e Modulith sem violações)
- [ ] 7.2 Subir `docker compose up -d postgres`, iniciar a app no perfil `local`, conferir `select count(*) from procedimento` = 3 e encerrar app e contêiner
- [ ] 7.3 Conferir `git grep -il "h2" -- api-autorizador-processo/pom.xml` vazio e `git grep -il "legado\|legacy" -- api-autorizador-processo` vazio
- [ ] 7.4 Registrar evidência por tarefa, resumo na spec e `git mv` da spec para `.specs/archive/`

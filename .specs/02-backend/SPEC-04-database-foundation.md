# SPEC-04 — Fundação do banco (PostgreSQL 16 · Flyway · auditoria)

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · persistência |
| Prioridade | P0 |
| Depende de | SPEC-03 |
| Sucessoras | SPEC-07, SPEC-10, SPEC-22, SPEC-50 |
| Paralelismo | junto com SPEC-05 |

## 1. Objetivo
Introduzir PostgreSQL 16, Flyway, Testcontainers e a auditoria (RN10), com o esquema V1/V2 de `DATABASE_DESIGN.md`, perfis de configuração e seed local. Sem código de negócio ainda. **Risco tratado aqui:** compatibilidade do Envers com o Hibernate do Boot 4.1.2.
**Restrições:** sem H2; `ddl-auto=none`; migrations imutáveis; seed fora de `src/main/java` e só no perfil `local`; senhas só por variável.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `01-architecture/DATABASE_DESIGN.md`, `DECISIONS.md` (ADR-05, ADR-14), skill `java-persistence-performance` (`flyway-migrations.md`, `testing-and-observability.md`) — aplicar integralmente; onde cita o produto de origem (`.spec/`, `owner_id`), usar `.specs/` e a autenticação deste projeto (sem multi-tenant)

## 3. Tarefas
- [ ] **T1.** Dependências: driver PostgreSQL, `spring-boot-starter-data-jpa`, Flyway (+ módulo PostgreSQL), Testcontainers (PostgreSQL 16), `hibernate-envers` (versão do BOM).
- [ ] **T2.** `application.yml`: `spring.datasource.*` via `DB_URL/DB_USERNAME/DB_PASSWORD`, `jpa.hibernate.ddl-auto=none`, `open-in-view=false`, `flyway` ativo; perfil `local` aponta para `postgres` do Compose (`localhost:5432`) e acrescenta `classpath:db/seed`.
- [ ] **T3.** `V1__schema_base.sql` e `V2__auditoria.sql` exatamente conforme `DATABASE_DESIGN.md`.
- [ ] **T4.** **Spike Envers:** entidade mínima de teste (descartável) auditada em PostgreSQL via Testcontainers, com Java 25 e o Hibernate efetivo. Se funcionar, fixar a configuração (`org.hibernate.envers.audit_table_suffix=_audit`); se falhar, implementar o **plano B** (gatilhos PostgreSQL em `V2`) e registrar a decisão em `DECISIONS.md`.
- [ ] **T5.** `db/seed/V1000__seed_local.sql` (1234, 4567, 6789) apenas no perfil `local`.
- [ ] **T6.** Classe-base de teste de integração com contêiner PostgreSQL 16 reutilizável (singleton) e utilitário de limpeza entre testes.
- [ ] **T7.** Testes: migração limpa aplica V1+V2; UNIQUE de `numero_procedimento`; PK composta rejeita duplicata em `procedimento_sexo/idade`; CHECKs; FK impede apagar procedimento com autorização; auditoria registra INSERT.

**Aceite:** *Given* banco vazio · *When* a aplicação inicia · *Then* Flyway aplica V1–V2 e `flyway_schema_history` lista 2 versões com sucesso; com perfil `local`, a seed cria 3 procedimentos.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T7 | `java-persistence-performance`, `java-modern-engineering` |
| T6–T7 | Testcontainers; Docker |

## 5. Verificação obrigatória
- `./mvnw verify` → testes de persistência verdes (PostgreSQL 16 real)
- `docker compose up -d postgres` + app no perfil `local` → seed presente (`select count(*) from procedimento` = 3)
- Registro do resultado do spike (Envers OK ou plano B) em `DECISIONS.md`
- Definition of Done: migrations versionadas e aplicadas, testes executados, decisão de auditoria registrada, nenhuma dependência de H2.

## 6. Resultado
_Preencher ao concluir._

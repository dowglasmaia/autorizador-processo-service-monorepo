# SPEC-50 — Migração de dados da versão original (CONDICIONAL)

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · banco |
| Prioridade | P3 — **condicional** (PRM-02) |
| Depende de | SPEC-04, SPEC-07, SPEC-08, SPEC-12 |
| Sucessoras | nenhuma (não bloqueia a finalização) |
| Condição de início | existir base real da versão original (MySQL/H2) com dados a preservar; sem isso, marcar **cancelada sem necessidade** com motivo, conforme `como-executar.md` §2 |

## 1. Objetivo
Importar, sem perda, procedimentos e autorizações da base original para o PostgreSQL 16, caso existam dados reais. O discovery não encontrou evidência de dados de produção (dev usa `create-drop`; teste usa H2 semeado). **Não apagar nada na origem.**
**Restrições:** somente leitura na origem; idempotente; relatório obrigatório de contagens; sem executar contra produção sem autorização expressa; nenhum nome "legado/legacy" no script (usar `origem`/`importacao`).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `DATABASE_DESIGN.md` §4, `BASELINE_COMPORTAMENTO_ORIGINAL.md` §2; skill `java-persistence-performance` (`flyway-migrations.md`)

## 3. Tarefas
- [ ] **T1.** Confirmar com o responsável a existência e o local da base de origem; registrar a decisão (executar ou cancelar).
- [ ] **T2.** Script de importação `tools/importacao/importar.mjs` (ou SQL) lendo `procedimento`, `sexos`, `idades`, `solicitar_procedimento` da origem.
- [ ] **T3.** Regras: `sexos` 1→`M`, 2→`F`, **outros valores rejeitados e listados** (ex.: o código 3 da sondagem); de-duplicar `sexos/idades`; preservar `numero_procedimento`; novos campos NEW-01 nulos; `autorizacao.criada_em = NULL`; `permitido` preservado; mapear `procedimento_id` pelo número.
- [ ] **T4.** Relatório: contagens origem × destino por tabela e lista de linhas rejeitadas; falha se houver divergência não justificada.
- [ ] **T5.** Teste com base de origem sintética (JDK 8 em contêiner, perfil `test` com dados de `BASELINE`) → importação → conferência via API.

**Aceite:** *Given* a base de origem sintética com 3 procedimentos e 2 autorizações · *When* importar duas vezes · *Then* 3 e 2 linhas no destino (idempotente) e relatório sem divergência.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T2–T5 | `java-persistence-performance`; Node/SQL; Docker |

## 5. Verificação obrigatória
- Execução do T5 com relatório de contagens
- Definition of Done: importação idempotente verificada, rejeições listadas, nenhum dado de origem alterado — ou spec cancelada com motivo aprovado.

## 6. Resultado
**Status: não aplicável — cancelada sem necessidade** (`como-executar.md` §2). Data: 2026-11-08 (etapa 14 do loop `test-01`).

**Motivo:** a condição de início (base real da versão original com dados a preservar) não se verificou. O discovery já indicava ausência de dados de produção (dev `create-drop`; teste com H2 semeado) e a busca na etapa 14 não achou nenhuma origem acessível. Decisão tomada em modo automático, sem consulta ao responsável (T1); se existir uma base fora do repositório, reabrir esta spec. [REVISAR]

**Evidência (comandos reproduzíveis, sem valores sensíveis):**
- `find . ( -name node_modules -o -name .git -o -name target ) -prune -o ( -name '*.mv.db' -o -name '*.dump' -o -name '*.bak' -o -name '*.sql' ) -print` → apenas as migrations Flyway V1–V4 e `db/seed/V1000__seed_local.sql` do backend novo; nenhum dump ou base de origem.
- `grep -nEi 'origem|mysql|h2|source_db|legacy' .env.example compose.yaml` → sem ocorrências.
- `env | grep -Ei '^(ORIGEM|MYSQL|H2|SOURCE_DB)'` → nenhuma variável.

**T2–T5:** não executadas (sem origem); `tools/importacao/` não foi criado. Nenhum dado, credencial ou dump foi versionado.

# SPEC-42 — Smoke de desempenho e dimensionamento

| Campo | Valor |
|---|---|
| Domínio / projeto | Qualidade · transversal |
| Prioridade | P3 |
| Depende de | SPEC-30 |
| Sucessoras | SPEC-43 |
| Paralelismo | junto com SPEC-40 e SPEC-41 |

## 1. Objetivo
Medir (não presumir) o desempenho contra as metas provisórias de `NON_FUNCTIONAL_REQUIREMENTS.md` e ajustar índices, tamanhos de página e tamanho do bundle com base em evidência.
**Restrições:** dados sintéticos gerados por script (nunca reais); só alterar índices/consultas com `EXPLAIN` e ganho medido; registrar baseline antes de otimizar; nenhuma mudança de comportamento.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `NON_FUNCTIONAL_REQUIREMENTS.md` (NFR-02..04), `DATABASE_DESIGN.md` §3; skill `java-persistence-performance` (`sql-indexing-performance.md`, `decision-matrices.md`)

## 3. Tarefas
- [ ] **T1.** Script de carga de dados sintéticos (1.000 procedimentos, 10.000 autorizações) para o PostgreSQL do Compose.
- [ ] **T2.** Carga HTTP simples (20 conexões, 60 s) em `GET /procedimentos`, `POST /autorizacoes`, `GET /autorizacoes`, `GET /painel/resumo`; registrar p50/p95/p99 e taxa de erro.
- [ ] **T3.** Contar SQL por requisição nas listas (confirmar ausência de N+1) e `EXPLAIN (ANALYZE)` das consultas do painel.
- [ ] **T4.** Medir bundle inicial e LCP simulado (Lighthouse CI ou equivalente).
- [ ] **T5.** Se alguma meta falhar: diagnosticar causa, propor e aplicar o menor ajuste (índice via migration nova; `@BatchSize`; lazy route), remedir.
- [ ] **T6.** Registrar números e conclusões em `.specs/05-quality/PERFORMANCE_REPORT.md`; ajustar metas se a base medida justificar.

**Aceite:** relatório com medições reais; metas NFR-02..04 atendidas ou justificadas com plano.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T3, T5 | `java-persistence-performance` |
| T4, T5 | `angular-architect` |

## 5. Verificação obrigatória
- Execução real das cargas com números registrados (sem estimativas)
- `./mvnw verify` e `npm test` verdes após ajustes
- Definition of Done: `PERFORMANCE_REPORT.md` preenchido com dados medidos.

## 6. Resultado
_Preencher ao concluir._

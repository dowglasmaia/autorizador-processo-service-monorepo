# SPEC-09 — Módulo `painel` (resumo agregado) — NEW-02

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · módulo `painel` |
| Prioridade | P2 |
| Depende de | SPEC-07, SPEC-08 |
| Sucessoras | SPEC-20 |
| Paralelismo | pode rodar junto com SPEC-17, SPEC-18, SPEC-19 |

## 1. Objetivo
Criar `GET /api/v1/painel/resumo`, que o protótipo exige e a versão original não tem: totais, idades cobertas, cobertura por idade, distribuição por sexo e últimas 4 autorizações, calculados **no servidor** por SQL agregado (ADR-11). Somente leitura; **não** persiste nada novo.
**Restrições:** não inventar métricas além das do protótipo; o KPI "Aprovação" **não** é calculado aqui (negativas não são persistidas — PEN-01); `distribuicaoSexos` conta **procedimentos que aceitam** cada sexo (um procedimento M,F conta nos dois), como `nM/nF` do protótipo; consulta `readOnly`.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` (`ResumoPainel`), `DATABASE_DESIGN.md` §3, `TEST_STRATEGY.md` cenário 10; protótipo `.specs/design/prototype/Autorizador.dc.html` (`renderVals`)

## 3. Tarefas
- [ ] **T1.** Scaffold `painel` no estilo `crud` de leitura (sem entidade própria): `ObterResumoPainelUseCase`, porta `PainelConsulta`, adaptador com consultas SQL agregadas (JdbcClient/JPQL) em `infrastructure`.
- [ ] **T2.** Consultas: totais; `idade,count(*)` agrupado e ordenado; `sexo,count(distinct procedimento_id)`; últimas 4 autorizações com procedimento (reutilizar o mapeamento de `autorizacao`).
- [ ] **T3.** `PainelController` implementando a interface gerada.
- [ ] **T4.** Testes: PostgreSQL com dados conhecidos (cenário 10 de `TEST_STRATEGY`: totais 3/0, idades 10→2, 20→2, 30→1, M=2, F=3), banco vazio (zeros, listas vazias), `WebMvc`.
- [ ] **T5.** Medir tempo e SQL com 1.000 procedimentos / 10.000 autorizações (referência para SPEC-42).

**Aceite:** *Given* seed local e nenhuma autorização · *When* `GET /painel/resumo` · *Then* `totalProcedimentos=3`, `totalAutorizacoes=0`, `idadesCobertas=3`, `coberturaPorIdade=[{10,2},{20,2},{30,1}]`, `distribuicaoSexos={masculino:2,feminino:3}`, `ultimasAutorizacoes=[]`.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T3 | `java-spring-create-module`, `java-modern-engineering` |
| T2, T4–T5 | `java-persistence-performance` |

## 5. Verificação obrigatória
- `check-architecture.mjs` → 0 violações; `./mvnw verify` → verde
- Registro de SQL/tempo do T5
- Definition of Done: contrato respeitado, valores do aceite verificados por teste executado.

## 6. Resultado
Concluída em 2026-11-08 pela etapa `11-modulo-painel` do loop `test-01` (16 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/11-modulo-painel/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.

# SPEC-07 — Módulo `procedimento` (paridade com a versão original)

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · módulo `procedimento` |
| Prioridade | P1 |
| Depende de | SPEC-04, SPEC-05, SPEC-06, SPEC-22 |
| Sucessoras | SPEC-08, SPEC-09, SPEC-12, SPEC-17, SPEC-18, SPEC-31, SPEC-50 |
| Paralelismo | nenhum com 08/12 (compartilham o agregado) |

## 1. Objetivo
Entregar o fluxo completo do catálogo no modelo Hexagonal Pragmática: cadastrar, listar (paginado) e obter procedimento, preservando RN1–RN4, RN9, RN10 e corrigindo D2/D7/D8 (DIV-02, DIV-03, DIV-06, DIV-07). Inclui a regra de domínio `Procedimento.avaliar(idade, sexo)` (RN5–RN7) usada por SPEC-08.
**Estilo:** `rich` (D1: invariantes e cálculo). Sem campos NEW-01 (SPEC-12).
**Restrições:** endpoints **protegidos** (SPEC-22): testes usam o utilitário de JWT de teste; domínio puro; `numProcedimento` imutável; cadastro com listas vazias permitido (RN9); entrada sem `id`; não expor entidade JPA; `@Transactional` no caso de uso.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md`, `DATABASE_DESIGN.md`, `PRODUCT_CONTEXT.md` §7, baseline B01–B08; skills `java-spring-create-module` (`decision-guide.md`, exemplo `order`), `java-persistence-performance`

## 3. Tarefas
- [ ] **T1.** Executar `scaffold-module.mjs --feature procedimento --entity Procedimento --style rich` com o contrato da SPEC-05; registrar decisões D1–D6.
- [ ] **T2.** Domínio (tests-first): `Procedimento.create(num, sexos, idades)` com invariantes (`num ≥ 1`, idades `0..150`, conjuntos sem duplicatas), `avaliar(idade, sexo)` → boolean (sexo ∈ sexos **E** idade ∈ idades; sexo normalizado sem caixa); enum `Sexo` (M, F) com `parse` aceitando minúsculas.
- [ ] **T3.** Casos de uso: `CadastrarProcedimentoUseCase` (unicidade; violação de UNIQUE → `ProcedimentoDuplicadoException`), `ListarProcedimentosUseCase` (filtro `q`, ordenação, paginação), `ObterProcedimentoUseCase` (por id e por número → `ProcedimentoNaoEncontradoException`).
- [ ] **T4.** Porta `ProcedimentoRepository` + adaptador JPA (`ProcedimentoEntity`, `@ElementCollection` para sexos/idades com PK composta e `@BatchSize`), mapeamento manual Entity↔Domínio; auditoria conforme decisão da SPEC-04.
- [ ] **T5.** `ProcedimentoController` implementando a interface gerada; `201` com corpo e `Location`; `idades` ordenadas na saída.
- [ ] **T6.** Testes por camada (domínio, casos de uso com fake, `@DataJpaTest` PostgreSQL, `@WebMvcTest`, `@SpringBootTest` ponta a ponta) incluindo **concorrência** (duas inserções simultâneas do mesmo número → um `201`, um `403`).
- [ ] **T7.** Medir SQL por requisição da listagem (sem N+1) e registrar a contagem.

**Aceite (G/W/T):** *Given* seed local · *When* `GET /api/v1/procedimentos` · *Then* 200 com 3 itens, `idades` ascendentes. *Given* 9999 inexistente · *When* `POST` com `sexos:["M","F"]`,`idades:[18,30]` · *Then* 201 e `GET` o retorna. *When* repetir · *Then* 403 `PROCEDIMENTO_DUPLICADO`. *When* `sexos:["X"]` · *Then* 400 e `GET` continua 200.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T5 | `java-spring-create-module` (obrigatória), `java-modern-engineering` (records, sealed/pattern matching só se simplificar) |
| T4, T6–T7 | `java-persistence-performance` |
| T5 | `openapi-rest-contract` (conferência com o contrato) |

## 5. Verificação obrigatória
- `node <skill>/scripts/check-architecture.mjs --src api-autorizador-processo/src/main/java --base-package org.maia.autorizador` → 0 violações
- `./mvnw verify` → verde (inclui `ArchitectureTest`, Modulith, testes de concorrência)
- `node tools/parity/replay.mjs --target v1 --base-url http://localhost:8080 --only procedimento --email "$AUTH_BOOTSTRAP_EMAIL" --senha "$AUTH_BOOTSTRAP_SENHA"` → casos de procedimento OK (parcial; fechamento em SPEC-31)
- Definition of Done: RN1–RN4, RN9, RN10 cobertas por testes executados; sem N+1; contrato respeitado.

## 6. Resultado
_Preencher ao concluir._

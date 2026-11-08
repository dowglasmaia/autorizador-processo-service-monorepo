# Tarefas — Módulo procedimento

## 1. Preparação
- [x] 1.1 Confirmar o que a etapa 7 entregou (`SecurityConfig`, `JwtTestSupport`, `VarreduraSegurancaTest`, `401` no contrato) e o resíduo `spike/auditoria`; ler `API_CONTRACTS.md`, `DATABASE_DESIGN.md`, `PRODUCT_CONTEXT.md` §7 e baseline B01–B08; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Ler a skill `java-spring-create-module` (`decision-guide.md`, exemplo `order`) e executar `scaffold-module.mjs --feature procedimento --entity Procedimento --style rich`; registrar as decisões D1–D6.

## 2. Domínio
- [x] 2.1 Escrever os testes do domínio (invariantes, `avaliar`, `Sexo.parse`, listas vazias) antes da implementação.
- [x] 2.2 Implementar `Procedimento.create`, `avaliar` e o enum `Sexo` com `parse` sem diferença de caixa; `numProcedimento` imutável.
- [x] 2.3 Criar `ProcedimentoDuplicadoException` e `ProcedimentoNaoEncontradoException` sobre `BusinessRuleException`.

## 3. Casos de uso e porta
- [x] 3.1 Definir a porta `ProcedimentoRepository` (salvar, por id, por número, página com filtro `q`).
- [x] 3.2 Implementar `CadastrarProcedimentoUseCase` com unicidade e tradução da violação de UNIQUE.
- [x] 3.3 Implementar `ListarProcedimentosUseCase` (filtro por prefixo, ordenação, paginação) e `ObterProcedimentoUseCase`.
- [x] 3.4 Testar os casos de uso com repositório fake.

## 4. Persistência
- [x] 4.1 Criar `ProcedimentoEntity` com `@ElementCollection` para sexos e idades, `@BatchSize` e `@Audited`, validada contra `V1` e `V2`.
- [x] 4.2 Implementar o adaptador JPA e o mapeamento manual entidade↔domínio.
- [x] 4.3 Remover `src/test/java/spike/auditoria` (duplica a tabela `procedimento`), aproveitando sua verificação de revisão Envers nos testes da entidade real.
- [x] 4.4 Testar a persistência com PostgreSQL (Testcontainers): ida e volta, unicidade e auditoria da criação.

## 5. API
- [x] 5.1 Implementar `ProcedimentoController` sobre a interface gerada (`201` com corpo e `Location`, `idades` ascendentes, `sexos` em M, F).
- [x] 5.2 Testar com `@WebMvcTest`: `201`, `400` (sexo inválido, número `0`, idade fora da faixa, JSON malformado), `403` e `404`; confirmar que `VarreduraSegurancaTest` cobre as rotas novas (`401` sem token).

## 6. Testes ponta a ponta e desempenho
- [x] 6.1 Testar ponta a ponta com JWT de teste: listagem do seed, cadastro seguido de consulta, duplicidade e entrada inválida sem quebrar a listagem.
- [x] 6.2 Testar a concorrência: duas inserções simultâneas do mesmo número → um `201` e um `403`.
  > ✅ 2026-11-08 16:05 +00:00 — Duas threads disparam o mesmo POST via MockMvc; resultado 201 + 403 e uma única linha no banco. Verificação: `ProcedimentoConcorrenciaDesempenhoTest` (Testcontainers) verde.
- [x] 6.3 Medir o SQL por requisição da listagem (sem N+1) e registrar a contagem na evidência.
  > ✅ 2026-11-08 16:05 +00:00 — Contagem de SQL por requisição da listagem (Hibernate Statistics): 3 comandos com 3 itens e 3 com 23 itens (itens + sexos + idades; sem N+1). Verificação: mesmo teste, asserção de igualdade.

## 7. Verificação e encerramento
- [x] 7.1 Estender o `ArchitectureTest` ao módulo e rodar `check-architecture.mjs` (0 violações); conferir a ausência de "legado/legacy".
  > ✅ 2026-11-08 16:05 +00:00 — Regra `procedimento_domain_sem_framework` adicionada ao `ArchitectureTest` (21 regras verdes); `check-architecture.mjs`: 47 arquivos, 0 violações; nenhuma ocorrência de legado/legacy em `src/main`.
- [x] 7.2 Rodar `./mvnw verify` e corrigir as falhas; registrar bloqueio se o Docker faltar.
  > ✅ 2026-11-08 16:05 +00:00 — `./mvnw verify` (JDK 25, Docker disponível): 138 testes, 0 falhas, BUILD SUCCESS.
- [x] 7.3 Rodar o replay dos golden cases de procedimento (`tools/parity/replay.mjs --only procedimento`) e registrar as diferenças aceitas.
  > ✅ 2026-11-08 16:05 +00:00 — Replay `--only procedimento --target v1` contra a API real (jar + PostgreSQL 16 em contêiner descartável, removidos ao fim): 8/8 OK (B01–B08). Diferenças aceitas: as DIV já mapeadas no `cases.json` (DIV-02/03/04/06/07).
- [x] 7.4 Registrar evidência por tarefa e resumo, atualizar o `INDEX.md` e arquivar a spec com `git mv`.
  > ✅ 2026-11-08 16:05 +00:00 — Evidência registrada; SPEC-07 movida com `git mv` para `.specs/archive/`; `INDEX.md` atualizado.

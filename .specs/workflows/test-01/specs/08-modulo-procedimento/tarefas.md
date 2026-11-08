# Tarefas — Módulo procedimento

## 1. Preparação
- [ ] 1.1 Confirmar o que a etapa 7 entregou (`SecurityConfig`, `JwtTestSupport`, `VarreduraSegurancaTest`, `401` no contrato) e o resíduo `spike/auditoria`; ler `API_CONTRACTS.md`, `DATABASE_DESIGN.md`, `PRODUCT_CONTEXT.md` §7 e baseline B01–B08; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Ler a skill `java-spring-create-module` (`decision-guide.md`, exemplo `order`) e executar `scaffold-module.mjs --feature procedimento --entity Procedimento --style rich`; registrar as decisões D1–D6.

## 2. Domínio
- [ ] 2.1 Escrever os testes do domínio (invariantes, `avaliar`, `Sexo.parse`, listas vazias) antes da implementação.
- [ ] 2.2 Implementar `Procedimento.create`, `avaliar` e o enum `Sexo` com `parse` sem diferença de caixa; `numProcedimento` imutável.
- [ ] 2.3 Criar `ProcedimentoDuplicadoException` e `ProcedimentoNaoEncontradoException` sobre `BusinessRuleException`.

## 3. Casos de uso e porta
- [ ] 3.1 Definir a porta `ProcedimentoRepository` (salvar, por id, por número, página com filtro `q`).
- [ ] 3.2 Implementar `CadastrarProcedimentoUseCase` com unicidade e tradução da violação de UNIQUE.
- [ ] 3.3 Implementar `ListarProcedimentosUseCase` (filtro por prefixo, ordenação, paginação) e `ObterProcedimentoUseCase`.
- [ ] 3.4 Testar os casos de uso com repositório fake.

## 4. Persistência
- [ ] 4.1 Criar `ProcedimentoEntity` com `@ElementCollection` para sexos e idades, `@BatchSize` e `@Audited`, validada contra `V1` e `V2`.
- [ ] 4.2 Implementar o adaptador JPA e o mapeamento manual entidade↔domínio.
- [ ] 4.3 Remover `src/test/java/spike/auditoria` (duplica a tabela `procedimento`), aproveitando sua verificação de revisão Envers nos testes da entidade real.
- [ ] 4.4 Testar a persistência com PostgreSQL (Testcontainers): ida e volta, unicidade e auditoria da criação.

## 5. API
- [ ] 5.1 Implementar `ProcedimentoController` sobre a interface gerada (`201` com corpo e `Location`, `idades` ascendentes, `sexos` em M, F).
- [ ] 5.2 Testar com `@WebMvcTest`: `201`, `400` (sexo inválido, número `0`, idade fora da faixa, JSON malformado), `403` e `404`; confirmar que `VarreduraSegurancaTest` cobre as rotas novas (`401` sem token).

## 6. Testes ponta a ponta e desempenho
- [ ] 6.1 Testar ponta a ponta com JWT de teste: listagem do seed, cadastro seguido de consulta, duplicidade e entrada inválida sem quebrar a listagem.
- [ ] 6.2 Testar a concorrência: duas inserções simultâneas do mesmo número → um `201` e um `403`.
- [ ] 6.3 Medir o SQL por requisição da listagem (sem N+1) e registrar a contagem na evidência.

## 7. Verificação e encerramento
- [ ] 7.1 Estender o `ArchitectureTest` ao módulo e rodar `check-architecture.mjs` (0 violações); conferir a ausência de "legado/legacy".
- [ ] 7.2 Rodar `./mvnw verify` e corrigir as falhas; registrar bloqueio se o Docker faltar.
- [ ] 7.3 Rodar o replay dos golden cases de procedimento (`tools/parity/replay.mjs --only procedimento`) e registrar as diferenças aceitas.
- [ ] 7.4 Registrar evidência por tarefa e resumo, atualizar o `INDEX.md` e arquivar a spec com `git mv`.

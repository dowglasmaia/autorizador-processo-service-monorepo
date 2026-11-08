# Tarefas — Módulo painel

## 1. Preparação
- [ ] 1.1 Reconferir o que a etapa 10 entregou em `autorizacao` (casos de uso, portas, mapeamento do DTO `Autorizacao`) e ler `API_CONTRACTS.md` (`ResumoPainel`), `DATABASE_DESIGN.md` §3, `TEST_STRATEGY.md` cenário 10 e `renderVals` do protótipo; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Ler as skills `java-spring-create-module` e `java-persistence-performance` e criar o esqueleto do módulo `painel` (estilo leitura, sem entidade), registrando as decisões.

## 2. Casos de uso e porta
- [ ] 2.1 Definir a porta `PainelConsulta` e o modelo de resultado do resumo (sem tipos gerados nem JPA).
- [ ] 2.2 Escrever os testes de `ObterResumoPainelUseCase` com porta fake antes da implementação (valores do seed, vazio, máximo de 4 autorizações).
- [ ] 2.3 Implementar `ObterResumoPainelUseCase` (`@Transactional(readOnly = true)`).

## 3. Consultas agregadas
- [ ] 3.1 Implementar as consultas de totais (procedimentos e autorizações) e de `idade, count(*)` agrupado e ordenado por idade.
- [ ] 3.2 Implementar `sexo, count(distinct procedimento_id)` para `distribuicaoSexos`, devolvendo zero para sexo ausente.
- [ ] 3.3 Implementar as últimas 4 autorizações (`criada_em desc nulls last, id desc`) com seus procedimentos em número constante de consultas.
- [ ] 3.4 Testar o adaptador com PostgreSQL (Testcontainers): cenário 10 (3/0, 10→2, 20→2, 30→1, M=2, F=3), banco vazio, procedimento M,F contado nos dois e mais de 4 autorizações.

## 4. API
- [ ] 4.1 Implementar `PainelController` sobre a interface gerada, montando `Autorizacao`/`Procedimento` a partir do resultado, sem usar `domain.model` de outro módulo.
- [ ] 4.2 Testar com `@WebMvcTest` (200 com corpo completo) e confirmar `401` sem token em `VarreduraSegurancaTest`.
- [ ] 4.3 Testar ponta a ponta com JWT de teste o aceite (seed, nenhuma autorização) e após uma autorização criada.

## 5. Arquitetura e desempenho
- [ ] 5.1 Estender o `ArchitectureTest` ao módulo `painel` (domínio/aplicação sem framework, sem acesso à infraestrutura de outros módulos) com 0 violações, e conferir `ModularityTests`.
- [ ] 5.2 Medir tempo e número de SQL de `GET /painel/resumo` com 1.000 procedimentos / 10.000 autorizações e registrar como referência para a SPEC-42; decidir sobre índice só se necessário.

## 6. Fechamento
- [ ] 6.1 Rodar `./mvnw verify` em `api-autorizador-processo`, corrigir falhas e conferir que não há "legado/legacy" no backend.
- [ ] 6.2 Registrar evidências, desvios e o resumo conforme `como-executar.md`.

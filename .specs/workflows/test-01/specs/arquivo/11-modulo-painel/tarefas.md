# Tarefas — Módulo painel

## 1. Preparação
- [x] 1.1 Reconferir `ListarAutorizacoesUseCase` e o controller de `autorizacao` entregues na etapa 10 (API pública a reutilizar; mapeamento do DTO é privado) e ler `API_CONTRACTS.md` (`ResumoPainel`), `DATABASE_DESIGN.md` §3, `TEST_STRATEGY.md` cenário 10 e `renderVals` do protótipo; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Ler as skills `java-spring-create-module` e `java-persistence-performance` e criar o esqueleto do módulo `painel` (estilo leitura, sem entidade), registrando as decisões.

## 2. Casos de uso e porta
- [x] 2.1 Definir a porta `PainelConsulta` e o modelo de resultado do resumo (sem tipos gerados nem JPA).
- [x] 2.2 Escrever os testes de `ObterResumoPainelUseCase` com porta fake antes da implementação (valores do seed, vazio, máximo de 4 autorizações).
- [x] 2.3 Implementar `ObterResumoPainelUseCase` (`@Transactional(readOnly = true)`).

## 3. Consultas agregadas
- [x] 3.1 Implementar as consultas de totais (procedimentos e autorizações) e de `idade, count(*)` agrupado e ordenado por idade.
- [x] 3.2 Implementar `sexo, count(distinct procedimento_id)` para `distribuicaoSexos`, devolvendo zero para sexo ausente.
- [x] 3.3 Obter as últimas 4 autorizações via `ListarAutorizacoesUseCase.executar(0, 4)` (porta/serviço em `painel` que o encapsula), com procedimentos em número constante de consultas e `criada_em` nulo tratado.
- [x] 3.4 Testar o adaptador com PostgreSQL (Testcontainers): cenário 10 (3/0, 10→2, 20→2, 30→1, M=2, F=3), banco vazio, procedimento M,F contado nos dois e mais de 4 autorizações.

## 4. API
- [x] 4.1 Implementar `PainelController` sobre a interface gerada, com mapeador próprio de `Autorizacao`/`Procedimento` gerados a partir do resultado, sem usar `domain.model` de outro módulo.
- [x] 4.2 Testar com `@WebMvcTest` (200 com corpo completo) e confirmar `401` sem token em `VarreduraSegurancaTest`.
- [x] 4.3 Testar ponta a ponta com JWT de teste o aceite (seed, nenhuma autorização) e após uma autorização criada.

## 5. Arquitetura e desempenho
- [x] 5.1 Estender o `ArchitectureTest` ao módulo `painel` (domínio/aplicação sem framework, sem acesso à infraestrutura de outros módulos) com 0 violações, e conferir `ModularityTests`.
- [x] 5.2 Medir tempo e número de SQL de `GET /painel/resumo` com 1.000 procedimentos / 10.000 autorizações e registrar como referência para a SPEC-42; decidir sobre índice só se necessário.

## 6. Fechamento
- [x] 6.1 Rodar `./mvnw verify` em `api-autorizador-processo`, corrigir falhas e conferir que não há "legado/legacy" no backend.
- [x] 6.2 Registrar evidências, desvios e o resumo conforme `como-executar.md`.

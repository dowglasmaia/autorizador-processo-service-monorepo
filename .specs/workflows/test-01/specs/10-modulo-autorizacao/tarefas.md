# Tarefas — Módulo autorizacao

## 1. Preparação
- [ ] 1.1 Reconferir o que a etapa 9 entregou em `procedimento` (domínio, `Resultado`, contrato) e ler `API_CONTRACTS.md`, `PRODUCT_CONTEXT.md` §6–7 e baseline B09–B15; registrar a ausência de `.specs/06-execution-plan/`.
- [ ] 1.2 Ler a skill `java-spring-create-module` e executar `scaffold-module.mjs --feature autorizacao --entity Autorizacao --style rich`; registrar as decisões D1–D6.

## 2. Domínio
- [ ] 2.1 Escrever os testes do domínio antes da implementação (`conceder` sempre `permitido=true`, exceção de negativa).
- [ ] 2.2 Implementar `Autorizacao.conceder(procedimento)` e `Autorizacao.restore`, sem setters.
- [ ] 2.3 Criar `AutorizacaoNegadaException` (`AUTORIZACAO_NEGADA`, 403) sobre `BusinessRuleException`.

## 3. Casos de uso e porta
- [ ] 3.1 Definir a porta `AutorizacaoRepository` (salvar, listar página mais recentes primeiro).
- [ ] 3.2 Implementar `SolicitarAutorizacaoUseCase` (`@Transactional`, sexo normalizado, negativa sem persistir, log sem dados do paciente).
- [ ] 3.3 Implementar `ListarAutorizacoesUseCase` com paginação e procedimento carregado em lote.
- [ ] 3.4 Testar os casos de uso com repositório fake: autorizado, negado por sexo, negado por idade, RN9, inexistente → 404, sexo minúsculo, `Masculino` → erro de validação.

## 4. Persistência
- [ ] 4.1 Criar `AutorizacaoEntity` com FK para `procedimento` e `@Audited`, validada contra `V1` e `V2`.
- [ ] 4.2 Implementar o adaptador JPA e o mapeamento entidade↔domínio, com listagem sem N+1.
- [ ] 4.3 Testar a persistência com PostgreSQL (Testcontainers): ida e volta, duas autorizações do mesmo procedimento (RN11), nada gravado na negativa e auditoria da criação.

## 5. API
- [ ] 5.1 Implementar `AutorizacaoController` sobre a interface gerada (`201` com corpo, `criadaEm` em UTC).
- [ ] 5.2 Testar com `@WebMvcTest`: `201`, `400` (sexo inválido, idade fora da faixa, JSON malformado), `403` e `404`; confirmar `401` sem token em `VarreduraSegurancaTest`.

## 6. Testes ponta a ponta e desempenho
- [ ] 6.1 Testar ponta a ponta com JWT de teste os cenários de aceite (4567/20/F → 201 e listada; 1234/30/M → 403 sem mudar a contagem; 55555 → 404).
- [ ] 6.2 Medir o SQL por requisição de `GET /autorizacoes` com 3 e 23 itens (Hibernate Statistics), afirmar contagem constante e registrar.

## 7. Verificação e encerramento
- [ ] 7.1 Estender o `ArchitectureTest` ao módulo, rodar `check-architecture.mjs` (0 violações) e conferir a ausência de "legado/legacy".
- [ ] 7.2 Rodar `./mvnw verify` e corrigir as falhas; registrar bloqueio se o Docker faltar.
- [ ] 7.3 Rodar o replay dos golden cases (`tools/parity/replay.mjs --only autorizacao`) e registrar as diferenças aceitas.

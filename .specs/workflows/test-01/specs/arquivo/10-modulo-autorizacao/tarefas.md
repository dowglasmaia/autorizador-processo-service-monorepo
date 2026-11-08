# Tarefas — Módulo autorizacao

## 1. Preparação
- [x] 1.1 Reconferir o que as etapas 8 e 9 entregaram em `procedimento` (`DadosCatalogo`, `Resultado`/`Catalogo`, porta, `ArchitectureTest`) e ler `API_CONTRACTS.md`, `PRODUCT_CONTEXT.md` §6–7 e baseline B09–B15; registrar a ausência de `.specs/06-execution-plan/`.
- [x] 1.2 Ler a skill `java-spring-create-module` e executar o scaffold do módulo (`--feature autorizacao --entity Autorizacao --style rich`) ou, se o script não existir, criar a estrutura à mão no padrão de `procedimento`; registrar as decisões D1–D6. _(O script existe e foi rodado com `--dry-run`: geraria 20 arquivos genéricos em inglês e duplicaria `shared`; a estrutura foi criada à mão no padrão de `procedimento`.)_

## 2. Domínio
- [x] 2.1 Escrever os testes do domínio antes da implementação (`conceder` sempre `permitido=true`, exceção de negativa).
- [x] 2.2 Implementar `Autorizacao.conceder(procedimento)` e `Autorizacao.restore`, sem setters.
- [x] 2.3 Criar `AutorizacaoNegadaException` (`AUTORIZACAO_NEGADA`, 403) sobre `BusinessRuleException`.

## 3. Casos de uso e porta
- [x] 3.0 Acrescentar, de forma aditiva, `ProcedimentoRepository.buscarPorIds`, seu adaptador (uma consulta) e `ObterProcedimentoUseCase.porIds`, com teste de que a regressão das etapas 8 e 9 segue verde.
- [x] 3.1 Definir a porta `AutorizacaoRepository` (salvar, listar página mais recentes primeiro).
- [x] 3.2 Implementar `SolicitarAutorizacaoUseCase` (`@Transactional`, sexo normalizado, negativa sem persistir, log sem dados do paciente).
- [x] 3.3 Implementar `ListarAutorizacoesUseCase` com paginação e procedimento carregado em lote.
- [x] 3.4 Testar os casos de uso com repositório fake: autorizado, negado por sexo, negado por idade, RN9, inexistente → 404, sexo minúsculo, `Masculino` → erro de validação.

## 4. Persistência
- [x] 4.1 Criar `AutorizacaoEntity` com FK para `procedimento` e `@Audited`, validada contra `V1` e `V2`.
- [x] 4.2 Implementar o adaptador JPA e o mapeamento entidade↔domínio, com listagem sem N+1.
- [x] 4.3 Testar a persistência com PostgreSQL (Testcontainers): ida e volta, duas autorizações do mesmo procedimento (RN11), nada gravado na negativa e auditoria da criação.

## 5. API
- [x] 5.1 Implementar `AutorizacaoController` sobre a interface gerada (`201` com corpo, `criadaEm` em UTC), montando o `Procedimento` gerado a partir de `Resultado`/`Catalogo`, sem usar `domain.model`.
- [x] 5.2 Testar com `@WebMvcTest`: `201`, `400` (sexo inválido, idade fora da faixa, JSON malformado), `403` e `404`; confirmar `401` sem token em `VarreduraSegurancaTest`.

## 6. Testes ponta a ponta e desempenho
- [x] 6.1 Testar ponta a ponta com JWT de teste os cenários de aceite (4567/20/F → 201 e listada; 1234/30/M → 403 sem mudar a contagem; 55555 → 404).
- [x] 6.2 Medir o SQL por requisição de `GET /autorizacoes` com 3 e 23 itens (Hibernate Statistics), afirmar contagem constante e registrar. _(`AutorizacaoDesempenhoTest`: 4 statements com 3 itens e 4 com 23.)_

## 7. Verificação e encerramento
- [x] 7.1 Estender o `ArchitectureTest` ao módulo (domínio sem framework, controller sem `domain.model`), confirmar 0 violações (o `check-architecture.mjs` não existe) e conferir a ausência de "legado/legacy". _(Regra `autorizacao_domain_sem_framework` adicionada; a regra de controller já cobria o módulo; 0 violações; `grep` de legado/legacy em `src/main` sem ocorrências.)_
- [x] 7.2 Rodar `./mvnw verify` e corrigir as falhas; registrar bloqueio se o Docker faltar. _(Docker disponível; 210 testes, 0 falhas, 0 erros, jar gerado.)_
- [x] 7.3 Rodar o replay dos golden cases (`tools/parity/replay.mjs --only autorizacao`) se servidor e banco estiverem disponíveis; senão registrar como não executado e deixar para a SPEC-31. _(NÃO EXECUTADO: exige a API v1 e o PostgreSQL no ar, e não se deixam processos rodando no loop. Os cenários B09–B15 já são cobertos por `AutorizacaoApiTest`; replay fica para a SPEC-31.)_

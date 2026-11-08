# Módulo procedimento

Etapa 8 de 31 · Fase 2 · derivada da `SPEC-07` (`.specs/02-backend/SPEC-07-modulo-procedimento.md`). Proposta reconferida contra o código entregue pela etapa 7 (autenticação JWT).

## Objetivo
Entregar o fluxo do catálogo no módulo `procedimento` em Hexagonal Pragmática (pacote por funcionalidade, estilo `rich`): cadastrar, listar (paginado) e obter procedimento, com paridade com a versão original nas regras RN1–RN4, RN9 e RN10 e com as correções DIV-02, DIV-03, DIV-06 e DIV-07. Inclui a regra de domínio `Procedimento.avaliar(idade, sexo)` (RN5–RN7), que a etapa de `autorizacao` vai consumir. Os endpoints são protegidos pelo JWT da etapa 7.

## Ponto de partida (conferido após a etapa 7)
- `api-autorizador-processo/` tem `Application`, `shared.error.BusinessRuleException`, `shared.web.GlobalExceptionHandler` e `CorsConfig`, `shared.logging.RequestLoggingFilter`, migrações `V1__schema_base.sql` (tabelas `procedimento`, `procedimento_sexo`, `procedimento_idade`, `autorizacao`), `V2__auditoria.sql` (`*_audit`, `revinfo`), seed local, `hibernate-envers` no `pom.xml`, `ArchitectureTest`, `ModularityTests` e `IntegrationTestBase` (Testcontainers).
- O contrato `openapi/modules/procedimento.yaml` já existe (etapa 5): `listarProcedimentos` (`q`, `page`, `size`, `sort`), `cadastrarProcedimento` (`201` com corpo e `Location`; `400`, `403`), `obterProcedimento` (`404`). A interface é gerada em `org.maia.autorizador.generated`. O contrato não será alterado, salvo ajuste mínimo exigido pelo `401` global que a etapa 7 introduzir.
- Não existe nenhum pacote de feature em `src/main`: o módulo `procedimento` é novo. A pasta `tools/parity/` (golden cases e `replay.mjs`) existe desde a etapa 2.
- `.specs/06-execution-plan/` não existe (desvio a registrar na evidência).
- **Entregue pela etapa 7:** módulo `autenticacao` (`api`, `application`, `domain`, `infrastructure`), `shared.security.SecurityConfig` (JWT HS256, `401`/`403` em RFC 9457), migração `V3__usuario.sql`, `responses.yaml` com `401` e `support.JwtTestSupport` (`cabecalhoBearer(id, email)`) para os testes protegidos. `VarreduraSegurancaTest` percorre todos os mapeamentos MVC e exige `401` sem token; as rotas novas entram nela automaticamente.
- **Resíduo a tratar:** `src/test/java/spike/auditoria/` (`SpikeProcedimento` mapeia a tabela real `procedimento` com `@Audited`, ADR-05). Ao criar `ProcedimentoEntity` haverá duas entidades na mesma tabela; o spike precisa ser removido ou migrado para a entidade real (seus testes passam a cobrir a auditoria do módulo).

## Escopo
1. Gerar o esqueleto com a skill `java-spring-create-module` (`--feature procedimento --entity Procedimento --style rich`), a partir do contrato; registrar as decisões D1–D6 da skill.
2. Domínio puro (tests-first): `Procedimento.create(num, sexos, idades)` com invariantes (`num ≥ 1`, idades `0..150`, conjuntos sem duplicatas, listas vazias permitidas — RN9), `avaliar(idade, sexo)` (sexo ∈ sexos **E** idade ∈ idades, sexo sem diferença de caixa), enum `Sexo` (`M`, `F`) com `parse` que aceita minúsculas; `numProcedimento` imutável.
3. Casos de uso: `CadastrarProcedimentoUseCase` (unicidade; violação do UNIQUE vira `ProcedimentoDuplicadoException`, `403 PROCEDIMENTO_DUPLICADO`), `ListarProcedimentosUseCase` (`q` por prefixo, ordenação, paginação) e `ObterProcedimentoUseCase` (por id e por número; `ProcedimentoNaoEncontradoException`, `404 PROCEDIMENTO_NAO_ENCONTRADO`), com `@Transactional` no caso de uso. As exceções estendem `BusinessRuleException` da etapa 6.
4. Porta `ProcedimentoRepository` e adaptador JPA (`ProcedimentoEntity`, `@ElementCollection` para sexos e idades sobre `procedimento_sexo`/`procedimento_idade`, `@BatchSize`, `@Audited` para a auditoria Envers — RN10), mapeamento manual entidade↔domínio, sem expor a entidade.
5. `ProcedimentoController` sobre a interface gerada: `201` com corpo e `Location` para `/api/v1/procedimentos/{id}`, `idades` ascendentes e `sexos` na ordem M, F na saída, entrada sem `id`.
6. Testes por camada: domínio, casos de uso com fake, persistência com PostgreSQL (Testcontainers), `@WebMvcTest`, `@SpringBootTest` ponta a ponta com JWT de teste e concorrência (duas inserções simultâneas do mesmo número → um `201` e um `403`); auditoria (revisão criada no cadastro).
7. Medir o SQL por requisição da listagem (sem N+1) e registrar a contagem na evidência.
8. `ArchitectureTest` estendido ao módulo e `check-architecture.mjs` com 0 violações; replay dos golden cases de procedimento (parcial; fechamento na etapa de paridade).

## Fora do escopo (fica para outras etapas)
- Catálogo estendido (`nome`, `codigoTuss` etc., NEW-01): SPEC-12.
- Módulos `autorizacao` e `painel` (usam `Procedimento.avaliar`): etapas seguintes.
- Telas de procedimentos no frontend: SPEC-17 e SPEC-18.
- Fechamento da paridade com o baseline: etapa de paridade (SPEC-31); migração de dados: SPEC-50.
- Alterações no contrato além do necessário; a autenticação em si (etapa 7).

## Critérios de aceitação
- *Dado* o seed local, *quando* `GET /api/v1/procedimentos` com token válido, *então* `200` com 3 itens e `idades` ascendentes.
- *Dado* o número 9999 inexistente, *quando* `POST` com `sexos:["M","F"]` e `idades:[18,30]`, *então* `201` com corpo e `Location`, e o `GET` o retorna; repetir → `403 PROCEDIMENTO_DUPLICADO`.
- `sexos:["X"]`, número `0`, idade fora de `0..150` ou JSON malformado → `400` e a listagem continua `200`; `sexos` em minúsculas são aceitos; listas vazias são aceitas.
- `GET /procedimentos/{id}` inexistente → `404 PROCEDIMENTO_NAO_ENCONTRADO`; sem token → `401`.
- Concorrência: duas inserções simultâneas do mesmo número resultam em um `201` e um `403`.
- Listagem sem N+1 (contagem de SQL registrada); o cadastro gera registro de auditoria.
- `./mvnw verify` verde (com `ArchitectureTest` e `ModularityTests`; Testcontainers exige Docker, senão registrar bloqueio); nenhum nome "legado/legacy".

## Riscos
- Docker indisponível bloqueia os testes de persistência e de concorrência; registrar sem marcar como concluídos.
- Corrida no cadastro: a checagem prévia não basta; o `UNIQUE` do banco é a garantia e a violação precisa ser traduzida (cuidado com a transação marcada para rollback).
- `@ElementCollection` com `@Audited` e Hibernate 7 pode gerar N+1 ou tabelas de auditoria divergentes de `V2`; validar contra o esquema existente.
- Duas entidades JPA para a tabela `procedimento` (spike de auditoria e a real) quebram o contexto ou a auditoria; remover o spike na mesma tarefa que cria a entidade.
- O script `scaffold-module.mjs` pode assumir convenções diferentes do código existente; ajustar à mão e registrar.

## Decisões tomadas sem consulta
- **Contrato aprovado:** o `procedimento.yaml` da etapa 5 e a SPEC-07 valem como aprovação; nenhuma alteração além do necessário.
- **Sem catálogo estendido:** campos NEW-01 ficam fora, conforme o limite do plano.
- **Sexo na entrada:** aceita `M`/`F` em qualquer caixa (DIV-02); qualquer outro valor é `400`.
- **[REVISAR] Auditoria com `@Audited`:** usada para atender RN10 sobre as tabelas `_audit` já criadas na etapa 4; se o Envers divergir do esquema, ajustar a entidade e não a migração.
- **[REVISAR] Status da duplicidade:** `403 PROCEDIMENTO_DUPLICADO`, como na versão original e no contrato (e não `409`).
- **Spike de auditoria:** `spike/auditoria` é descartável (ADR-05); é substituído pelos testes da entidade real, sem alterar a migração `V2`.
- **Skill de módulo:** o esqueleto da skill é ponto de partida; o código existente tem precedência em caso de conflito.

# Módulo autorizacao

Etapa 10 de 31 · Fase 2 · derivada da `SPEC-08` (`.specs/02-backend/SPEC-08-modulo-autorizacao.md`). Proposta antecipada, escrita enquanto a etapa 9 (catálogo estendido) era implementada; deve ser revisada contra o que a etapa 9 entregar.

## Objetivo
Implementar o módulo `autorizacao`: **solicitar** uma autorização (`POST /api/v1/autorizacoes`) e consultar o **histórico** (`GET /api/v1/autorizacoes`), preservando RN5–RN8, RN10 e RN11 e corrigindo D1 (DIV-01, DIV-05, DIV-08). Fluxo: carregar o procedimento por número → `Procedimento.avaliar(idade, sexo)` → se verdadeiro, gravar `Autorizacao(permitido=true)` e devolver `201`; se falso, `403 AUTORIZACAO_NEGADA` **sem gravar** (RN8). Dados do paciente (idade, sexo) não vão na URL nem são persistidos (PEN-01); `permitido` é sempre `true`.

## Ponto de partida (conferido no código atual, após a etapa 8)
- Não existe o pacote `autorizacao` em `src/main/java`. Já existem `autenticacao`, `procedimento` e `shared`.
- Contrato `openapi/modules/autorizacao.yaml` pronto: `solicitarAutorizacao` (201/400/403/404/500), `listarAutorizacoes` (`page`, `size` 1..200), `AutorizacaoRequest` (`numProcedimento`, `idade` 0..150, `sexo` `^[MFmf]$`), `Autorizacao` (`id`, `procedimento`, `permitido`, `criadaEm` anulável) e `PaginaAutorizacoes`. `Autorizacao.procedimento` referencia `procedimento.yaml#Procedimento`, portanto herda os campos novos da etapa 9. **Nenhuma mudança de contrato nesta etapa.**
- Banco: tabela `autorizacao` (`id`, `procedimento_id` NOT NULL com FK, `permitido`, `criada_em` default `now()`), índice `ix_autorizacao_procedimento` e `autorizacao_audit` (V1/V2). Sem coluna de idade ou sexo; nenhuma migração nova é prevista.
- `procedimento` expõe `ObterProcedimentoUseCase.porNumero(int)` (devolve o domínio `Procedimento`, lança `ProcedimentoNaoEncontradoException`, 404 `PROCEDIMENTO_NAO_ENCONTRADO`), `Procedimento.avaliar(int, Sexo|String)` e `Sexo.parse` (sem diferença de caixa, `IllegalArgumentException` para inválidos, mapeada para 400 pelo `GlobalExceptionHandler`). A porta `ProcedimentoRepository` só tem busca por id/número e listagem; **não há busca em lote por ids**.
- `BusinessRuleException` com `Categoria.OPERACAO_NAO_PERMITIDA` já mapeia para 403 no `GlobalExceptionHandler`; falta só a exceção do módulo com `code=AUTORIZACAO_NEGADA`.
- `ArchitectureTest` (21 regras) e `ModularityTests` existem; falta a regra do domínio de `autorizacao`. `SecurityConfig`/`VarreduraSegurancaTest` já exigem JWT para `/api/v1/**`.
- A etapa 9 altera `Procedimento` (campos de catálogo) e `ObterProcedimentoUseCase.Resultado` de forma aditiva; esta etapa **não** altera `procedimento` (SPEC-08, paralelismo) e deve ser reconferida contra o entregue pela etapa 9. `.specs/06-execution-plan/` não existe (desvio a registrar).

## Escopo
1. Scaffold do módulo `autorizacao` (skill `java-spring-create-module`, estilo `rich`) e registro das decisões D1–D6.
2. Domínio: `Autorizacao.conceder(procedimento)` (sempre `permitido=true`) e `AutorizacaoNegadaException` (`AUTORIZACAO_NEGADA`, 403). Reutiliza `ProcedimentoNaoEncontradoException`.
3. `SolicitarAutorizacaoUseCase` (`@Transactional`): obtém o procedimento por número, normaliza o sexo (`m`→`M`), avalia; negativa lança a exceção **sem persistir**; log de evento de negócio (concedida/negada) sem idade, sexo nem outro dado do paciente.
4. `ListarAutorizacoesUseCase` (paginado, das mais recentes para as mais antigas) com os procedimentos carregados em lote (sem N+1).
5. Porta `AutorizacaoRepository` e adaptador JPA (`AutorizacaoEntity` com FK para `procedimento`, `@Audited`, RN10).
6. `AutorizacaoController` sobre a interface gerada; `400` para sexo inválido (`Masculino`) e idade fora da faixa.
7. Testes: domínio e casos de uso (autorizado, negado por sexo, negado por idade, procedimento sem sexos/idades = RN9, inexistente → 404, sexo minúsculo, `Masculino` → 400); persistência com PostgreSQL (nada gravado na negativa; duas autorizações do mesmo procedimento = RN11; auditoria); `@WebMvcTest`; ponta a ponta; contagem de SQL constante de `GET /autorizacoes`.
8. `ArchitectureTest` estendido ao módulo; `check-architecture.mjs` com 0 violações; replay dos golden cases `--only autorizacao`.

## Fora do escopo (fica para outras etapas)
- Módulo `painel` e qualquer agregação/estatística: etapa seguinte (SPEC-09).
- Configuração/observabilidade (SPEC-11), telas do frontend (SPEC-18/19), fechamento da paridade (SPEC-31) e migração de dados (SPEC-50).
- Alterar `procedimento` ou o contrato; guardar idade/sexo do solicitante; gravar negativas; papéis de usuário.

## Critérios de aceitação
- *Dado* o seed, *quando* `POST /api/v1/autorizacoes {4567,20,"F"}` → `201` e `GET /autorizacoes` a lista (mais recente primeiro).
- *Quando* `{1234,30,"M"}` → `403 AUTORIZACAO_NEGADA` e a contagem de autorizações **não muda**.
- *Quando* `{55555,30,"M"}` → `404 PROCEDIMENTO_NAO_ENCONTRADO`; sexo `Masculino` ou idade fora de 0..150 → `400`.
- Sexo minúsculo é aceito; procedimento sem sexos ou idades nega sempre (RN9); duas autorizações do mesmo procedimento coexistem (RN11).
- A contagem de SQL de `GET /autorizacoes` é constante com 3 e com 23 itens (registrada).
- `./mvnw verify` verde (incluindo `ArchitectureTest`, `ModularityTests` e a regressão das etapas 8 e 9); sem nome "legado/legacy"; Docker é necessário para Testcontainers, senão registrar bloqueio.

## Riscos
- `autorizacao` precisa do procedimento no domínio: depender só do caso de uso/domínio de `procedimento`, nunca da infraestrutura (ArchUnit/Modulith).
- Carga em lote do procedimento: a porta de `procedimento` não tem busca por vários ids e não deve ser alterada; resolver com `@ManyToOne` na entidade da autorização e `JOIN FETCH`/`EntityGraph`, evitando N+1.
- O DTO gerado `Procedimento` (com campos da etapa 9) deve ser montado a partir dos dados do catálogo; revisar contra a etapa 9.
- `criada_em` é anulável e em UTC.
- Docker indisponível bloqueia testes de persistência e ponta a ponta.

## Decisões tomadas sem consulta
- **Contrato aprovado:** a SPEC-08 e `API_CONTRACTS.md` valem como aprovação; nenhuma alteração em `autorizacao.yaml`.
- **Sem migração:** o esquema V1/V2 já cobre a autorização; idade e sexo não são persistidos (PEN-01).
- **Negativa por exceção:** `AutorizacaoNegadaException` estende `BusinessRuleException` com `OPERACAO_NAO_PERMITIDA` (403), reaproveitando o mapeamento existente.
- **Reuso de `procedimento` pelo caso de uso:** `autorizacao` chama `ObterProcedimentoUseCase.porNumero` e usa o domínio `Procedimento`; `procedimento` não é alterado.
- **[REVISAR] Carga em lote na entidade:** em vez de ampliar a porta de `procedimento`, `AutorizacaoEntity` mapeia `@ManyToOne` para a tabela `procedimento` com fetch em lote na listagem; reavaliar se o `ModularityTests` aceita essa dependência de persistência entre módulos.
- **[REVISAR] Procedimento aninhado:** a resposta traz o `Procedimento` completo, incluindo campos da etapa 9 (nulos no seed); confirmar contra o entregue pela etapa 9.

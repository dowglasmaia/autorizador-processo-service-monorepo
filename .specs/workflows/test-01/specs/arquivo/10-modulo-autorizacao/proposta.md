# Módulo autorizacao

Etapa 10 de 31 · Fase 2 · derivada da `SPEC-08` (`.specs/02-backend/SPEC-08-modulo-autorizacao.md`). Proposta revisada contra o código já entregue pelas etapas 8 e 9 (catálogo estendido concluído).

## Objetivo
Implementar o módulo `autorizacao`: **solicitar** uma autorização (`POST /api/v1/autorizacoes`) e consultar o **histórico** (`GET /api/v1/autorizacoes`), preservando RN5–RN8, RN10 e RN11 e corrigindo D1 (DIV-01, DIV-05, DIV-08). Fluxo: carregar o procedimento por número → `Procedimento.avaliar(idade, sexo)` → se verdadeiro, gravar `Autorizacao(permitido=true)` e devolver `201`; se falso, `403 AUTORIZACAO_NEGADA` **sem gravar** (RN8). Dados do paciente (idade, sexo) não vão na URL nem são persistidos (PEN-01); `permitido` é sempre `true`.

## Ponto de partida (conferido no código atual, após as etapas 8 e 9)
- Não existe o pacote `autorizacao` em `src/main/java`. Já existem `autenticacao`, `procedimento` e `shared`.
- Contrato `openapi/modules/autorizacao.yaml` pronto: `solicitarAutorizacao` (201/400/403/404/500), `listarAutorizacoes` (`page`, `size` 1..200), `AutorizacaoRequest` (`numProcedimento`, `idade` 0..150, `sexo` `^[MFmf]$`), `Autorizacao` (`id`, `procedimento`, `permitido`, `criadaEm` anulável) e `PaginaAutorizacoes`. `Autorizacao.procedimento` referencia `procedimento.yaml#Procedimento`, portanto herda os campos novos da etapa 9. **Nenhuma mudança de contrato nesta etapa.**
- Banco: tabela `autorizacao` (`id`, `procedimento_id` NOT NULL com FK, `permitido`, `criada_em` default `now()`), índice `ix_autorizacao_procedimento` e `autorizacao_audit` (V1/V2); a V4 da etapa 9 só tocou `procedimento`/`procedimento_audit`. Sem coluna de idade ou sexo; nenhuma migração nova é prevista.
- `procedimento` expõe `ObterProcedimentoUseCase.porNumero(int)` (devolve o domínio `Procedimento`, lança `ProcedimentoNaoEncontradoException`, 404 `PROCEDIMENTO_NAO_ENCONTRADO`), `Procedimento.avaliar(int, Sexo|String)` e `Sexo.parse` (sem diferença de caixa, `IllegalArgumentException` para inválidos, mapeada para 400 pelo `GlobalExceptionHandler`). A porta `ProcedimentoRepository` só tem `buscarPorId`, `buscarPorNumero` e `listar`; **não há busca em lote por ids**. `ProcedimentoEntity` é pacote-privada, então `autorizacao` não pode mapear `@ManyToOne` para ela.
- `BusinessRuleException` com `Categoria.OPERACAO_NAO_PERMITIDA` já mapeia para 403 no `GlobalExceptionHandler`; falta só a exceção do módulo com `code=AUTORIZACAO_NEGADA`.
- `ArchitectureTest` (21 regras) e `ModularityTests` existem; falta a regra do domínio de `autorizacao`. `SecurityConfig`/`VarreduraSegurancaTest` já exigem JWT para `/api/v1/**`.
- A etapa 9 entregou `DadosCatalogo` em `Procedimento` (campos de catálogo nulos no seed) e `ObterProcedimentoUseCase.Resultado` com `Catalogo` (enums como texto). O `ArchitectureTest` proíbe o controller de usar `domain.model`; o controller de `autorizacao` deve montar o `Procedimento` gerado a partir de `ObterProcedimentoUseCase.Resultado`. `Pagina` está em `procedimento.domain.model`; `autorizacao` terá seu próprio resultado paginado.
- `check-architecture.mjs` não existe no repositório (registrado na etapa 9): a verificação equivalente é o `ArchitectureTest`. `tools/parity/replay.mjs` e `cases.json` existem desde a etapa 2. `.specs/06-execution-plan/` não existe (desvio a registrar).

## Escopo
1. Scaffold do módulo `autorizacao` (skill `java-spring-create-module`, estilo `rich`) e registro das decisões D1–D6.
2. Domínio: `Autorizacao.conceder(procedimento)` (sempre `permitido=true`) e `AutorizacaoNegadaException` (`AUTORIZACAO_NEGADA`, 403). Reutiliza `ProcedimentoNaoEncontradoException`.
3. `SolicitarAutorizacaoUseCase` (`@Transactional`): obtém o procedimento por número, normaliza o sexo (`m`→`M`), avalia; negativa lança a exceção **sem persistir**; log de evento de negócio (concedida/negada) sem idade, sexo nem outro dado do paciente.
4. `ListarAutorizacoesUseCase` (paginado, das mais recentes para as mais antigas) com os procedimentos carregados em lote (sem N+1).
5. Porta `AutorizacaoRepository` e adaptador JPA (`AutorizacaoEntity` com FK para `procedimento`, `@Audited`, RN10).
6. `AutorizacaoController` sobre a interface gerada; `400` para sexo inválido (`Masculino`) e idade fora da faixa.
7. Testes: domínio e casos de uso (autorizado, negado por sexo, negado por idade, procedimento sem sexos/idades = RN9, inexistente → 404, sexo minúsculo, `Masculino` → 400); persistência com PostgreSQL (nada gravado na negativa; duas autorizações do mesmo procedimento = RN11; auditoria); `@WebMvcTest`; ponta a ponta; contagem de SQL constante de `GET /autorizacoes`.
8. `ArchitectureTest` estendido ao módulo (0 violações); replay dos golden cases `--only autorizacao` se houver servidor e banco disponíveis.

## Fora do escopo (fica para outras etapas)
- Módulo `painel` e qualquer agregação/estatística: etapa seguinte (SPEC-09).
- Configuração/observabilidade (SPEC-11), telas do frontend (SPEC-18/19), fechamento da paridade (SPEC-31) e migração de dados (SPEC-50).
- Alterar o comportamento de `procedimento` ou o contrato (só se admite o acréscimo aditivo da busca em lote, ver decisões); guardar idade/sexo do solicitante; gravar negativas; papéis de usuário.

## Critérios de aceitação
- *Dado* o seed, *quando* `POST /api/v1/autorizacoes {4567,20,"F"}` → `201` e `GET /autorizacoes` a lista (mais recente primeiro).
- *Quando* `{1234,30,"M"}` → `403 AUTORIZACAO_NEGADA` e a contagem de autorizações **não muda**.
- *Quando* `{55555,30,"M"}` → `404 PROCEDIMENTO_NAO_ENCONTRADO`; sexo `Masculino` ou idade fora de 0..150 → `400`.
- Sexo minúsculo é aceito; procedimento sem sexos ou idades nega sempre (RN9); duas autorizações do mesmo procedimento coexistem (RN11).
- A contagem de SQL de `GET /autorizacoes` é constante com 3 e com 23 itens (registrada).
- `./mvnw verify` verde (incluindo `ArchitectureTest`, `ModularityTests` e a regressão das etapas 8 e 9); sem nome "legado/legacy"; Docker é necessário para Testcontainers, senão registrar bloqueio.

## Riscos
- `autorizacao` precisa do procedimento no domínio: depender só do caso de uso/domínio de `procedimento`, nunca da infraestrutura (ArchUnit/Modulith).
- Carga em lote do procedimento sem N+1: a entidade de `procedimento` é pacote-privada; resolver com a busca em lote aditiva (ver decisões) e provar a contagem de SQL constante.
- O DTO gerado `Procedimento` (com campos da etapa 9) deve ser montado a partir de `Resultado`/`Catalogo`, convertendo o texto nos enums gerados como faz `ProcedimentoController`.
- `criada_em` é anulável e em UTC.
- Docker indisponível bloqueia testes de persistência e ponta a ponta.

## Decisões tomadas sem consulta
- **Contrato aprovado:** a SPEC-08 e `API_CONTRACTS.md` valem como aprovação; nenhuma alteração em `autorizacao.yaml`.
- **Sem migração:** o esquema V1/V2 já cobre a autorização; idade e sexo não são persistidos (PEN-01).
- **Negativa por exceção:** `AutorizacaoNegadaException` estende `BusinessRuleException` com `OPERACAO_NAO_PERMITIDA` (403), reaproveitando o mapeamento existente.
- **Reuso de `procedimento` pelo caso de uso:** `autorizacao` chama `ObterProcedimentoUseCase.porNumero` e usa o domínio `Procedimento`; `procedimento` não é alterado.
- **[REVISAR] Carga em lote por acréscimo aditivo em `procedimento`:** `ProcedimentoEntity` é pacote-privada e a porta não busca por vários ids, então `@ManyToOne` entre módulos é inviável. Decisão: acrescentar `ProcedimentoRepository.buscarPorIds(Collection<Long>)` (implementada com uma consulta, mantendo o `@BatchSize` das coleções) e expor `ObterProcedimentoUseCase.porIds`, sem alterar nenhum comportamento existente. Isso contraria a restrição "não alterar `procedimento`" da SPEC-08, cuja razão (paralelismo com a SPEC-12) deixou de existir porque a etapa 9 já foi entregue. `AutorizacaoEntity` guarda só `procedimento_id` (coluna da FK).
- **Paginação própria:** `autorizacao` define seu resultado paginado nos casos de uso, sem importar `procedimento.domain.model.Pagina`, para respeitar `ModularityTests`/ArchUnit.
- **Procedimento aninhado:** a resposta traz o `Procedimento` completo via `Resultado`/`Catalogo` (campos de catálogo nulos no seed); nenhuma mudança de contrato.
- **Verificação de arquitetura:** `check-architecture.mjs` não existe; vale o `ArchitectureTest`. Desvio: `.specs/06-execution-plan/` não existe.
- **Scaffold feito à mão (tarefa 1.2):** `scaffold-module.mjs --dry-run` geraria 20 arquivos genéricos (nomes em inglês, `CreateAutorizacaoUseCase`, `ApiError`, `shared/config/ClockConfig` duplicado). Criei a estrutura à mão no padrão de `procedimento` (`domain/model`, `domain/exception`, `application/port`, `application/usecase`). D1 rich (regra de negação e invariante); D2 porta só para o repositório; D3 mapeamento manual no adaptador; D4 um caso de uso por operação; D5 controller sobre a interface gerada; D6 `autorizacao` usa casos de uso e domínio de `procedimento`, nunca a infraestrutura. `.specs/06-execution-plan/` não existe (ignorado).
- **[REVISAR] `@NamedInterface` em `procedimento`:** o `ModularityTests` rejeita o uso de tipos de subpacotes de outro módulo. Acrescentei `package-info.java` com `@NamedInterface` em `procedimento.application.usecase`, `domain.model` e `domain.exception` e fiz o `ArchitectureTest` ignorar classes `package-info` (`SemPackageInfo`), pois as regras de pacote as tratavam como classes de verdade. Nenhum comportamento mudou.
- **`Pagina` de `autorizacao`:** vive em `autorizacao.domain.model.Pagina` (a regra `portas_sao_interfaces` impede classes aninhadas na porta).
- **`Autorizacao` guarda só `procedimentoId`:** `conceder` exige procedimento já gravado (`id` não nulo). `SolicitarAutorizacaoUseCase.executar` devolve `Concedida(id, Resultado, permitido, criadaEm)` para o controller não tocar `domain.model`; valida o sexo antes de buscar o procedimento (sexo inválido → 400 mesmo com número inexistente).
- **Verificação do lote 1:** `AutorizacaoTest`, testes de `procedimento` (inclusive PostgreSQL e `buscarPorIds`), `ArchitectureTest` e `ModularityTests` verdes. O contexto Spring completo só volta a subir quando o adaptador de `autorizacao` (tarefa 4.2) existir; a validação foi feita com um stub temporário, já removido.
- **Verificação do lote 2:** `AutorizacaoUseCasesTest` (10), `AutorizacaoPersistenciaTest` (6, PostgreSQL), `AutorizacaoControllerTest` (9, `@WebMvcTest`), `AutorizacaoApiTest` (6, ponta a ponta com JWT), `VarreduraSegurancaTest`, `ArchitectureTest` e `ModularityTests` verdes; contexto Spring completo sobe com o adaptador de `autorizacao`.
- **DTO aninhado gerado:** o gerador do módulo `autorizacao` cria seu próprio `AutorizacaoProcedimento` (enums aninhados `CategoriaEnum` etc.) em vez de reutilizar `generated.procedimento.model.Procedimento`; o controller converte `Resultado`/`Catalogo` para ele via `fromValue`. Sem mudança de contrato.
- **Ordenação da listagem:** `criada_em` decrescente (nulos por último) e `id` decrescente como desempate; `criada_em` é preenchida pela aplicação (truncada em microssegundos), como em `procedimento`.
- **Validação de página/tamanho:** feita no `ListarAutorizacoesUseCase` (0.., 1..200), além do Bean Validation do contrato; `400` pelo `GlobalExceptionHandler`.
- **Verificação do lote 3:** `AutorizacaoDesempenhoTest` mede `GET /autorizacoes` (Hibernate Statistics): 4 statements com 3 itens e 4 com 23 (constante, sem N+1). `ArchitectureTest` ganhou `autorizacao_domain_sem_framework`; 0 violações e nenhuma ocorrência de legado/legacy em `src/main`. `./mvnw verify` com Docker: 210 testes, 0 falhas, jar gerado.
- **Replay dos golden cases não executado (7.3):** exige API v1 e PostgreSQL em execução, o que o loop não deixa rodando; os cenários B09–B15 estão cobertos por `AutorizacaoApiTest`. Fica para a SPEC-31.

# SPEC-05 — Contrato OpenAPI v1 (procedimento, autorizacao, painel, erros)

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · contrato (consumido também pelo frontend) |
| Prioridade | P0 |
| Depende de | SPEC-03 |
| Sucessoras | SPEC-06, SPEC-07, SPEC-16, SPEC-22, SPEC-32 |
| Paralelismo | junto com SPEC-04 |

## 1. Objetivo
Criar o contrato OpenAPI 3.1 por módulo, com erros comuns RFC 9457, e a geração de interfaces (`interfaceOnly`) — **antes** de qualquer controller ou tela. O módulo `procedimento` nasce com o **contrato base** (sem os campos NEW-01, que entram em SPEC-12).
**Autorização para gerar:** `API_CONTRACTS.md` é a proposta completa exigida pela skill (endpoints, parâmetros, esquemas, erros, arquivos e execuções Maven). O executor registra que a proposta é a deste documento e **não** gera nada fora dela; qualquer mudança nova exige revisão de `API_CONTRACTS.md` e nova autorização (`shared/como-executar.md` §2).
**Restrições:** não copiar `ProblemDetail` em módulos; sem controllers/serviços nesta spec; não preservar o springfox.

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `01-architecture/API_CONTRACTS.md`, `00-product-discovery/BASELINE_COMPORTAMENTO_ORIGINAL.md`; skill `openapi-rest-contract` (`contract-conventions.md`, `modular-contracts.md`, `input-format.md`)

## 3. Tarefas
- [ ] **T1.** Inspecionar o `pom.xml` gerado (execução do gerador, pacotes) conforme a skill e confirmar o layout `openapi/modules` + `openapi/common`.
- [ ] **T2.** `openapi/common/responses.yaml`: `ProblemDetail` (com `code`, `timestamp`, `errors[]`) e respostas `400/403/404/500` padronizadas.
- [ ] **T3.** `openapi/modules/procedimento.yaml`: `listarProcedimentos`, `cadastrarProcedimento`, `obterProcedimento`; esquemas `Sexo`, `ProcedimentoRequest`, `Procedimento`, `PaginaProcedimentos`.
- [ ] **T4.** `openapi/modules/autorizacao.yaml`: `solicitarAutorizacao`, `listarAutorizacoes`; esquemas `AutorizacaoRequest`, `Autorizacao`, `PaginaAutorizacoes`.
- [ ] **T5.** `openapi/modules/painel.yaml`: `obterResumoPainel`; `ResumoPainel`.
- [ ] **T5b.** O módulo `auth` e o esquema de segurança `bearerAuth` são contratados em SPEC-22 (mesmo fluxo da skill, aprovado por `API_CONTRACTS.md` §6); aqui apenas garantir que a configuração do gerador aceite um quarto módulo.
- [ ] **T6.** Uma execução do gerador por módulo (IDs determinísticos, pacotes isolados, `spring`, `interfaceOnly=true`).
- [ ] **T7.** Validar com `validate.mjs` (OpenAPI 3.1, `$ref`, `operationId` únicos) e rodar a geração Maven.

**Aceite:** *Given* os YAMLs · *When* `./mvnw generate-sources` · *Then* interfaces geradas existem em pacotes separados por módulo, sem duplicidade de rota/`operationId`, e `ProblemDetail` é único.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1–T7 | `openapi-rest-contract` (obrigatória; usar `module-scaffold.mjs` e `validate.mjs`) |

## 5. Verificação obrigatória
- `node <skill>/scripts/validate.mjs api-autorizador-processo` → sem falhas
- `./mvnw -q generate-sources compile` → sucesso
- Conferência manual do YAML contra a tabela de `API_CONTRACTS.md` §1–§3 (operações, status, nomes) → sem diferenças
- Definition of Done: 6 operações e 3 módulos contratados, erros comuns únicos, nenhum código de negócio.

## 6. Resultado
_Preencher ao concluir._

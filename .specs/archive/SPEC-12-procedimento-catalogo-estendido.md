# SPEC-12 — Catálogo estendido do procedimento (nova feature de backend) — NEW-01

| Campo | Valor |
|---|---|
| Domínio / projeto | Backend · módulo `procedimento` |
| Prioridade | P1 |
| Depende de | SPEC-07 |
| Sucessoras | SPEC-17, SPEC-31, SPEC-32, SPEC-50 |
| Paralelismo | pode rodar junto com SPEC-08 |

## 1. Objetivo
O backend original só guarda número, sexos e idades; a tela **Cadastrar procedimento** do protótipo coleta mais dados. Por decisão do responsável do produto, esses dados entram como **nova feature de backend**, para que a interface não tenha campos "conceituais". Acrescenta de forma **aditiva** ao módulo `procedimento`: `nome`, `codigoTuss`, `categoria`, `especialidade`, `tipoCobertura`, `quantidadeMaxima`, `periodo`, `exigeAutorizacaoPrevia`, `exigeJustificativaMedica` (`API_CONTRACTS.md` §2).
**Restrições (críticas):** são **dados de catálogo**; **não** mudam `Procedimento.avaliar` nem a decisão de autorização (ADR-10, PEN-02); `numProcedimento` continua o código interno e imutável; registros anteriores têm os novos campos nulos; `nome` e `categoria` obrigatórios apenas na criação; `quantidadeMaxima` e `periodo` andam juntos; mudança de contrato exige a atualização pela skill `openapi-rest-contract` (módulo existente, execução reutilizada).

## 2. Ler antes
- **Sempre:** `.specs/memory/`, `.specs/shared/`
- **Desta spec:** `API_CONTRACTS.md` §2 (acréscimo NEW-01), `DATABASE_DESIGN.md` V4, `PRODUCT_CONTEXT.md`, `FUNCTIONAL_TRACEABILITY.md` F04–F06; protótipo (etapas 1, 2 e 4 do formulário)

## 3. Tarefas
- [ ] **T1.** Atualizar `openapi/modules/procedimento.yaml` (aditivo): novos campos em `ProcedimentoRequest`/`Procedimento`, enums `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo`; validações de `API_CONTRACTS.md`; regenerar e validar (`validate.mjs`). Nenhuma operação nova nem remoção.
- [ ] **T2.** `V4__catalogo_estendido.sql` conforme `DATABASE_DESIGN.md`; espelhar colunas nas tabelas `*_audit`.
- [ ] **T3.** Domínio: tipos `Categoria`, `Especialidade`, `TipoCobertura`, `Periodo`; invariantes (`quantidadeMaxima ≥ 1` ⇒ `periodo` obrigatório e vice-versa; `nome` 1..200 após trim); `Procedimento.create(...)` aceita os novos dados; **`avaliar` inalterado** (teste de regressão explícito).
- [ ] **T4.** Casos de uso e adaptador: persistir/ler os novos campos; padrões `tipoCobertura=PLANO`, booleanos `false`; saída nula para linhas antigas.
- [ ] **T5.** Controller: mapear request/response estendidos; erros `VALIDACAO_INVALIDA` com `errors[]` por campo.
- [ ] **T6.** Testes: domínio (invariantes), integração PostgreSQL (V4 sobre dados existentes das migrations V1–V3, sem perda), WebMvc (obrigatórios, combinações inválidas), ponta a ponta, regressão: toda a suíte de SPEC-07/08 continua verde sem alteração de expectativa.
- [ ] **T7.** Atualizar seed local (`db/seed`) com nome/categoria para as 3 linhas **somente** se os valores vierem do protótipo; caso contrário manter nulos e documentar.
- [ ] **T8.** Atualizar `tools/parity/cases.json` com casos NEW-01 (marcados `novo`, fora da paridade original).

**Aceite (G/W/T):** *Given* a API com V4 · *When* `POST /procedimentos` com `nome`, `categoria`, `tipoCobertura=REGRAS`, `quantidadeMaxima=2`, `periodo=ANO` · *Then* 201 e `GET` devolve os mesmos valores. *When* sem `nome` · *Then* 400 `VALIDACAO_INVALIDA` com `errors[campo=nome]`. *When* `quantidadeMaxima=2` sem `periodo` · *Then* 400. *Given* procedimento 1234 anterior · *Then* seus novos campos são `null` e `POST /autorizacoes` continua decidindo apenas por sexo e idade.

## 4. Skills e ferramentas
| Tarefa | Skill / ferramenta |
|---|---|
| T1 | `openapi-rest-contract` (atualização de módulo existente) |
| T2, T4, T6 | `java-persistence-performance` (migração segura, sem perda) |
| T3–T6 | `java-spring-create-module` (modo `create`/extensão do módulo), `java-modern-engineering` |

## 5. Verificação obrigatória
- `validate.mjs` → sem falhas; `./mvnw verify` → verde, incluindo regressão de SPEC-07/08
- `check-architecture.mjs` → 0 violações
- Teste de migração: banco com dados de V1–V3 recebe V4 sem perda (contagens iguais antes/depois)
- Definition of Done: contrato atualizado e coerente com o frontend (SPEC-17), `avaliar` comprovadamente inalterado, evidência de testes executados.

## 6. Resultado
Concluída em 2026-11-08 pela etapa `09-catalogo-estendido` do loop `test-01` (21 tarefas concluídas, nenhuma pendente). Evidência por tarefa em `.specs/workflows/test-01/specs/arquivo/09-catalogo-estendido/tarefas.md`; desvios e decisões em `proposta.md` da mesma pasta. Verificação global final (checkout limpo, Compose, e2e, paridade, contrato) em `.specs/05-quality/RELATORIO_FINAL.md`.
